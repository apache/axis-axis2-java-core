#!/usr/bin/env python3
"""
gen_mcp_schema.py — Build-time MCP inputSchema generator (Option 3)

Reads an Axis2/C service header file, finds *_request_t structs, maps C field
types to JSON Schema types, and writes mcpInputSchema parameters into the
corresponding services.xml.

Usage
-----
    python3 tools/gen_mcp_schema.py \\
        --header path/to/service.h \\
        --services path/to/services.xml \\
        [--prefix finbench_] \\
        [--encoding utf-8] \\
        [--dry-run]

The script writes in-place unless --dry-run is given, in which case it prints
the updated XML to stdout.

Limitations
-----------
- Nested structs and anonymous union members are NOT supported.  The struct
  body regex stops at the first '}', so inner struct/union blocks will cause
  field truncation.  A WARNING is printed when a parsed struct body contains
  a '{' character that suggests nesting.
- Only typedef struct { ... } name_t; patterns are detected.
- C preprocessor macros and conditional compilation (#if/#endif) are not
  evaluated; fields inside #ifdef blocks may be included unconditionally.

C → JSON Schema type mapping
-----------------------------
int / long / int32_t / int64_t / axis2_int32_t   → "integer"
double / float                                    → "number"
char * / axis2_char_t *                           → "string"
axis2_bool_t / bool / int (named is_*/has_*)      → "boolean"
pointer-to-struct (foo_t *)                       → "object"
double * / float * (numeric array pointers)       → "array"

Required fields: any field without a "= 0" / "= NULL" / "= false" default in
the struct definition is treated as required.  Fields matching *_id or n_*
are also always required.

The script uses regex-only parsing (no libclang) so it works without a C
toolchain installed.  It is conservative: when a type cannot be mapped
unambiguously, it emits "type": "object" and logs a warning.
"""

import argparse
import json
import os
import re
import sys
import tempfile
from pathlib import Path
from xml.sax.saxutils import escape as xml_escape

# ---------------------------------------------------------------------------
# C type → JSON Schema type table
# ---------------------------------------------------------------------------
_SCALAR_MAP = [
    # (regex_pattern, json_schema_type)
    (r'\bint\b|\blong\b|\bint32_t\b|\bint64_t\b|\buint32_t\b|\buint64_t\b'
     r'|\baxis2_int32_t\b|\bsize_t\b',      "integer"),
    (r'\bdouble\b|\bfloat\b',               "number"),
    (r'\baxis2_char_t\s*\*|\bchar\s*\*',    "string"),
    (r'\baxis2_bool_t\b|\bbool\b',          "boolean"),
]

_STRUCT_PTR_RE = re.compile(r'\b(\w+_t)\s*\*')


def c_type_to_json_schema(c_type: str, field_name: str) -> dict:
    """Map a C type string to a minimal JSON Schema dict."""
    c_type = c_type.strip()

    # Boolean heuristic: field named is_*/has_*/enable_*/use_* with int type
    if re.match(r'(is|has|enable|use)_', field_name) and re.search(r'\bint\b', c_type):
        return {"type": "boolean"}

    # Pointer to numeric array (double * / float * used for matrix/weight arrays)
    if re.search(r'\bdouble\s*\*|\bfloat\s*\*', c_type):
        return {"type": "array", "items": {"type": "number"}}

    for pattern, schema_type in _SCALAR_MAP:
        if re.search(pattern, c_type):
            return {"type": schema_type}

    m = _STRUCT_PTR_RE.search(c_type)
    if m:
        return {"type": "object"}

    # Fallback — conservative
    print(f"  WARNING: unmapped C type '{c_type}' for field '{field_name}' → object",
          file=sys.stderr)
    return {"type": "object"}


# ---------------------------------------------------------------------------
# Struct parser
# ---------------------------------------------------------------------------
_STRUCT_RE = re.compile(
    r'typedef\s+struct\s+\w*\s*\{([^}]+)\}\s*(\w+_t)\s*;',
    re.DOTALL
)
_FIELD_RE = re.compile(
    r'^\s*(?P<type>(?:const\s+)?[\w\s\*]+?)\s+(?P<name>\w+)\s*(?:=\s*(?P<default>[^;]+))?\s*;',
    re.MULTILINE
)
_BLOCK_COMMENT_RE = re.compile(r'/\*.*?\*/', re.DOTALL)


def _strip_comments(text: str) -> str:
    """Remove C block comments (/* ... */) and line comments (// ...)."""
    # Block comments first (may span lines)
    text = _BLOCK_COMMENT_RE.sub(' ', text)
    # Line comments
    text = re.sub(r'//[^\n]*', ' ', text)
    return text


def parse_structs(header_text: str) -> dict[str, dict]:
    """
    Return {struct_name: {field_name: {"c_type": ..., "has_default": bool}}}.
    Only parses typedef struct { ... } name_t; blocks.

    Block and line comments are stripped from the body before field parsing
    so that comment text containing ';' is not matched as a field.
    """
    structs = {}
    for m in _STRUCT_RE.finditer(header_text):
        body = m.group(1)
        name = m.group(2)

        # Warn about potential nested struct/union — body regex stops at first '}'
        # so any nested block would already be truncated, but alert the user.
        if '{' in body:
            print(f"  WARNING: struct '{name}' body contains '{{' — nested struct/union "
                  f"members are not supported and may be missing from the schema.",
                  file=sys.stderr)

        # Strip comments before field parsing (F23 fix)
        clean_body = _strip_comments(body)

        fields = {}
        for fm in _FIELD_RE.finditer(clean_body):
            field_name = fm.group("name")
            c_type     = fm.group("type")
            default    = fm.group("default")
            c_type_stripped = c_type.strip()
            # Skip residual preprocessor or empty captures
            if not c_type_stripped or c_type_stripped.startswith("#"):
                continue
            fields[field_name] = {
                "c_type":      c_type_stripped,
                "has_default": default is not None,
            }
        if fields:
            structs[name] = fields
    return structs


def build_json_schema(struct_fields: dict) -> dict:
    """Build a JSON Schema object from parsed struct fields."""
    properties = {}
    required = []

    # First pass: collect which fields are numeric array pointers
    array_fields = set()
    for fname, info in struct_fields.items():
        c_type = info["c_type"]
        if re.search(r'\bdouble\s*\*|\bfloat\s*\*', c_type):
            array_fields.add(fname)

    for fname, info in struct_fields.items():
        c_type      = info["c_type"]
        has_default = info["has_default"]

        # Skip pure size-companion fields (_count, _len, _size suffixes) that
        # exist only to carry the array length alongside a pointer field.
        # n_* fields are intentionally kept — they are primary input parameters.
        if re.search(r'_count$|_len$|_size$', fname) and fname not in array_fields:
            continue

        schema_prop = c_type_to_json_schema(c_type, fname)

        # Ensure array items type is set for numeric arrays
        if schema_prop.get("type") == "array" and not schema_prop.get("items"):
            schema_prop["items"] = {"type": "number"}

        properties[fname] = schema_prop

        # Required heuristic: no default declared, or name matches *_id / n_*
        always_required = bool(re.search(r'_id$|^n_', fname))
        if always_required or not has_default:
            required.append(fname)

    schema: dict = {"type": "object", "properties": properties}
    if required:
        schema["required"] = required
    return schema


# ---------------------------------------------------------------------------
# services.xml patcher
# ---------------------------------------------------------------------------
def find_request_struct(structs: dict, op_name: str,
                        prefix: str = "") -> str | None:
    """
    Heuristically find the request struct for an operation name.

    Tries (in order):
      {prefix}{op_name}_request_t
      {op_name}_request_t
      {op_name}_req_t
    Falls back to a case-insensitive substring search on all struct names.
    """
    candidates = []
    if prefix:
        candidates.append(f"{prefix}{op_name}_request_t")
    candidates += [
        f"{op_name}_request_t",
        f"{op_name}_req_t",
    ]
    for c in candidates:
        if c in structs:
            return c
    # Case-insensitive fallback
    op_lower = op_name.lower()
    for sname in structs:
        if op_lower in sname.lower() and "request" in sname.lower():
            return sname
    return None


_OP_RE = re.compile(
    r'(<operation\s+name="(?P<opname>[^"]+)"[^>]*>)',
    re.DOTALL
)
_EXISTING_SCHEMA_RE = re.compile(
    r'\s*<parameter\s+name="mcpInputSchema">.*?</parameter>',
    re.DOTALL
)


def patch_services_xml(xml_text: str, structs: dict,
                       prefix: str = "") -> tuple[str, list[str]]:
    """
    For each <operation name="..."> block, find the matching request struct
    and inject (or replace) a mcpInputSchema parameter.

    Patches are collected and applied in reverse position order to avoid
    offset corruption when multiple operations are in the same file (F22 fix).

    JSON inserted into XML is escaped with xml.sax.saxutils.escape() to
    prevent malformed XML if struct field names contain &, <, or > (F20 fix).

    Returns (patched_xml, list_of_change_messages).
    """
    messages = []

    # Collect all patches as (start, end, replacement) triples, then apply
    # in reverse order so earlier positions are not invalidated by later edits.
    patches: list[tuple[int, int, str]] = []

    for m in _OP_RE.finditer(xml_text):
        op_name = m.group("opname")
        struct_name = find_request_struct(structs, op_name, prefix)
        if struct_name is None:
            messages.append(f"  SKIP {op_name}: no matching *_request_t struct found")
            continue

        schema = build_json_schema(structs[struct_name])
        # indent=2 produces readable XML; xml_escape protects against
        # JSON characters that are XML-special (&, <, >) (F20, F28 fix)
        schema_json = xml_escape(json.dumps(schema, indent=2))
        param_block = f'<parameter name="mcpInputSchema">{schema_json}</parameter>'

        op_start = m.start()
        tag_end  = m.end()   # end of the <operation ...> opening tag

        # Find the closing </operation> tag from op_start in the ORIGINAL text
        close_m = re.search(r'</operation>', xml_text[op_start:])
        if close_m is None:
            messages.append(f"  SKIP {op_name}: no </operation> closing tag found")
            continue

        op_end   = op_start + close_m.end()
        op_block = xml_text[op_start:op_end]

        if '<parameter name="mcpInputSchema">' in op_block:
            # Replace existing parameter — find its absolute span
            existing_m = _EXISTING_SCHEMA_RE.search(xml_text, op_start, op_end)
            if existing_m:
                patches.append((
                    existing_m.start(),
                    existing_m.end(),
                    "\n            " + param_block
                ))
                messages.append(
                    f"  UPDATE {op_name}: replaced mcpInputSchema from {struct_name}")
        else:
            # Insert immediately after the opening <operation ...> tag
            patches.append((
                tag_end,
                tag_end,
                "\n            " + param_block
            ))
            messages.append(
                f"  INSERT {op_name}: wrote mcpInputSchema from {struct_name}")

    # Apply patches in reverse order (largest offset first) to preserve positions
    patches.sort(key=lambda t: t[0], reverse=True)
    result = xml_text
    for start, end, replacement in patches:
        result = result[:start] + replacement + result[end:]

    return result, messages


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------
def main() -> None:
    p = argparse.ArgumentParser(description=__doc__,
                                formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--header",   required=True,
                   help="Path to .h file containing *_request_t structs")
    p.add_argument("--services", required=True,
                   help="Path to services.xml to patch in-place")
    p.add_argument("--prefix",   default="",
                   help="Application-specific struct name prefix (e.g. 'finbench_'). "
                        "Default: no prefix.")
    p.add_argument("--encoding", default="utf-8",
                   help="File encoding for both header and services.xml. Default: utf-8")
    p.add_argument("--dry-run",  action="store_true",
                   help="Print patched XML to stdout; do not write the file")
    args = p.parse_args()

    header_path   = Path(args.header).resolve()
    services_path = Path(args.services).resolve()

    if not header_path.exists():
        sys.exit(f"ERROR: header not found: {header_path}")
    if not services_path.exists():
        sys.exit(f"ERROR: services.xml not found: {services_path}")

    try:
        header_text = header_path.read_text(encoding=args.encoding)
    except UnicodeDecodeError as e:
        sys.exit(f"ERROR: cannot decode {header_path} as {args.encoding}: {e}\n"
                 f"       Try --encoding latin-1 or --encoding utf-8-sig")

    try:
        services_text = services_path.read_text(encoding=args.encoding)
    except UnicodeDecodeError as e:
        sys.exit(f"ERROR: cannot decode {services_path} as {args.encoding}: {e}\n"
                 f"       Try --encoding latin-1 or --encoding utf-8-sig")

    structs = parse_structs(header_text)
    if not structs:
        sys.exit("ERROR: no 'typedef struct { } name_t;' blocks found in header")

    print(f"Parsed {len(structs)} structs from {header_path.name}:", file=sys.stderr)
    for sname in structs:
        print(f"  {sname} ({len(structs[sname])} fields)", file=sys.stderr)

    patched, messages = patch_services_xml(services_text, structs,
                                           prefix=args.prefix)

    print("Schema generation results:", file=sys.stderr)
    for msg in messages:
        print(msg, file=sys.stderr)

    if args.dry_run:
        print(patched)
    else:
        # Atomic write: write to a sibling temp file, then rename (F24 fix)
        tmp_fd, tmp_path = tempfile.mkstemp(
            dir=services_path.parent,
            prefix=".gen_mcp_schema_",
            suffix=".tmp"
        )
        try:
            with os.fdopen(tmp_fd, "w", encoding=args.encoding) as fh:
                fh.write(patched)
            os.replace(tmp_path, services_path)
        except Exception:
            # Clean up temp file if rename failed
            try:
                os.unlink(tmp_path)
            except OSError:
                pass
            raise
        print(f"Written: {services_path}", file=sys.stderr)


if __name__ == "__main__":
    main()

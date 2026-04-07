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
        [--dry-run]

The script writes in-place unless --dry-run is given, in which case it prints
the updated XML to stdout.

C → JSON Schema type mapping
-----------------------------
int / long / int32_t / int64_t / axis2_int32_t   → "integer"
double / float                                    → "number"
char * / axis2_char_t *                           → "string"
axis2_bool_t / bool / int (named is_*/has_*)      → "boolean"
pointer-to-struct (foo_t *)                       → "object"
array + companion _count / n_ field               → "array"

Required fields: any field without a "= 0" / "= NULL" / "= false" default in
the struct definition is treated as required.  Fields named *_id, n_*, count_*
are also always required.

The script uses regex-only parsing (no libclang) so it works without a C
toolchain installed.  It is conservative: when a type cannot be mapped
unambiguously, it emits "type": "object" and logs a warning.
"""

import argparse
import json
import re
import sys
import textwrap
from pathlib import Path

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

    # Boolean heuristic: field named is_*/has_* with int type
    if re.match(r'(is|has|enable|use)_', field_name) and re.search(r'\bint\b', c_type):
        return {"type": "boolean"}

    # Pointer to array (double * / float * used for matrix/weight arrays)
    if re.search(r'\bdouble\s*\*|\bfloat\s*\*', c_type):
        return {"type": "array", "items": {"type": "number"}}

    for pattern, schema_type in _SCALAR_MAP:
        if re.search(pattern, c_type):
            return {"type": schema_type}

    m = _STRUCT_PTR_RE.search(c_type)
    if m:
        return {"type": "object"}

    # Fallback
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


def parse_structs(header_text: str) -> dict[str, dict]:
    """
    Return {struct_name: {field_name: {"c_type": ..., "has_default": bool}}}.
    Only parses typedef struct { ... } name_t; blocks.
    """
    structs = {}
    for m in _STRUCT_RE.finditer(header_text):
        body = m.group(1)
        name = m.group(2)
        fields = {}
        for fm in _FIELD_RE.finditer(body):
            field_name = fm.group("name")
            c_type     = fm.group("type")
            default    = fm.group("default")
            # Skip comment-only or empty lines picked up by the regex
            if c_type.strip().startswith("//") or c_type.strip().startswith("*"):
                continue
            fields[field_name] = {
                "c_type":      c_type.strip(),
                "has_default": default is not None,
            }
        if fields:
            structs[name] = fields
    return structs


def build_json_schema(struct_fields: dict) -> dict:
    """Build a JSON Schema object from parsed struct fields."""
    properties = {}
    required = []

    # Fields that are always array companions (paired with n_* / *_count) — skip them
    # as array size information; they are implicit.
    companion_size_re = re.compile(r'^n_|_count$|_len$|_size$')

    # First pass: collect array-indicator field names
    array_fields = set()
    for fname, info in struct_fields.items():
        c_type = info["c_type"]
        if re.search(r'\bdouble\s*\*|\bfloat\s*\*', c_type):
            array_fields.add(fname)

    for fname, info in struct_fields.items():
        c_type      = info["c_type"]
        has_default = info["has_default"]

        # Skip size companion fields (n_assets accompanies weights[], etc.)
        if companion_size_re.search(fname) and fname not in array_fields:
            # Keep n_assets as it is the primary dimension parameter
            if not fname.startswith("n_"):
                continue

        schema_prop = c_type_to_json_schema(c_type, fname)

        # Annotate array items for common financial arrays
        if schema_prop.get("type") == "array" and not schema_prop.get("items"):
            schema_prop["items"] = {"type": "number"}

        properties[fname] = schema_prop

        # Required: no default AND not a companion size field
        always_required = re.match(r'.+_id$|^n_', fname)
        if always_required or not has_default:
            required.append(fname)

    schema = {
        "type": "object",
        "properties": properties,
    }
    if required:
        schema["required"] = required
    return schema


# ---------------------------------------------------------------------------
# services.xml patcher
# ---------------------------------------------------------------------------
def find_request_struct(structs: dict, op_name: str) -> str | None:
    """
    Heuristically find the request struct for an operation name.
    Tries: finbench_{op_name}_request_t, {op_name}_request_t, {op_name}_req_t
    """
    service_prefix = "finbench_"
    candidates = [
        f"{service_prefix}{op_name}_request_t",
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


def patch_services_xml(xml_text: str, structs: dict) -> tuple[str, list[str]]:
    """
    For each <operation name="..."> block, find the matching request struct
    and inject (or replace) a mcpInputSchema parameter.

    Returns (patched_xml, list_of_change_messages).
    """
    messages = []
    result = xml_text

    for m in _OP_RE.finditer(xml_text):
        op_name = m.group("opname")
        struct_name = find_request_struct(structs, op_name)
        if struct_name is None:
            messages.append(f"  SKIP {op_name}: no matching *_request_t struct found")
            continue

        schema = build_json_schema(structs[struct_name])
        schema_json = json.dumps(schema, indent=16)
        param_block = f'<parameter name="mcpInputSchema">{schema_json}</parameter>'

        # Check if an mcpInputSchema already exists after this <operation ...> tag
        op_start = m.start()
        # Find the closing </operation>
        close_re = re.compile(r'</operation>', re.DOTALL)
        close_m = close_re.search(result, op_start)
        if close_m is None:
            continue
        op_block = result[op_start:close_m.end()]

        if '<parameter name="mcpInputSchema">' in op_block:
            # Replace existing
            new_op_block = _EXISTING_SCHEMA_RE.sub(
                "\n            " + param_block, op_block)
            result = result[:op_start] + new_op_block + result[close_m.end():]
            messages.append(f"  UPDATE {op_name}: replaced mcpInputSchema from {struct_name}")
        else:
            # Insert after the opening <operation ...> tag
            tag_end = op_start + len(m.group(1))
            indent = "\n            "
            result = (result[:tag_end]
                      + indent + param_block
                      + result[tag_end:])
            messages.append(f"  INSERT {op_name}: wrote mcpInputSchema from {struct_name}")

    return result, messages


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------
def main():
    p = argparse.ArgumentParser(description=__doc__,
                                formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--header",   required=True, help="Path to .h file")
    p.add_argument("--services", required=True, help="Path to services.xml")
    p.add_argument("--dry-run",  action="store_true",
                   help="Print patched XML to stdout, do not write")
    args = p.parse_args()

    header_path   = Path(args.header)
    services_path = Path(args.services)

    if not header_path.exists():
        sys.exit(f"ERROR: header not found: {header_path}")
    if not services_path.exists():
        sys.exit(f"ERROR: services.xml not found: {services_path}")

    header_text   = header_path.read_text(encoding="utf-8")
    services_text = services_path.read_text(encoding="utf-8")

    structs = parse_structs(header_text)
    if not structs:
        sys.exit("ERROR: no typedef struct { } name_t; blocks found in header")

    print(f"Parsed {len(structs)} structs from {header_path.name}:", file=sys.stderr)
    for sname in structs:
        print(f"  {sname} ({len(structs[sname])} fields)", file=sys.stderr)

    patched, messages = patch_services_xml(services_text, structs)

    print("Schema generation results:", file=sys.stderr)
    for msg in messages:
        print(msg, file=sys.stderr)

    if args.dry_run:
        print(patched)
    else:
        services_path.write_text(patched, encoding="utf-8")
        print(f"Written: {services_path}", file=sys.stderr)


if __name__ == "__main__":
    main()

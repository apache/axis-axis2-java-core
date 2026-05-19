## DEVELOPMENT CERTIFICATES ONLY — DO NOT USE IN PRODUCTION

These certificates are for the Axis2 MCP bridge mTLS demo and
the springbootdemo-tomcat11 sample application. They are:

- **Self-signed** (not issued by a trusted CA)
- **Password: `changeit`** (hardcoded in sample configs)
- **Committed to a public repository** (the CA private key is public)

Using these certificates in production would provide zero security —
any attacker can download the CA key from this repository and forge
client certificates.

For production mTLS, generate your own PKI with a proper CA that is
NOT committed to source control.

### Files

| File | Purpose |
|------|---------|
| `ca.key` / `ca.crt` | Root CA (RSA 4096, 10-year validity) |
| `server.key` / `server.crt` | Server cert for `localhost` |
| `server-keystore.p12` | Tomcat server keystore |
| `ca-truststore.p12` | Tomcat truststore (CA cert only) |
| `client.key` / `client.crt` | Client cert (`CN=axis2-mcp-bridge`) |
| `client-keystore.p12` | MCP bridge client keystore |

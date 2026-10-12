# Security Review

## Implemented controls

- BCrypt password hashing. Registration validates name/email/password length and checks UTF-8 password size against BCrypt's 72-byte limit.
- JWT signature validation, expiry parsing, issuer requirement, nonblank subject, and role allowlist (`USER`, `ADMIN`). Secret is Base64-decoded from `JWT_SECRET` and must contain at least 32 bytes.
- Stateless Spring Security with bearer-token authentication, backend role checks for admin APIs, and ownership checks for itinerary and booking operations.
- Exact-origin CORS configuration from `CORS_ALLOWED_ORIGINS`; credentials are disabled because the frontend uses Authorization headers. PATCH is allowed for admin status workflows.
- User CRUD is admin-only, excludes raw entity/password-hash fields, hashes new/reset passwords, prevents deleting the signed-in or last admin, and blocks deletion of accounts that own trips/bookings. The JWT filter reads each account's current persisted role on requests so role changes and account deletion revoke prior privileges.
- Safe generic unexpected API error response; no stacktrace or binding details returned by Spring's default error mapping.
- `.env` files are ignored by `.gitignore`; `package_release.py` excludes them from release archives and keeps only `.env.example` templates.

## Risks and required operational actions

- The previously supplied ZIP did contain `backend/.env` and the application properties included credential fallbacks. Treat values in that archive as exposed. Rotate database credentials, the admin password, and JWT signing key before deployment; removing values from a newer source tree does not revoke or erase old copies.
- An old database may already contain the former demo administrator account. The code no longer seeds fixed demo credentials, but it does not rewrite or delete existing accounts. An operator must change that account's password through a controlled process before exposing the database.
- JWT is stored by the existing frontend in browser local storage, which increases impact of a frontend XSS. A same-site HttpOnly cookie design would need a separate CSRF-aware migration.
- No login rate limiting, account lockout, user self-service password reset, MFA, or admin audit trail is implemented.
- Registration and error paths should be reviewed for account-enumeration policy before public launch; duplicate registration currently returns a conflict.
- CORS and secret configuration must be set per environment; never allow wildcard origins or weak test keys in production.
- SQL schema setup and migration have not been exercised against a disposable or production-like MySQL instance. No migration is automatically applied by the application.

## Verification scope

JWT unit tests were added for accepted tokens, wrong signing key, role validation, and weak configuration. Full endpoint authorization tests, dependency CVE scan, secret history scan, and MySQL-backed integration tests remain required. A successful application start alone is not a security assessment.

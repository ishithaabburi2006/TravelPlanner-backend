# Test and Build Report

Date: 2026-10-11. This report records checks run against the current workspace after the dashboard edit and release-packaging changes.

## Automated tests

- `JwtServiceTest`: 4 cases for accepted tokens, wrong-signature rejection, invalid role generation, and weak configuration.
- `AdminUserManagementTest`: 3 cases for password hashing and protections against last-admin deletion and self-demotion.
- `ItineraryControllerTest`: 3 cases for owner budget updates, cross-user update rejection, and missing-trip 404 behavior.
- No browser tests, full Spring MVC security tests, or MySQL integration tests are present.

## Verification results

| Command | Result |
|---|---|
| `npm ci --prefer-offline` from the clean ZIP extract | PASS; installed 52 packages. npm warned that `fsevents` install scripts are pending approval; the build did not require running that script. |
| `npm run build` from the clean ZIP extract | PASS; Vite 8.3.1 built the frontend production bundle. |
| `mvn -o clean test` | BLOCKED after compiling 33 main and 3 test sources; Maven Surefire's JUnit platform provider was not in the local cache and offline mode could not download it. **No tests executed.** |
| `mvn -o clean package -DskipTests` | PASS; Java 21 compiled and packaged the Spring Boot JAR. Tests were skipped. |
| `python3 package_release.py` + ZIP content check | PASS; generated a 77-file source archive. Confirmed no `.env`, `node_modules`, `target`, `dist`, `.git`, or `__MACOSX` entries; env templates and lockfile are included. |
| MySQL clean-install / upgrade | NOT RUN; no disposable MySQL instance was used. No live database was changed. |
| Dependency vulnerability scan | NOT RUN; this report does not make a dependency CVE claim. |

## Remaining verification

Run Maven tests with Maven Central access or the JUnit Surefire provider available, then run the ownership and lifecycle workflows against a disposable MySQL database. Verify both `database/schema.sql` on an empty database and the documented one-time migration on a copy of the existing schema. End-to-end account-to-trip-to-booking persistence and privilege tests remain outstanding.

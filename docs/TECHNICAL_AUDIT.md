# Technical Audit

Audit date: 2026-10-11. This review covers the source tree in this workspace, not an independently deployed environment. Findings are evidence-based and distinguish source defects from operational items that still need verification.

## Findings and remediation

| Severity | Finding / evidence | Impact | Status / verification |
|---|---|---|---|
| High | A previously supplied release ZIP included `backend/.env`, generated Maven output, frontend dependencies, and macOS metadata. The earlier archive should be treated as having exposed its values. | Account/database compromise risk and unprofessional distribution. | Added `package_release.py`, which excludes local env files, generated output, dependency directories, and OS metadata while retaining `.env.example`. Rotate any credentials in an archive already shared. Verify the new ZIP before distribution. |
| High | The startup configuration requires `DB_PASSWORD` and `JWT_SECRET`; credentials no longer have application-property fallbacks. | Weak defaults could let an unintended deployment start with known credentials. | Confirmed current `application.properties` requires the DB password and JWT secret; admin seeding has no default email/password. |
| High | Existing database schema may not have `booking.travelers`; supplied startup logs showed Hibernate validation failing on that column. | Backend does not start against the mismatched schema. | Clean-install DDL is now in `database/schema.sql`; documented the one-time existing-database migration. No live database was changed. |
| Medium | Dashboard previously exposed no trip-edit action even though the API supports `PUT /api/itineraries/{id}`. | Users could not fix a saved budget or trip details through the app. | Added edit action and form prefilling via the owner-protected trip summary endpoint; update uses the existing API. Frontend production build passes; interactive browser flow is not tested. |
| Medium | The startup destination seeder reused IDs and saved all seeded destinations, overwriting admin edits on restart. | Admin-managed destination content could be lost. | Seeder now inserts only missing destination names. Verify with a disposable MySQL restart test; not run against user data. |
| Medium | Destination and attraction missing-resource paths used bare `orElseThrow()`. | Missing records could be reported as generic server errors. | Destination and attraction detail controllers now return explicit 404 responses. Backend compilation passes; test execution is blocked by the unavailable Surefire provider. |
| Medium | Several collection endpoints are unpaginated; activity/day plans are free text; budgets use floating-point values. | Growth, scheduling, and financial accuracy limitations. | Documented as unresolved; API contract and data migration need deliberate design. |
| Medium | Attraction data, distances, times, and remote image licensing are not verified. | Commercial travel information could be incomplete, inaccurate, or improperly licensed. | No factual claims upgraded; provenance verification remains a release requirement. |
| Low | Initial documentation described local Hibernate `update` and referred to a missing backend template. | Setup confusion and risk of accidental schema mutation. | Local configuration now validates schema; setup docs refer to the included `backend/.env.example` and clean-install schema. |

## Architecture

React/Vite frontend calls a Java 21 Spring Boot REST API. Spring Data JPA persists to MySQL. JWT bearer tokens authenticate users; backend method security protects admin endpoints. Controllers currently call repositories directly; DTOs cover key account/trip/booking boundaries, with ownership checks in controller logic. Attraction seed data is maintained in Java source. The project has no automated migration runner, deployment manifests, or end-to-end browser suite.

## Release gate

Not ready for commercial production deployment. Before approval, run backend tests with the required Maven artifacts available, run MySQL integration and authorization tests, verify clean and upgrade database paths on disposable databases, add pagination and browser workflow tests, and validate data/image licensing. Assess the newly generated release archive for secret-free contents before sharing.

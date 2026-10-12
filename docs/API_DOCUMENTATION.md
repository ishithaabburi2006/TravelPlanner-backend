# API Reference

Base path: `/api`. Except registration/login and public discovery, requests require `Authorization: Bearer <JWT>`. JSON request bodies use `Content-Type: application/json`. Admin paths require role `ADMIN`.

| Method | Path | Access | Behavior |
|---|---|---|---|
| POST | `/auth/register` | Public | Register USER; request validates account fields. |
| POST | `/auth/login` | Public | Authenticate and return JWT. |
| GET | `/destinations?q=&type=` | Public | List/search destinations. |
| GET | `/destinations/{id}` | Public | Destination detail. |
| GET | `/attractions/destination/{destinationId}` | Public | List destination attractions. |
| GET | `/attractions/{id}` | Public | Attraction detail. |
| GET | `/itineraries` | USER/ADMIN | Current user's itineraries. |
| POST | `/itineraries` | USER/ADMIN | Create a personal itinerary; every request is saved as PENDING for admin review, regardless of budget. |
| PUT | `/itineraries/{id}` | Owner | Update an owned itinerary. |
| DELETE | `/itineraries/{id}` | Owner | Delete an owned itinerary. |
| GET | `/itineraries/{id}/summary` | Owner | Itinerary duration and daily budget summary. |
| GET | `/bookings` | USER/ADMIN | Current user's booking records. |
| POST | `/bookings` | USER/ADMIN | Create a locally recorded booking, optionally linked to owned itinerary. |
| DELETE | `/bookings/{id}` | Owner | Cancel a record; completed records cannot be cancelled. |
| GET | `/admin/stats` | ADMIN | Database counts. |
| GET | `/admin/users` | ADMIN | User ID, name, email, role DTO list. |
| POST | `/admin/users` | ADMIN | Create a USER or ADMIN; password is BCrypt-hashed. |
| PUT | `/admin/users/{id}` | ADMIN | Update name, email, role, and optionally password. Self email/role changes are restricted; last admin cannot be demoted. |
| DELETE | `/admin/users/{id}` | ADMIN | Delete account only if it is not the signed-in admin, is not the last admin, and owns no trips or booking records. |
| GET | `/admin/destinations` | ADMIN | Destination list. |
| POST/PUT/DELETE | `/admin/destinations[/{id}]` | ADMIN | Destination CRUD. |
| GET | `/admin/bookings` | ADMIN | Booking record list. |
| PATCH | `/admin/bookings/{id}/status?status=` | ADMIN | Change booking record status using permitted transitions. |
| GET | `/admin/itineraries` | ADMIN | Itinerary list. |
| PATCH | `/admin/itineraries/{id}/status?status=APPROVED\|REJECTED` | ADMIN | Review a PENDING trip request. |
| DELETE | `/admin/itineraries/{id}` | ADMIN | Delete only when no bookings are linked. |

Errors from API advice use `{timestamp,status,error,message,path}` for handled failures. Spring Security's 401/403 responses may use the framework entry point format. Attraction admin management, pagination, booking edit, and actual provider booking endpoints do not exist.

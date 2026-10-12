# Database Schema

The application uses MySQL 8, Spring Data JPA, and Hibernate schema validation. `database/schema.sql` is the clean-install schema and creates tables in the currently selected database without creating, dropping, or switching databases. Apply it only to a new, empty database. It includes the booking traveler count and relational constraints for users, trips, bookings, and attractions.

## Tables and relationships

- `users`: account identity, BCrypt password hash, and USER/ADMIN role.
- `destination`: searchable destination guides and estimated budgets.
- `attractions`: destination-linked nearby attractions. The current Java entity represents `destination_id` as a scalar rather than a JPA association.
- `itinerary`: trip plan owned by one user; dates, budget, traveller count, and planning text are currently stored on one record.
- `booking`: locally recorded booking details owned by a user and optionally linked to an itinerary. It does not represent a provider-confirmed reservation.

Indexes include unique user email, attraction destination/name, booking user/date, booking itinerary/status, and itinerary owner. Confirm the physical schema and indexes in MySQL after setup.

## Existing database upgrades

There is no migration framework. For an older database that lacks `booking.travelers`, back up the database, confirm the column is absent, and apply `database/migrations/manual_booking_travelers.sql` once. The startup profile uses `ddl-auto=validate` and fails safely when the schema does not match; it does not modify production data or table structure.

Budgets and booking amounts are currently Java `double` values, and currency is assumed to be INR in the interface. A future migration should move financial fields to fixed-precision decimal and store currency explicitly before supporting multiple currencies or financial reconciliation.

Attraction distance/time values are estimates. Coordinates, opening hours, entrance fees, accessibility, and seasonal restrictions are not stored. Destination/attraction seed text and remote images are not verified licensed commercial travel data.

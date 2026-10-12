-- Clean-install schema for MySQL 8. Select/create an empty application
-- database before applying this file. Existing installations should take a
-- backup and apply only the documented migration(s); do not drop or recreate
-- an existing database.

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(255),
  PRIMARY KEY (id),
  CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS destination (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  location VARCHAR(255),
  state VARCHAR(255),
  type VARCHAR(255),
  interests VARCHAR(255),
  average_budget DOUBLE NOT NULL,
  recommended_days INT NOT NULL,
  best_time VARCHAR(255),
  travel_from_hyderabad VARCHAR(255),
  ideal_for VARCHAR(255),
  highlights VARCHAR(1800),
  description VARCHAR(1800),
  image_url VARCHAR(255),
  PRIMARY KEY (id),
  INDEX idx_destination_name (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS attractions (
  id BIGINT NOT NULL AUTO_INCREMENT,
  destination_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  category VARCHAR(255),
  distance_km DOUBLE NOT NULL,
  estimated_minutes INT NOT NULL,
  best_for VARCHAR(255),
  visit_duration VARCHAR(255),
  description VARCHAR(1600),
  image_url VARCHAR(255),
  map_url VARCHAR(255),
  PRIMARY KEY (id),
  INDEX idx_attraction_destination (destination_id),
  INDEX idx_attraction_name (name),
  CONSTRAINT fk_attraction_destination FOREIGN KEY (destination_id)
    REFERENCES destination (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS itinerary (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  destination VARCHAR(255),
  start_date DATE,
  end_date DATE,
  budget DOUBLE NOT NULL,
  travelers INT NOT NULL,
  travel_mode VARCHAR(255),
  accommodation VARCHAR(255),
  food_preference VARCHAR(255),
  activities VARCHAR(2200),
  day_plan VARCHAR(3500),
  notes VARCHAR(3000),
  status VARCHAR(255),
  user_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_itinerary_user (user_id),
  CONSTRAINT fk_itinerary_user FOREIGN KEY (user_id)
    REFERENCES users (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS booking (
  id BIGINT NOT NULL AUTO_INCREMENT,
  service_type VARCHAR(255),
  provider VARCHAR(255),
  booking_reference VARCHAR(255),
  details VARCHAR(255),
  booking_date DATE,
  amount DOUBLE NOT NULL,
  status VARCHAR(255) NOT NULL,
  travelers INT NOT NULL DEFAULT 1,
  user_id BIGINT NOT NULL,
  itinerary_id BIGINT,
  PRIMARY KEY (id),
  INDEX idx_booking_user_date (user_id, booking_date),
  INDEX idx_booking_itinerary (itinerary_id),
  INDEX idx_booking_status (status),
  CONSTRAINT fk_booking_user FOREIGN KEY (user_id)
    REFERENCES users (id) ON DELETE RESTRICT,
  CONSTRAINT fk_booking_itinerary FOREIGN KEY (itinerary_id)
    REFERENCES itinerary (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

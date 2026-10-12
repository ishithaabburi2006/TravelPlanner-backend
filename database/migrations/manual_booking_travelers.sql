-- Apply once to databases created before booking traveler counts were introduced.
ALTER TABLE booking ADD COLUMN travelers INT NOT NULL DEFAULT 1;

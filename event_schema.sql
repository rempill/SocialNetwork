-- SQL schema for Event repository tables
-- This script creates the necessary tables for the PostgresEventRepository

-- Create the event table to store event data
CREATE TABLE IF NOT EXISTS event (
    id INTEGER PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Create the event_subscription table to store event-user subscriptions (many-to-many relationship)
-- This table links events to users who are subscribed to them
CREATE TABLE IF NOT EXISTS event_subscription (
    event_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (event_id, user_id),
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user_base(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_event_subscription_event_id ON event_subscription(event_id);
CREATE INDEX IF NOT EXISTS idx_event_subscription_user_id ON event_subscription(user_id);

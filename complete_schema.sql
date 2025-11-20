-- Complete SQL schema for the Social Network application
-- This includes both User and Event related tables

-- ============================================
-- USER TABLES (for reference - should already exist)
-- ============================================

-- Base user table (required for event subscriptions)
-- CREATE TABLE IF NOT EXISTS user_base (
--     id INTEGER PRIMARY KEY,
--     username VARCHAR(255) NOT NULL UNIQUE,
--     email VARCHAR(255) NOT NULL UNIQUE,
--     password VARCHAR(255) NOT NULL
-- );

-- Persoana (person) subtype table
-- CREATE TABLE IF NOT EXISTS persoana (
--     id INTEGER PRIMARY KEY,
--     nume VARCHAR(255) NOT NULL,
--     prenume VARCHAR(255) NOT NULL,
--     ocupatie VARCHAR(255),
--     data_nasterii DATE NOT NULL,
--     nivel_empatie INTEGER,
--     FOREIGN KEY (id) REFERENCES user_base(id) ON DELETE CASCADE
-- );

-- Duck subtype table
-- CREATE TABLE IF NOT EXISTS duck (
--     id INTEGER PRIMARY KEY,
--     tip_rata VARCHAR(50) NOT NULL,
--     viteza DOUBLE PRECISION NOT NULL,
--     rezistenta DOUBLE PRECISION NOT NULL,
--     FOREIGN KEY (id) REFERENCES user_base(id) ON DELETE CASCADE
-- );

-- User friendships table (symmetric undirected friendship)
-- CREATE TABLE IF NOT EXISTS user_friend (
--     user_id INTEGER NOT NULL,
--     friend_id INTEGER NOT NULL,
--     PRIMARY KEY (user_id, friend_id),
--     FOREIGN KEY (user_id) REFERENCES user_base(id) ON DELETE CASCADE,
--     FOREIGN KEY (friend_id) REFERENCES user_base(id) ON DELETE CASCADE,
--     CHECK (user_id < friend_id)
-- );

-- ============================================
-- EVENT TABLES (new - need to be created)
-- ============================================

-- Event table to store event data
CREATE TABLE IF NOT EXISTS event (
    id INTEGER PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Event subscription table to store event-user subscriptions (many-to-many relationship)
-- This table links events to users who are subscribed to them
CREATE TABLE IF NOT EXISTS event_subscription (
    event_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (event_id, user_id),
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user_base(id) ON DELETE CASCADE
);

-- Create indexes for better query performance on event_subscription
CREATE INDEX IF NOT EXISTS idx_event_subscription_event_id ON event_subscription(event_id);
CREATE INDEX IF NOT EXISTS idx_event_subscription_user_id ON event_subscription(user_id);

-- ============================================
-- SAMPLE DATA (optional - for testing)
-- ============================================

-- Insert sample events
-- INSERT INTO event (id, name) VALUES 
--     (1, 'Tech Meetup'),
--     (2, 'Social Gathering'),
--     (3, 'Community Event')
-- ON CONFLICT (id) DO NOTHING;

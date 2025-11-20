# Database Setup Instructions

This document explains how to set up the PostgreSQL database tables for the Social Network application.

## Prerequisites

- PostgreSQL installed and running
- Database named `duck_social_network` created (or use your preferred database name)
- User with appropriate privileges

## Setting up the Event Tables

If you already have the user tables set up, you only need to run the event schema:

```bash
psql -U postgres -d duck_social_network -f event_schema.sql
```

Or connect to your database and execute:

```sql
\i event_schema.sql
```

## Setting up Complete Schema

If you're setting up the database from scratch, use the complete schema file:

```bash
psql -U postgres -d duck_social_network -f complete_schema.sql
```

**Note:** The complete schema file has the user tables commented out. If you need those tables, uncomment them first.

## Manual Table Creation

You can also create the event tables manually by running these commands in your PostgreSQL client:

```sql
-- Create event table
CREATE TABLE event (
    id INTEGER PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- Create event subscription table
CREATE TABLE event_subscription (
    event_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (event_id, user_id),
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user_base(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_event_subscription_event_id ON event_subscription(event_id);
CREATE INDEX idx_event_subscription_user_id ON event_subscription(user_id);
```

## Verification

After creating the tables, verify they exist:

```sql
\dt event*
```

You should see:
- `event`
- `event_subscription`

## Tables Overview

### event
- `id` (INTEGER, PRIMARY KEY): Unique event identifier
- `name` (VARCHAR(255), NOT NULL): Event name/title

### event_subscription
- `event_id` (INTEGER, FK to event.id): Reference to the event
- `user_id` (INTEGER, FK to user_base.id): Reference to the subscribed user
- Primary key on (event_id, user_id) to prevent duplicate subscriptions
- CASCADE delete: When an event or user is deleted, subscriptions are automatically removed

## Connection Details

Update the connection details in `Main.java` if needed:

```java
String dbUrl = "jdbc:postgresql://localhost:5432/duck_social_network";
String dbUser = "postgres";
String dbPass = "your_password";
```

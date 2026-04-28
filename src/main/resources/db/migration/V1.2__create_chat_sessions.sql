-- ============================================
-- ms-java-biz Database Migration
-- Version: V1.2
-- Description: Create chat_sessions table and auto-sync trigger
-- ============================================

-- 1. Create chat_sessions table
CREATE TABLE IF NOT EXISTS chat_sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255),
    last_active_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for sorting by last_active_time
CREATE INDEX IF NOT EXISTS idx_chat_sessions_last_active ON chat_sessions(last_active_time DESC);

-- 2. Create function to sync session data from chat_messages
CREATE OR REPLACE FUNCTION sync_chat_session()
RETURNS TRIGGER AS $$
BEGIN
    -- If it's a new message
    IF (TG_OP = 'INSERT') THEN
        -- Check if session already exists
        IF EXISTS (SELECT 1 FROM chat_sessions WHERE session_id = NEW.session_id) THEN
            -- Update last active time
            UPDATE chat_sessions 
            SET last_active_time = NEW.created_at 
            WHERE session_id = NEW.session_id;
        ELSE
            -- Create new session
            -- Use the first message content as the initial title (if it's from user)
            INSERT INTO chat_sessions (session_id, title, last_active_time, created_at)
            VALUES (
                NEW.session_id, 
                CASE WHEN NEW.role = 'user' THEN LEFT(NEW.content, 100) ELSE 'New Conversation' END,
                NEW.created_at,
                NEW.created_at
            );
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3. Create trigger on chat_messages
DROP TRIGGER IF EXISTS trg_sync_chat_session ON chat_messages;
CREATE TRIGGER trg_sync_chat_session
AFTER INSERT ON chat_messages
FOR EACH ROW
EXECUTE FUNCTION sync_chat_session();


-- V1.2 optimized: Removed expensive initial data migration to prevent connection timeout.
-- Future session data will be handled by the trigger defined above.

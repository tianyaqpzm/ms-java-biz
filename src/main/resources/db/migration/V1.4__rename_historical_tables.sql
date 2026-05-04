-- V1.4__rename_historical_tables.sql

-- 1. Rename historical tables to follow ms_ prefix and singular naming convention
ALTER TABLE chat_messages RENAME TO ms_chat_message;
ALTER TABLE chat_sessions RENAME TO ms_chat_session;
ALTER TABLE knowledge_topic RENAME TO ms_knowledge_topic;
ALTER TABLE knowledge_document RENAME TO ms_knowledge_document;

-- 2. Add comments for consistency
COMMENT ON TABLE ms_chat_message IS '聊天消息历史表';
COMMENT ON TABLE ms_chat_session IS '聊天会话列表表';
COMMENT ON TABLE ms_knowledge_topic IS '知识库主题表';
COMMENT ON TABLE ms_knowledge_document IS '知识库文档映射表';

-- 3. Update the session sync function with new table names
CREATE OR REPLACE FUNCTION sync_chat_session()
RETURNS TRIGGER AS $$
BEGIN
    -- If it's a new message
    IF (TG_OP = 'INSERT') THEN
        -- Check if session already exists
        IF EXISTS (SELECT 1 FROM ms_chat_session WHERE session_id = NEW.session_id) THEN
            -- Update last active time
            UPDATE ms_chat_session 
            SET last_active_time = NEW.created_at 
            WHERE session_id = NEW.session_id;
        ELSE
            -- Create new session
            -- Use the first message content as the initial title (if it's from user)
            INSERT INTO ms_chat_session (session_id, title, last_active_time, created_at)
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

-- 4. Re-bind trigger (optional but ensures clean mapping)
DROP TRIGGER IF EXISTS trg_sync_chat_session ON ms_chat_message;
CREATE TRIGGER trg_sync_chat_session
AFTER INSERT ON ms_chat_message
FOR EACH ROW
EXECUTE FUNCTION sync_chat_session();

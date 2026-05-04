package com.dark.aiagent.domain.chat.repository;

import com.dark.aiagent.domain.chat.entity.ChatMessage;
import com.dark.aiagent.domain.chat.entity.ChatSession;
import java.util.List;

public interface ChatRepository {
    
    /**
     * 根据 SessionID 查询聊天记录 (按时间正序)
     */
    List<ChatMessage> findMessagesBySessionId(String sessionId);

    /**
     * 保存单条聊天记录
     */
    void saveMessage(ChatMessage message);

    /**
     * 获取所有会话列表 (包含推导出的 Title 和 最后活跃时间)
     */
    List<ChatSession> findAllSessions();

    /**
     * 删除指定会话及其所有消息
     */
    void deleteSession(String sessionId);
}

package com.dark.aiagent.infrastructure.persistence.chat.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 聊天会话持久化对象 (Data Object)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ms_chat_session")
public class ChatSessionDO {
    @TableId
    private String sessionId;
    private String title;
    private LocalDateTime lastActiveTime;
    private LocalDateTime createdAt;
}

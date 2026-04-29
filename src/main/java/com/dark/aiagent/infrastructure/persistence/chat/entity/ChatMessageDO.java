package com.dark.aiagent.infrastructure.persistence.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 聊天消息数据持久化对象 (Data Object)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("chat_messages")
public class ChatMessageDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    private String role; // "user" or "ai"
    private String content;
    private LocalDateTime createdAt;
}

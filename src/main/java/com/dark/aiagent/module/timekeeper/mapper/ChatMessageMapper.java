package com.dark.aiagent.module.timekeeper.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dark.aiagent.module.timekeeper.entity.ChatMessage;
import com.dark.aiagent.module.timekeeper.entity.ChatSessionDto;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("""
            SELECT session_id as sessionId,
                   (
                       SELECT content
                       FROM chat_messages cm2
                       WHERE cm2.session_id = cm1.session_id AND role = 'user'
                       ORDER BY created_at ASC
                       LIMIT 1
                   ) AS title,
                   MAX(created_at) AS lastActiveTime
            FROM chat_messages cm1
            GROUP BY session_id
            ORDER BY MAX(created_at) DESC
            """)
    List<ChatSessionDto> getSessions();
}

package com.dark.aiagent.infrastructure.persistence.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dark.aiagent.infrastructure.persistence.chat.entity.ChatSessionDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionDO> {
}

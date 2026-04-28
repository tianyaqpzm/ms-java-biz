package com.dark.aiagent.infrastructure.persistence.chat.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dark.aiagent.infrastructure.persistence.chat.entity.ChatMessageDO;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessageDO> {
}

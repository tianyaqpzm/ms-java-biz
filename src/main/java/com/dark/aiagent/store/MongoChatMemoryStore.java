package com.dark.aiagent.store;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.dark.aiagent.infrastructure.persistence.chat.entity.MongoChatMessageDO;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

/**
 * @desc
 * 
 * @date 2025/5/7 23:58
 */
@Component
public class MongoChatMemoryStore implements ChatMemoryStore {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        MongoChatMessageDO mongoChatMessageDO = mongoTemplate.findOne(query, MongoChatMessageDO.class);
        if (mongoChatMessageDO == null) {
            return new LinkedList<>();
        }
        return ChatMessageDeserializer.messagesFromJson(mongoChatMessageDO.getContent());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("content", ChatMessageSerializer.messagesToJson(messages));
        // 根据query条件能查询出文档，则修改文档；否则新增文档
        mongoTemplate.upsert(query, update, MongoChatMessageDO.class);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        mongoTemplate.remove(query, MongoChatMessageDO.class);

    }
}

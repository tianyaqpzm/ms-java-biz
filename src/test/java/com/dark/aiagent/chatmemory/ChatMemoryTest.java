package com.dark.aiagent.chatmemory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.dark.aiagent.assistant.Assistant;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;

/**
 * @desc
 * 
 * @date 2025/5/5 23:12
 */
@SpringBootTest(properties = {"spring.profiles.active=test",
        "langchain4j.community.dashscope.chat-model.enabled=false", "DASH_SCOPE_API_KEY=dummy"})
@org.springframework.test.context.ActiveProfiles("test")
public class ChatMemoryTest {
    @MockBean
    OllamaChatModel ollamaChatModel;

    @BeforeEach
    void setUp() {
        ChatResponse mockResponse =
                ChatResponse.builder().aiMessage(AiMessage.from("这是模拟的回复")).build();
        when(ollamaChatModel.chat(any(ChatRequest.class))).thenReturn(mockResponse);
        when(ollamaChatModel.chat(any(List.class))).thenReturn(mockResponse);
        when(ollamaChatModel.chat(any(UserMessage.class))).thenReturn(mockResponse);
    }

    @Test
    public void test1() {
        UserMessage userMessage1 = UserMessage.userMessage("我是嬛嬛");
        ChatResponse chatResponse1 = ollamaChatModel.chat(userMessage1);
        AiMessage aiMessage1 = chatResponse1.aiMessage();
        System.out.println(aiMessage1.text());

        UserMessage userMessage2 = UserMessage.userMessage("你知道我是谁吗");
        ChatResponse chatResponse2 =
                ollamaChatModel.chat(Arrays.asList(userMessage1, aiMessage1, userMessage2));
        AiMessage aiMessage2 = chatResponse2.aiMessage();
        System.out.println(aiMessage2.text());

    }

    @Test
    public void test2() {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        Assistant assistant = AiServices.builder(Assistant.class).chatLanguageModel(ollamaChatModel)
                .chatMemory(chatMemory).build();

        String ans1 = assistant.chat("我是嬛嬛");
        System.out.println(ans1);

        String ans2 = assistant.chat("我是谁");
        System.out.println(ans2);
    }
}

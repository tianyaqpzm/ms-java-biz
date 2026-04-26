package com.dark.aiagent.assistant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;

/**
 * @desc
 * 
 * @date 2025/5/5 20:51
 */
@SpringBootTest(properties = {"spring.profiles.active=test",
        "langchain4j.community.dashscope.chat-model.enabled=false", "DASH_SCOPE_API_KEY=dummy"})
@org.springframework.test.context.ActiveProfiles("test")
public class AIServiceTest {

    @MockBean
    private OllamaChatModel ollamaChatModel;

    @Test
    public void testChat() {
        ChatResponse mockResponse =
                ChatResponse.builder().aiMessage(AiMessage.from("这是模拟的回复")).build();
        when(ollamaChatModel.chat(any(ChatRequest.class))).thenReturn(mockResponse);

        Assistant assistant = AiServices.create(Assistant.class, ollamaChatModel);
        String ans = assistant.chat("你是谁");
        System.out.println(ans);
    }
}

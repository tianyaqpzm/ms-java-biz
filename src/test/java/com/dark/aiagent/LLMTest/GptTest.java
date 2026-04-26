package com.dark.aiagent.LLMTest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * @desc
 * @date 2025/4/24 22:21
 */
@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
public class GptTest {

    @Test
    public void testDemo() {
        OpenAiChatModel model =
                OpenAiChatModel.builder().baseUrl("http://langchain4j.dev/demo/openai/v1")
                        .apiKey("demo").modelName("gpt-4o-mini").build();

        String res = model.chat("你好，你是谁？");

        System.out.println(res);
    }
}

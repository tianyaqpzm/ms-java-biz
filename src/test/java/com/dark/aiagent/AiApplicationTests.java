package com.dark.aiagent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import com.dark.aiagent.module.timekeeper.repository.TimeLimitedEventRepository;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
class AiApplicationTests {

    @MockBean
    private TimeLimitedEventRepository timeLimitedEventRepository;

    @MockBean
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setup() {
        // 满足 MappingContext 初始化的必要 Mock
        when(mongoTemplate.getConverter()).thenReturn(mock(MongoConverter.class));
    }

    @Test
    void contextLoads() {}

}

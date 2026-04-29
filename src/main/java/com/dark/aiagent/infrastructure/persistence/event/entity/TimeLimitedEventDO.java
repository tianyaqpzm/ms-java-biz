package com.dark.aiagent.infrastructure.persistence.event.entity;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 限时事件数据持久化对象 (Data Object)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "time_limit_events")
public class TimeLimitedEventDO {
    @Id
    private String id;
    private String title;
    private String category;
    private Date date;
    private String time;
    private String description;
    private Boolean repeatYearly;
    private AppearanceDO appearance;
    private Date createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppearanceDO {
        private String type; // "image" or "color"
        private String value; // image path or hex code
    }
}

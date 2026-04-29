package com.dark.aiagent.application.event.dto;

import java.util.Date;
import lombok.Data;

@Data
public class EventDto {
    private String id;
    private String title;
    private String category;
    private Date date;
    private String time;
    private String description;
    private Boolean repeatYearly;
    private AppearanceDto appearance;
    private Date createdAt;

    @Data
    public static class AppearanceDto {
        private String type;
        private String value;
    }
}

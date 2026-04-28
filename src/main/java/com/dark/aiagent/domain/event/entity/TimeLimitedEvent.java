package com.dark.aiagent.domain.event.entity;

import com.dark.aiagent.domain.event.valueobject.EventAppearance;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * 限时事件领域实体 (充血模型)
 */
public class TimeLimitedEvent {

    private final String id;
    private String title;
    private String category;
    private Date date;
    private String time;
    private String description;
    private Boolean repeatYearly;
    private EventAppearance appearance;
    private Date createdAt;

    // 供从持久层恢复的私有构造
    private TimeLimitedEvent(String id, String title, String category, Date date, String time, String description, Boolean repeatYearly, EventAppearance appearance, Date createdAt) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.date = date;
        this.time = time;
        this.description = description;
        this.repeatYearly = repeatYearly;
        this.appearance = appearance;
        this.createdAt = createdAt;
    }

    public static TimeLimitedEvent create(String title, String category, Date date, String time, String description, Boolean repeatYearly, EventAppearance appearance) {
        return new TimeLimitedEvent(UUID.randomUUID().toString().replace("-", ""), title, category, date, time, description, repeatYearly, appearance, new Date());
    }

    public static TimeLimitedEvent restore(String id, String title, String category, Date date, String time, String description, Boolean repeatYearly, EventAppearance appearance, Date createdAt) {
        return new TimeLimitedEvent(id, title, category, date, time, description, repeatYearly, appearance, createdAt);
    }

    public void update(String title, String category, Date date, String time, String description, Boolean repeatYearly, EventAppearance appearance) {
        this.title = title;
        this.category = category;
        this.date = date;
        this.time = time;
        this.description = description;
        this.repeatYearly = repeatYearly;
        this.appearance = appearance;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public Date getDate() { return date; }
    public String getTime() { return time; }
    public String getDescription() { return description; }
    public Boolean getRepeatYearly() { return repeatYearly; }
    public EventAppearance getAppearance() { return appearance; }
    public Date getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeLimitedEvent that = (TimeLimitedEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

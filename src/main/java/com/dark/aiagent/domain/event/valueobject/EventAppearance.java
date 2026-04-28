package com.dark.aiagent.domain.event.valueobject;

import java.util.Objects;

/**
 * 事件外观值对象 (不可变)
 */
public record EventAppearance(String type, String value) {
    public EventAppearance {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Appearance type cannot be empty");
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Appearance value cannot be empty");
        }
    }
}

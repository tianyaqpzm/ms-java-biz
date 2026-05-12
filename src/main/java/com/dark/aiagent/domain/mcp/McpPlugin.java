package com.dark.aiagent.domain.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class McpPlugin {
    private final UUID id;
    private final String name;
    private final String title;
    private final String description;
    private final String icon;
    private final String type;
    private final JsonNode config;
    private boolean enabled;
    private final boolean system;
    private final OffsetDateTime createTime;
    private final OffsetDateTime updateTime;

    private McpPlugin() {
        this.id = null;
        this.name = null;
        this.title = null;
        this.description = null;
        this.icon = null;
        this.type = null;
        this.config = null;
        this.enabled = false;
        this.system = false;
        this.createTime = null;
        this.updateTime = null;
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        McpPlugin that = (McpPlugin) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

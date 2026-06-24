package com.promptjuggler;

import java.util.Map;
import java.util.UUID;

/** Optional parameters for {@link PromptJuggler#runPrompt} and {@link PromptJuggler#runWorkflow}. */
public final class RunOptions {

    final String priority;
    final UUID thread;
    final String environment;
    final Map<String, String> envVars;
    final Map<String, Object> metadata;
    final String channel;

    private RunOptions(Builder builder) {
        this.priority = builder.priority;
        this.thread = builder.thread;
        this.environment = builder.environment;
        this.envVars = builder.envVars;
        this.metadata = builder.metadata;
        this.channel = builder.channel;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String priority;
        private UUID thread;
        private String environment;
        private Map<String, String> envVars;
        private Map<String, Object> metadata;
        private String channel;

        /** Processing priority: {@code onsite}, {@code normal}, or {@code low}. */
        public Builder priority(String priority) {
            this.priority = priority;
            return this;
        }

        public Builder thread(UUID thread) {
            this.thread = thread;
            return this;
        }

        public Builder thread(String thread) {
            this.thread = UUID.fromString(thread);
            return this;
        }

        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder envVars(Map<String, String> envVars) {
            this.envVars = envVars;
            return this;
        }

        /** Metadata values are {@code String} or {@code List<String>}. */
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        /** Memory channel (prompt runs only; ignored by {@code runWorkflow}). */
        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }

        public RunOptions build() {
            return new RunOptions(this);
        }
    }
}

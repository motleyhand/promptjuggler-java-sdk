package com.promptjuggler;

import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Optional parameters for {@link PromptJuggler#runPrompt} and {@link PromptJuggler#runWorkflow}.
 * Every field is null when the caller leaves it unset. Passing {@code null} to any setter is
 * equivalent to leaving that field unset, so callers can forward a nullable value without guarding.
 */
public final class RunOptions {

  final @Nullable String priority;
  final @Nullable UUID thread;
  final @Nullable String environment;
  final @Nullable Map<String, String> envVars;
  final @Nullable Map<String, Object> metadata;
  final @Nullable String channel;

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

    private @Nullable String priority;
    private @Nullable UUID thread;
    private @Nullable String environment;
    private @Nullable Map<String, String> envVars;
    private @Nullable Map<String, Object> metadata;
    private @Nullable String channel;

    /** Processing priority: {@code onsite}, {@code normal}, or {@code low}. */
    public Builder priority(@Nullable String priority) {
      this.priority = priority;
      return this;
    }

    public Builder thread(@Nullable UUID thread) {
      this.thread = thread;
      return this;
    }

    public Builder thread(@Nullable String thread) {
      this.thread = thread != null ? UUID.fromString(thread) : null;
      return this;
    }

    public Builder environment(@Nullable String environment) {
      this.environment = environment;
      return this;
    }

    public Builder envVars(@Nullable Map<String, String> envVars) {
      this.envVars = envVars;
      return this;
    }

    /** Metadata values are {@code String} or {@code List<String>}. */
    public Builder metadata(@Nullable Map<String, Object> metadata) {
      this.metadata = metadata;
      return this;
    }

    /** Memory channel (prompt runs only; ignored by {@code runWorkflow}). */
    public Builder channel(@Nullable String channel) {
      this.channel = channel;
      return this;
    }

    public RunOptions build() {
      return new RunOptions(this);
    }
  }
}

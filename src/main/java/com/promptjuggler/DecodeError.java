package com.promptjuggler;

import org.jspecify.annotations.Nullable;

/**
 * Thrown when the API replied with a success status but the body didn't decode into the expected
 * model. The request succeeded, so retrying a run starts a second one.
 */
public final class DecodeError extends PromptJugglerException {

  DecodeError(@Nullable String message, Throwable cause) {
    super(message, cause);
  }
}

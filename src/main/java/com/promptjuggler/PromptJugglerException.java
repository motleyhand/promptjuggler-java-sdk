package com.promptjuggler;

import org.jspecify.annotations.Nullable;

/** Base class for every error the SDK raises. Catch this to handle any SDK failure. */
public class PromptJugglerException extends RuntimeException {

  PromptJugglerException(@Nullable String message, Throwable cause) {
    super(message, cause);
  }
}

package com.promptjuggler;

import org.jspecify.annotations.Nullable;

/**
 * Base class for every error the SDK raises. Checked, so every call that reaches the API forces the
 * caller to handle (or declare) it. Catch this to handle any SDK failure at once.
 */
public class PromptJugglerException extends Exception {

  PromptJugglerException(@Nullable String message, Throwable cause) {
    super(message, cause);
  }
}

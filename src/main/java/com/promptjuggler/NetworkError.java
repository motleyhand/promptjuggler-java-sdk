package com.promptjuggler;

import org.jspecify.annotations.Nullable;

/** Thrown when the request never reached the API (DNS failure, timeout, offline). */
public final class NetworkError extends PromptJugglerException {

  NetworkError(@Nullable String message, Throwable cause) {
    super(message, cause);
  }
}

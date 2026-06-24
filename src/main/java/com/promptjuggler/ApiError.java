package com.promptjuggler;

/** Thrown when the API responds with a non-2xx status. */
public final class ApiError extends PromptJugglerException {

  private final int statusCode;

  ApiError(String message, int statusCode, Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
  }

  /** The HTTP status code returned by the API. */
  public int statusCode() {
    return statusCode;
  }
}

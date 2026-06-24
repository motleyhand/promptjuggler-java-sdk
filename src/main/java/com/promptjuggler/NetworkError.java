package com.promptjuggler;

/** Thrown when the request never reached the API (DNS failure, timeout, offline). */
public final class NetworkError extends PromptJugglerException {

    NetworkError(String message, Throwable cause) {
        super(message, cause);
    }
}

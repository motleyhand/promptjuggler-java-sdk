package com.promptjuggler;

/** Base class for every error the SDK raises. Catch this to handle any SDK failure. */
public class PromptJugglerException extends RuntimeException {

    PromptJugglerException(String message, Throwable cause) {
        super(message, cause);
    }
}

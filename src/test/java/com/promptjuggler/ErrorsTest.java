package com.promptjuggler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ErrorsTest {

    private static final String UUID1 = "550e8400-e29b-41d4-a716-446655440000";

    @Test
    void translatesNon2xxIntoApiError() {
        try (MockServer server = new MockServer().respond(404, "{\"error\":\"Prompt run not found\"}")) {
            ApiError error = assertThrows(ApiError.class, () -> server.client().getPromptRun(UUID1));
            assertEquals(404, error.statusCode());
            assertEquals("Prompt run not found", error.getMessage());
        }
    }

    @Test
    void apiErrorIsAPromptJugglerException() {
        try (MockServer server = new MockServer().respond(500, "{\"error\":\"boom\"}")) {
            assertThrows(PromptJugglerException.class, () -> server.client().getPromptRun(UUID1));
        }
    }

    @Test
    void wrapsConnectionFailureInNetworkError() {
        // Nothing is listening on port 1 — the request never gets a response.
        PromptJuggler client = new PromptJuggler("test-key", "http://127.0.0.1:1");
        assertThrows(NetworkError.class, () -> client.getPromptRun(UUID1));
    }
}

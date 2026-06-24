package com.promptjuggler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.promptjuggler.client.ApiClient;
import com.promptjuggler.client.ApiException;
import com.promptjuggler.client.api.KnowledgeBasesApi;
import com.promptjuggler.client.api.PromptRunsApi;
import com.promptjuggler.client.api.PromptsApi;
import com.promptjuggler.client.api.WorkflowRunsApi;
import com.promptjuggler.client.model.CreatePromptRun;
import com.promptjuggler.client.model.CreatePromptRunResponse;
import com.promptjuggler.client.model.CreateWorkflowRun;
import com.promptjuggler.client.model.CreateWorkflowRunResponse;
import com.promptjuggler.client.model.KnowledgeBaseResponse;
import com.promptjuggler.client.model.KnowledgeDocumentResponse;
import com.promptjuggler.client.model.PromptRevision;
import com.promptjuggler.client.model.PromptRun;
import com.promptjuggler.client.model.WorkflowRun;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;

/**
 * Ergonomic entry point to the PromptJuggler API. Wraps the generated client: flat method calls in,
 * generated typed models out, with API errors translated into {@link ApiError}. Synchronous.
 */
public final class PromptJuggler {

    private static final String DEFAULT_BASE_URL = "https://promptjuggler.com";

    private final ApiClient apiClient;
    private final String apiKey;
    private final String baseUrl;
    private final PromptsApi prompts;
    private final PromptRunsApi promptRuns;
    private final WorkflowRunsApi workflowRuns;
    private final KnowledgeBasesApi knowledgeBases;

    public PromptJuggler(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL);
    }

    /** Construct a client pointed at a custom base URL (e.g. for testing). */
    public PromptJuggler(String apiKey, String baseUrl) {
        ApiClient client = new ApiClient();
        client.updateBaseUri(baseUrl);
        client.setRequestInterceptor(builder -> builder.header("Authorization", "Bearer " + apiKey));
        this.apiClient = client;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.prompts = new PromptsApi(client);
        this.promptRuns = new PromptRunsApi(client);
        this.workflowRuns = new WorkflowRunsApi(client);
        this.knowledgeBases = new KnowledgeBasesApi(client);
    }

    /** Fetch a prompt revision by slug and version (a revision number or a tag like {@code production}). */
    public PromptRevision getPrompt(String slug, String version) {
        return send(() -> prompts.getPromptRevision(slug, version));
    }

    public PromptRevision getPrompt(String slug, int version) {
        return getPrompt(slug, String.valueOf(version));
    }

    /** Trigger a prompt run (async — returns the run ID; poll {@link #getPromptRun} for the result). */
    public CreatePromptRunResponse runPrompt(String slug, String version, Map<String, String> inputs) {
        return runPrompt(slug, version, inputs, RunOptions.builder().build());
    }

    public CreatePromptRunResponse runPrompt(String slug, int version, Map<String, String> inputs) {
        return runPrompt(slug, String.valueOf(version), inputs);
    }

    public CreatePromptRunResponse runPrompt(
            String slug, String version, Map<String, String> inputs, RunOptions options) {
        CreatePromptRun body = new CreatePromptRun().inputs(inputs);
        if (options.priority != null) {
            body.priority(CreatePromptRun.PriorityEnum.fromValue(options.priority));
        }
        if (options.thread != null) {
            body.thread(options.thread);
        }
        if (options.environment != null) {
            body.environment(options.environment);
        }
        if (options.envVars != null) {
            body.envVars(options.envVars);
        }
        if (options.metadata != null) {
            body.metadata(options.metadata);
        }
        if (options.channel != null) {
            body.channel(options.channel);
        }
        return send(() -> promptRuns.createPromptRun(slug, version, body));
    }

    public CreatePromptRunResponse runPrompt(
            String slug, int version, Map<String, String> inputs, RunOptions options) {
        return runPrompt(slug, String.valueOf(version), inputs, options);
    }

    /** Fetch a prompt run by ID. */
    public PromptRun getPromptRun(UUID runId) {
        return send(() -> promptRuns.getPromptRun(runId));
    }

    public PromptRun getPromptRun(String runId) {
        return getPromptRun(UUID.fromString(runId));
    }

    /** Trigger a workflow run (async — returns the run ID; poll {@link #getWorkflowRun} for the result). */
    public CreateWorkflowRunResponse runWorkflow(String slug, String version, Map<String, String> inputs) {
        return runWorkflow(slug, version, inputs, RunOptions.builder().build());
    }

    public CreateWorkflowRunResponse runWorkflow(String slug, int version, Map<String, String> inputs) {
        return runWorkflow(slug, String.valueOf(version), inputs);
    }

    public CreateWorkflowRunResponse runWorkflow(
            String slug, String version, Map<String, String> inputs, RunOptions options) {
        CreateWorkflowRun body = new CreateWorkflowRun().inputs(inputs);
        if (options.priority != null) {
            body.priority(CreateWorkflowRun.PriorityEnum.fromValue(options.priority));
        }
        if (options.thread != null) {
            body.thread(options.thread);
        }
        if (options.environment != null) {
            body.environment(options.environment);
        }
        if (options.envVars != null) {
            body.envVars(options.envVars);
        }
        if (options.metadata != null) {
            body.metadata(options.metadata);
        }
        return send(() -> workflowRuns.createWorkflowRun(slug, version, body));
    }

    public CreateWorkflowRunResponse runWorkflow(
            String slug, int version, Map<String, String> inputs, RunOptions options) {
        return runWorkflow(slug, String.valueOf(version), inputs, options);
    }

    /** Fetch a workflow run by ID. */
    public WorkflowRun getWorkflowRun(UUID runId) {
        return send(() -> workflowRuns.getWorkflowRun(runId));
    }

    public WorkflowRun getWorkflowRun(String runId) {
        return getWorkflowRun(UUID.fromString(runId));
    }

    /** Fetch a knowledge base by slug. */
    public KnowledgeBaseResponse getKnowledgeBase(String slug) {
        return send(() -> knowledgeBases.publicGetKnowledgeBase(slug));
    }

    /** Fetch a knowledge document by ID. */
    public KnowledgeDocumentResponse getKnowledgeDocument(UUID documentId) {
        return send(() -> knowledgeBases.publicGetDocument(documentId));
    }

    public KnowledgeDocumentResponse getKnowledgeDocument(String documentId) {
        return getKnowledgeDocument(UUID.fromString(documentId));
    }

    /** Delete a knowledge document by ID. */
    public void deleteKnowledgeDocument(UUID documentId) {
        send(() -> {
            knowledgeBases.publicDeleteDocument(documentId);
            return null;
        });
    }

    public void deleteKnowledgeDocument(String documentId) {
        deleteKnowledgeDocument(UUID.fromString(documentId));
    }

    /** Upload one or more documents to a knowledge base (processed asynchronously). */
    public List<KnowledgeDocumentResponse> uploadDocuments(String slug, List<File> files) {
        // The generated client names every multipart part "files"; the server needs them
        // bracket-indexed (files[i]) to parse them as an array. Build the body and send it
        // through the client's HttpClient, reusing its base URI + bearer auth.
        MultipartEntityBuilder multipart = MultipartEntityBuilder.create();
        for (int i = 0; i < files.size(); i++) {
            multipart.addBinaryBody("files[" + i + "]", files.get(i));
        }
        return send(() -> {
            HttpEntity entity = multipart.build();
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            entity.writeTo(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/v1/knowledge-bases/" + slug + "/documents"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", entity.getContentType().getValue())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                    .build();
            HttpResponse<String> response =
                    apiClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new ApiException(response.statusCode(), null, response.body());
            }
            return apiClient
                    .getObjectMapper()
                    .readValue(response.body(), new TypeReference<List<KnowledgeDocumentResponse>>() {});
        });
    }

    private <T> T send(Callable<T> call) {
        try {
            return call.call();
        } catch (ApiException e) {
            throw translate(e);
        } catch (IOException e) {
            throw new NetworkError(e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NetworkError(e.getMessage(), e);
        } catch (Exception e) {
            throw new PromptJugglerException(e.getMessage(), e);
        }
    }

    private PromptJugglerException translate(ApiException e) {
        // A response with no status code means the request never landed (DNS, timeout, ...).
        if (e.getCode() == 0) {
            return new NetworkError(e.getMessage(), e);
        }
        String message = extractMessage(e.getResponseBody());
        return new ApiError(message != null ? message : e.getMessage(), e.getCode(), e);
    }

    private String extractMessage(String body) {
        if (body == null || body.isEmpty()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.JsonNode node = apiClient.getObjectMapper().readTree(body);
            com.fasterxml.jackson.databind.JsonNode error = node.get("error");
            return error != null && error.isTextual() ? error.asText() : null;
        } catch (IOException e) {
            return null;
        }
    }
}

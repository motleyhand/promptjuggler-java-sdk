# PromptJuggler Java SDK

The official Java client for the [PromptJuggler](https://promptjuggler.com) API. Run prompts
and workflows, manage knowledge bases, and verify webhooks — with typed model objects and
flat, synchronous methods, over the JDK's built-in HTTP client (no third-party HTTP stack).

## Requirements

- Java 17+

## Installation

Gradle:

```kotlin
implementation("com.promptjuggler:promptjuggler-java:1.0.0")
```

Maven:

```xml
<dependency>
  <groupId>com.promptjuggler</groupId>
  <artifactId>promptjuggler-java</artifactId>
  <version>1.0.0</version>
</dependency>
```

Use the latest version from [Maven Central](https://central.sonatype.com/artifact/com.promptjuggler/promptjuggler-java).

## Usage

```java
import com.promptjuggler.PromptJuggler;
import com.promptjuggler.client.model.CreatePromptRunResponse;
import com.promptjuggler.client.model.PromptRun;
import com.promptjuggler.client.model.RunStatus;
import java.util.Map;

PromptJuggler pj = new PromptJuggler("your-api-key");

// Trigger a run (async — returns the run ID)
CreatePromptRunResponse created = pj.runPrompt("greeting", "production", Map.of("name", "Ada"));

// Poll for the result
PromptRun run = pj.getPromptRun(created.getId());
if (run.getStatus() == RunStatus.COMPLETED) {
    System.out.println(run.getOutput());
}
```

Errors surface as `ApiError` (with a `statusCode()`); both it and `NetworkError` extend the
checked `PromptJugglerException`, which every API call declares `throws`. Verify incoming
webhooks with `Webhooks.verifySignature()`.

## Documentation

Full guides and the API reference: **https://docs.promptjuggler.com/sdks/java/overview**

## License

MIT

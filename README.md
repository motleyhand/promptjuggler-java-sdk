# PromptJuggler Java SDK

The official Java client for the [PromptJuggler](https://promptjuggler.com) API. Run prompts
and workflows, manage knowledge bases, and verify webhooks — with typed model objects and
flat, synchronous methods, over the JDK's built-in HTTP client (no third-party HTTP stack).

## Requirements

- Java 17+

## Installation

Replace `VERSION` with the latest release: [![Maven Central](https://img.shields.io/maven-central/v/com.promptjuggler/promptjuggler-java)](https://central.sonatype.com/artifact/com.promptjuggler/promptjuggler-java)

Gradle:

```kotlin
implementation("com.promptjuggler:promptjuggler-java:VERSION")
```

Maven:

```xml
<dependency>
  <groupId>com.promptjuggler</groupId>
  <artifactId>promptjuggler-java</artifactId>
  <version>VERSION</version>
</dependency>
```

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

Errors surface as `ApiError` (with a `statusCode()`), `NetworkError` (no response), or
`DecodeError` (an undecodable success body); all extend the checked `PromptJugglerException`,
which every API call declares `throws`. Verify incoming webhooks with `Webhooks.verifySignature()`.

## Documentation

Full guides and the API reference: **https://docs.promptjuggler.com/sdks/java/overview**

## License

MIT

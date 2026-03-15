# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Maintaining This File

Update CLAUDE.md proactively whenever the project state, architecture decisions, or guidance changes — without waiting to be asked.

## Project Goal

A Minecraft mod that lets users interact with an LLM via in-game chat, supporting multiple providers (OpenAI, Anthropic, Google Gemini, DeepSeek, Mistral, Ollama).

## Components

1. **In-game chat command** — `/tris <message>` sends messages to the configured LLM and prints responses in chat
2. **Tool calling** — the LLM can invoke `executeCommand` (run Minecraft commands) and `getScreenshot` (capture a screenshot); results are fed back to the model in a loop

### Architecture
- Fabric mod for Minecraft 1.21.11, client-side only
- Package: `com.swalrus.tris`; main entrypoint `Tris`, client entrypoint `TrisClient`
- **LLM abstraction**: `LlmService` uses [langchain4j](https://github.com/langchain4j/langchain4j) (`1.0.0-beta1`) with `ChatLanguageModel` — provider is selected at runtime based on `TrisConfig.provider`
- **Provider factory**: `LlmService.createModel(TrisConfig)` returns the right `ChatLanguageModel` implementation; DeepSeek reuses `OpenAiChatModel` with a custom `baseUrl`
- **Config**: `TrisConfig` has three fields: `provider` (enum dropdown), `model` (string), `apiKey` (string). For Ollama, `apiKey` holds the base URL (defaults to `http://localhost:11434`)
- API calls run on a virtual thread; responses are dispatched back to the game thread via `Minecraft.execute()`
- Config managed by Cloth Config AutoConfig, accessible via ModMenu → Tris → Config

### Conversation history
`TrisCommand` holds a `List<ChatMessage>` history (langchain4j types). The model is cached; history is cleared automatically when the provider/model/key config changes.

### Threading
Minecraft runs on a single game thread. All background work (API calls, tool handlers) must be dispatched back to the game thread via `client.execute()`.

### Tool calling flow
`LlmService.chat()` runs a loop:
1. Send `ChatRequest` with tool specs to the model
2. If `AiMessage.hasToolExecutionRequests()` → execute each tool via `ToolExecutor`, append `ToolExecutionResultMessage`(s)
3. For `getScreenshot`, also append a `UserMessage` with `ImageContent` so vision-capable models can see the image
4. Repeat until a plain text response is returned

## Build

```sh
./gradlew build
```

Requires Java 21+. **Always run after code changes.**

## Key Dependencies

- Fabric Loader 0.18.4, Fabric API 0.141.3+1.21.11
- Cloth Config `21.11.153` (`me.shedaniel.cloth:cloth-config-fabric`)
- ModMenu `17.0.0-beta.2` (`com.terraformersmc:modmenu`, `modCompileOnly`)
- langchain4j `1.0.0-beta1`: `langchain4j-open-ai`, `langchain4j-anthropic`, `langchain4j-google-ai-gemini`, `langchain4j-mistral-ai`, `langchain4j-ollama` (all bundled via Fabric JiJ `include`)

## Notes

- Mojang mappings are used (not Yarn) — use `net.minecraft.client.Minecraft`, `net.minecraft.network.chat.Component`, `player.displayClientMessage(component, false)`, etc.
- `ImageContent.from(base64, mimeType)` is the correct langchain4j 1.0.0-beta1 API for base64 images (not `fromBase64`)
- `ChatRequest` tool specs go through `.parameters(DefaultChatRequestParameters.builder().toolSpecifications(...).build())` to avoid the deprecated `.toolSpecifications()` shorthand on the builder

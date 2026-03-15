# Project State

## Stage 1: Project Structure
- [x] Choose mod loader (Fabric)
- [x] Initialize mod project with Gradle and Fabric template
- [x] Add Anthropic Java SDK as a dependency
- [x] Confirm the mod loads in-game

## Stage 2: In-Game Chat Command ✓
- [x] Add an in-game config screen with an API token field (Cloth Config + ModMenu)
- [x] Register a `/tris <message>` chat command
- [x] On command, send the message to DeepSeek via the DeepSeek API using the configured token
- [x] Print DeepSeek's response back into in-game chat
- [x] Handle API errors gracefully in chat (e.g. missing token, invalid key)
- [x] Maintain conversation history across messages

## Stage 3: Tool Calls
Use DeepSeek tool calls (function calling API) so the LLM can interact with the game directly during a conversation.

- [x] Define tools (`executeCommand`, `getScreenshot`) as function schemas in the DeepSeek API request
- [x] Handle `tool_calls` responses from DeepSeek — execute the requested tool in-game on the game thread
- [x] Send tool results back to DeepSeek and continue the conversation
- [x] `executeCommand` — runs an in-game command on the game thread and returns the output
- [x] `getScreenshot` — captures the current game frame and returns it as a base64 image

## Stage 4: Polish
- [ ] Package and test the mod in the target instance (Fabulously Optimized 12.0.6)

# Vibe-build with Tris

Tris is a Minecraft client mod that lets you chat with an LLM directly from the game. The model can run commands on your behalf, so you can ask it to build, spawn mobs, heal you, etc.

If you want an improvement for this mod, please submit an issue. Contributions are also appreciated.

### Demo

[![Youtube Video](https://github.com/swalrus1/tris-mc/blob/main/assets/video_thumbnail.png?raw=true)](https://youtu.be/lk8c0aLSCg0)

## How to use

### Setup

Before sending your first message, open **Mod Menu → Tris → Config** and set:

- **Provider** — choose from OpenAI, Anthropic, Google Gemini, DeepSeek, Mistral, or Ollama
- **Model** — the model name, e.g. `gpt-4o`, `claude-sonnet-4-5`, `gemini-2.0-flash`
- **API Key** — your API key for the chosen provider (for Ollama, leave blank or enter a custom base URL)

### Basic chat

```
/tris <message>
```

Type anything after `/tris` and the response appears in chat.

```
/tris what should I build next?
/tris how do I find diamonds faster?
/tris translate "buenos días" to English
```

### Giving the model tools

The model can use two built-in tools without any extra syntax — just ask naturally:

**Run a command**
```
/tris give me 64 diamonds
/tris set the time to noon
/tris teleport me to 0 64 0
```
The model will call `executeCommand` and show you the result.

**Take a screenshot**
```
/tris what do you see?
/tris describe my current surroundings
/tris is there anything dangerous nearby?
```
The model will capture your current view and analyze it.

**Combining both**
```
/tris look at my inventory and give me whatever I'm missing to craft a beacon
/tris check what biome I'm in and suggest what to build here
```

### Conversation context

The model remembers the conversation for the duration of your current world session. When you leave the world, the context is cleared, so each new session starts fresh.

### Configuration

Open **Mod Menu → Tris → Config** to set:

| Field | Description |
|---|---|
| Provider | One of: OpenAI, Anthropic, Google Gemini, DeepSeek, Mistral, Ollama |
| Model | Model name, e.g. `gpt-4o`, `claude-sonnet-4-5`, `gemini-2.0-flash` |
| API Key | Your API key. For Ollama, leave blank for `http://localhost:11434` or enter a custom base URL. |
| System Prompt | Optional extra instructions prepended to every session. |

## Installation

> **Download:** TODO — link to Modrinth / CurseForge

**Requirements**
- Minecraft 1.21.11
- [Fabric Loader](https://fabricmc.net/use/installer/) 0.18.4+
- [Fabric API](https://modrinth.com/mod/fabric-api) 0.141.3+

**Steps**
1. Install Fabric Loader for Minecraft 1.21.11.
2. Download the Tris `.jar` from Modrinth or CurseForge (TODO).
3. Drop the jar into your `.minecraft/mods` folder.
4. Launch the game, open Mod Menu → Tris → Config, and enter your API key.

Tris bundles all LLM provider libraries — no extra downloads required.

## Building from source

**Prerequisites:** Java 21+

```sh
git clone https://github.com/swalrus1/tris-mc.git
cd tris-mc
./gradlew build
```

The compiled jar is placed in `build/libs/`.

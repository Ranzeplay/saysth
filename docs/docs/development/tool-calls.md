---
sidebar_position: 3
---

# Tool calls

The villager conversation API supports OpenAI-compatible function/tool calls. Tools
are sent with the request, and the mod executes requested tools before asking the
model for its final response.

## Built-in villager tools

Every villager conversation includes these tools:

| Tool | Purpose |
| --- | --- |
| `get_villager_profession` | Returns the villager's current profession |
| `get_villager_world_time` | Returns formatted world time and world ticks |
| `get_villager_happiness` | Returns the villager's happiness value |
| `get_villager_reputation` | Returns the villager's reputation with the player |
| `get_villager_experience` | Returns villager trading experience |
| `get_villager_age` | Returns the villager's age |
| `get_villager_trades` | Returns the villager's current trades |

These values are no longer copied into every system prompt. The model should call
the relevant tool when it needs current game state.

## Defining a tool

Implement `Tool` and provide a stable name, a description, a JSON Schema object for parameters,
and an execution method:

```java
public final class GetWeatherTool implements Tool {
    @Override
    public String getName() {
        return "get_weather";
    }

    @Override
    public String getDescription() {
        return "Get the current weather near the villager.";
    }

    @Override
    public JsonObject getParameters() {
        var schema = new JsonObject();
        schema.addProperty("type", "object");
        schema.add("properties", new JsonObject());
        schema.add("required", new JsonArray());
        return schema;
    }

    @Override
    public String execute(JsonObject arguments) {
        if (arguments == null) {
            throw new IllegalArgumentException("Tool arguments cannot be null");
        }
        return "clear";
    }
}
```

Tool results are strings. Throw an `IllegalArgumentException` for invalid
arguments instead of returning a misleading success value.

## Registering a tool

Tools are registered per villager request in `VillagerManager.createVillagerTools`.
Add a tool to that list when it needs access to the current villager or player:

```java
tools.add(new GetWeatherTool(villager, player));
```

The manager permits up to five tool-call rounds per request. Unknown tool names
are returned to the model as an error result, and the tool-call conversation
messages are retained only for the current request.

## Adding prompt context

Use `VillagerSystemPromptHook` for context that should be injected into the temporary system prompt rather than
queried by the model:

```java
Main.VILLAGER_MANAGER.addSystemPromptHook((villager, player, memory) ->
        Optional.of("The player is currently inside the village."));
```

Return `Optional.empty()` when there is no context to add. Blank values are
ignored. Prompt hooks should not be used for rapidly changing values that the
model can retrieve through a tool.

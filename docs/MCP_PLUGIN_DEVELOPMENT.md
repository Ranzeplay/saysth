# MCP Server Plugin Development Guide

This guide explains how to create MCP (Model Context Protocol) server plugins for the SaySomething mod.

## Table of Contents

1. [Overview](#overview)
2. [Getting Started](#getting-started)
3. [Creating Your First Plugin](#creating-your-first-plugin)
4. [Plugin Capabilities](#plugin-capabilities)
5. [Best Practices](#best-practices)
6. [Advanced Topics](#advanced-topics)
7. [Examples](#examples)

## Overview

MCP server plugins extend the capabilities of villagers by providing:

- **Tools**: Functions that the AI can call to perform actions or retrieve data
- **Resources**: Data sources like documentation or knowledge bases
- **Prompts**: Pre-defined conversation templates

## Getting Started

### Prerequisites

- Java 21 or higher
- Basic understanding of the SaySomething mod architecture
- Familiarity with LangChain4j (optional but helpful)

### Adding the Dependency

Add the SaySomething common module to your build.gradle:

```gradle
dependencies {
    implementation 'space.ranzeplay:saysth-common:1.2.0'
}
```

## Creating Your First Plugin

### Step 1: Implement the Interface

Create a class that implements `McpServerPlugin`:

```java
package com.example.plugins;

import dev.langchain4j.agent.tool.ToolSpecification;
import space.ranzeplay.saysth.mcp.McpServerPlugin;

import java.util.List;
import java.util.Map;

public class GreetingPlugin implements McpServerPlugin {
    
    @Override
    public String getName() {
        return "greeting-plugin";
    }
    
    @Override
    public String getDescription() {
        return "Provides customized greetings for players";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public List<ToolSpecification> getTools() {
        return List.of(
            ToolSpecification.builder()
                .name("greet_player")
                .description("Generate a personalized greeting for a player")
                .addParameter("playerName", String.class, "Name of the player to greet")
                .addParameter("timeOfDay", String.class, "Time of day (morning, afternoon, evening)")
                .build()
        );
    }
    
    @Override
    public String executeTool(String toolName, Map<String, Object> arguments) {
        if ("greet_player".equals(toolName)) {
            String playerName = (String) arguments.get("playerName");
            String timeOfDay = (String) arguments.get("timeOfDay");
            
            return String.format("Good %s, %s! Welcome to our village!", 
                    timeOfDay, playerName);
        }
        return null;
    }
}
```

### Step 2: Register the Plugin

Register your plugin during mod initialization:

```java
import space.ranzeplay.saysth.mcp.McpPluginRegistry;

public class MyMod {
    public void onInitialize() {
        McpPluginRegistry registry = McpPluginRegistry.getInstance();
        registry.registerPlugin(new GreetingPlugin());
    }
}
```

### Step 3: Configure SaySomething

Set the API platform to use LangChain4j MCP in `config/saysth/config.json`:

```json
{
    "apiConfigPlatform": "langchain4j-mcp"
}
```

Configure your LLM provider in `config/saysth/api-config.json`:

```json
{
    "apiKey": "your-api-key",
    "modelName": "gpt-4",
    "enableMcpTools": true
}
```

## Plugin Capabilities

### Tools

Tools are functions that the AI can invoke during conversations.

#### Defining Tools

Use `ToolSpecification.builder()` to define tools:

```java
@Override
public List<ToolSpecification> getTools() {
    return List.of(
        ToolSpecification.builder()
            .name("get_weather")
            .description("Get current weather for a location")
            .addParameter("location", String.class, "City name or coordinates")
            .addParameter("units", String.class, "Temperature units (celsius or fahrenheit)")
            .build(),
        
        ToolSpecification.builder()
            .name("get_forecast")
            .description("Get weather forecast for the next 7 days")
            .addParameter("location", String.class, "City name")
            .build()
    );
}
```

#### Executing Tools

Implement the `executeTool` method to handle tool calls:

```java
@Override
public String executeTool(String toolName, Map<String, Object> arguments) {
    switch (toolName) {
        case "get_weather":
            String location = (String) arguments.get("location");
            String units = (String) arguments.getOrDefault("units", "celsius");
            return fetchWeather(location, units);
            
        case "get_forecast":
            String loc = (String) arguments.get("location");
            return fetchForecast(loc);
            
        default:
            return null;
    }
}
```

### Resources

Resources provide data that the AI can reference:

```java
@Override
public List<McpResource> getResources() {
    return List.of(
        McpResource.text(
            "game://rules",
            "Game Rules",
            "Complete game rules and guidelines"
        ),
        McpResource.json(
            "game://config",
            "Configuration",
            "Current game configuration"
        )
    );
}

@Override
public String readResource(String resourceUri) {
    switch (resourceUri) {
        case "game://rules":
            return loadRulesFromFile();
        case "game://config":
            return getCurrentConfig();
        default:
            return null;
    }
}
```

### Prompts

Prompts are pre-defined conversation templates:

```java
@Override
public List<McpPrompt> getPrompts() {
    return List.of(
        McpPrompt.simple(
            "welcome",
            "Welcome Message",
            "A friendly welcome for new players"
        ),
        new McpPrompt(
            "quest-intro",
            "Quest Introduction",
            "Introduce a new quest to the player",
            List.of(
                Map.of("name", "questName", "description", "Name of the quest", "required", true),
                Map.of("name", "difficulty", "description", "Quest difficulty level", "required", false)
            )
        )
    );
}
```

## Best Practices

### 1. Tool Design

- **Atomic Operations**: Each tool should do one thing well
- **Clear Names**: Use descriptive names like `get_player_stats` instead of `tool1`
- **Good Descriptions**: Help the AI understand when to use each tool
- **Validate Input**: Always validate arguments before processing

```java
@Override
public String executeTool(String toolName, Map<String, Object> arguments) {
    if ("get_player_stats".equals(toolName)) {
        // Validate required arguments
        if (!arguments.containsKey("playerName")) {
            throw new IllegalArgumentException("playerName is required");
        }
        
        String playerName = (String) arguments.get("playerName");
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new IllegalArgumentException("playerName cannot be empty");
        }
        
        // Process the request
        return getStats(playerName);
    }
    return null;
}
```

### 2. Error Handling

Always handle errors gracefully:

```java
@Override
public String executeTool(String toolName, Map<String, Object> arguments) {
    try {
        // Tool logic here
        return performOperation();
    } catch (IOException e) {
        return "Error: Unable to access data source";
    } catch (Exception e) {
        return "Error: " + e.getMessage();
    }
}
```

### 3. Logging

Use comprehensive logging for debugging:

```java
import space.ranzeplay.saysth.Main;

@Override
public String executeTool(String toolName, Map<String, Object> arguments) {
    Main.LOGGER.debug("Executing tool '{}' with arguments: {}", toolName, arguments);
    
    try {
        String result = performOperation();
        Main.LOGGER.info("Tool '{}' completed successfully", toolName);
        return result;
    } catch (Exception e) {
        Main.LOGGER.error("Tool '{}' failed: {}", toolName, e.getMessage(), e);
        return "Error: " + e.getMessage();
    }
}
```

### 4. Resource Management

Clean up resources properly:

```java
private DatabaseConnection connection;

@Override
public void onLoad() throws Exception {
    Main.LOGGER.info("Loading database plugin");
    connection = new DatabaseConnection();
    connection.connect();
}

@Override
public void onUnload() {
    Main.LOGGER.info("Unloading database plugin");
    if (connection != null) {
        connection.close();
    }
}
```

## Advanced Topics

### Stateful Plugins

Maintain state across tool calls:

```java
public class StatefulPlugin implements McpServerPlugin {
    private final Map<String, Integer> counters = new ConcurrentHashMap<>();
    
    @Override
    public String executeTool(String toolName, Map<String, Object> arguments) {
        if ("increment_counter".equals(toolName)) {
            String key = (String) arguments.get("key");
            int newValue = counters.compute(key, (k, v) -> v == null ? 1 : v + 1);
            return "Counter '" + key + "' is now " + newValue;
        }
        return null;
    }
}
```

### Async Operations

Handle long-running operations:

```java
import java.util.concurrent.*;

public class AsyncPlugin implements McpServerPlugin {
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    @Override
    public String executeTool(String toolName, Map<String, Object> arguments) {
        if ("heavy_computation".equals(toolName)) {
            try {
                Future<String> future = executor.submit(() -> performComputation());
                return future.get(30, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                return "Error: Operation timed out";
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }
        return null;
    }
    
    @Override
    public void onUnload() {
        executor.shutdown();
    }
}
```

### Multi-Plugin Communication

Plugins can access other plugins via the registry:

```java
@Override
public String executeTool(String toolName, Map<String, Object> arguments) {
    if ("combined_operation".equals(toolName)) {
        McpPluginRegistry registry = McpPluginRegistry.getInstance();
        
        // Use another plugin's tool
        String result1 = registry.executeTool("other-plugin", "get_data", arguments);
        
        // Process the result
        return processData(result1);
    }
    return null;
}
```

## Examples

### Example 1: Time Plugin

```java
public class TimePlugin implements McpServerPlugin {
    @Override
    public String getName() {
        return "time-plugin";
    }
    
    @Override
    public String getDescription() {
        return "Provides current time and date information";
    }
    
    @Override
    public List<ToolSpecification> getTools() {
        return List.of(
            ToolSpecification.builder()
                .name("get_current_time")
                .description("Get the current time")
                .addParameter("timezone", String.class, "Timezone (e.g., UTC, America/New_York)")
                .build()
        );
    }
    
    @Override
    public String executeTool(String toolName, Map<String, Object> arguments) {
        if ("get_current_time".equals(toolName)) {
            String timezone = (String) arguments.getOrDefault("timezone", "UTC");
            ZoneId zone = ZoneId.of(timezone);
            LocalDateTime now = LocalDateTime.now(zone);
            return now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return null;
    }
}
```

### Example 2: Math Plugin

```java
public class MathPlugin implements McpServerPlugin {
    @Override
    public String getName() {
        return "math-plugin";
    }
    
    @Override
    public String getDescription() {
        return "Performs mathematical calculations";
    }
    
    @Override
    public List<ToolSpecification> getTools() {
        return List.of(
            ToolSpecification.builder()
                .name("calculate")
                .description("Evaluate a mathematical expression")
                .addParameter("expression", String.class, "Mathematical expression to evaluate")
                .build()
        );
    }
    
    @Override
    public String executeTool(String toolName, Map<String, Object> arguments) {
        if ("calculate".equals(toolName)) {
            String expression = (String) arguments.get("expression");
            try {
                double result = evaluateExpression(expression);
                return String.valueOf(result);
            } catch (Exception e) {
                return "Error: Invalid expression";
            }
        }
        return null;
    }
    
    private double evaluateExpression(String expr) {
        // Simple expression evaluation (use a library like exp4j in production)
        return Double.parseDouble(expr);
    }
}
```

### Example 3: Data Storage Plugin

```java
public class DataStoragePlugin implements McpServerPlugin {
    private final Map<String, String> storage = new ConcurrentHashMap<>();
    
    @Override
    public String getName() {
        return "storage-plugin";
    }
    
    @Override
    public String getDescription() {
        return "Provides key-value data storage";
    }
    
    @Override
    public List<ToolSpecification> getTools() {
        return List.of(
            ToolSpecification.builder()
                .name("store_data")
                .description("Store a value with a key")
                .addParameter("key", String.class, "Storage key")
                .addParameter("value", String.class, "Value to store")
                .build(),
            
            ToolSpecification.builder()
                .name("retrieve_data")
                .description("Retrieve a stored value by key")
                .addParameter("key", String.class, "Storage key")
                .build()
        );
    }
    
    @Override
    public String executeTool(String toolName, Map<String, Object> arguments) {
        switch (toolName) {
            case "store_data":
                String key = (String) arguments.get("key");
                String value = (String) arguments.get("value");
                storage.put(key, value);
                return "Stored: " + key + " = " + value;
                
            case "retrieve_data":
                String retrieveKey = (String) arguments.get("key");
                String retrievedValue = storage.get(retrieveKey);
                return retrievedValue != null ? retrievedValue : "Key not found: " + retrieveKey;
                
            default:
                return null;
        }
    }
}
```

## Troubleshooting

### Plugin Not Loading

1. Check that the plugin is registered during mod initialization
2. Verify the plugin name is unique
3. Check logs for exception messages during `onLoad()`

### Tools Not Available

1. Ensure `enableMcpTools` is set to `true` in `api-config.json`
2. Verify the plugin is successfully registered
3. Check that tools are properly defined in `getTools()`

### Tool Execution Fails

1. Add logging to your `executeTool()` method
2. Validate all required arguments
3. Check for exceptions in the logs
4. Ensure tool names match exactly (case-sensitive)

## Tool Integration with AI Models

### Current Implementation

The current SDK provides the foundation for MCP tool integration. Tools are registered and available through the `McpPluginRegistry`, but are not automatically invoked by the AI model during conversations.

### Future Implementation

Full tool integration with automatic AI invocation requires implementing LangChain4j's AI Services pattern. This is a more advanced topic that involves:

1. **Creating an AI Service Interface**:
```java
public interface VillagerAI {
    @SystemMessage("You are a helpful villager assistant.")
    String chat(@UserMessage String message);
}
```

2. **Registering Tools with the Service**:
```java
VillagerAI ai = AiServices.builder(VillagerAI.class)
    .chatLanguageModel(model)
    .tools(registry.getAllTools())
    .toolExecutor(new McpToolExecutor(registry))
    .build();
```

3. **Implementing Tool Execution**:
The `McpToolExecutor` would route tool calls to the appropriate plugin through the registry.

### Current Usage

Developers can:
- Register plugins and tools
- Access tools programmatically through `McpPluginRegistry`
- Execute tools manually for custom integrations
- Use the SDK as a foundation for building custom AI service implementations

For production tool integration, consider extending `LangChain4jMCPConfig` with a custom implementation that uses AI Services.

## Additional Resources

- [LangChain4j Documentation](https://docs.langchain4j.dev/)
- [Model Context Protocol Specification](https://modelcontextprotocol.io/)
- [SaySomething Mod Documentation](https://saysth.ranzeplay.space)

## Support

For questions or issues:
- Open an issue on [GitHub](https://github.com/Ranzeplay/saysth/issues)
- Join the discussion in GitHub Discussions

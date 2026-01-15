package space.ranzeplay.saysth.mcp.examples;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.mcp.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Example demonstrating advanced data deserialization features.
 *
 * <p>This plugin shows how to use:</p>
 * <ul>
 *   <li>{@link ToolArgumentsDeserializer} for manual argument extraction</li>
 *   <li>{@link ToolArgumentsSchema} for schema-based automatic deserialization</li>
 *   <li>{@link ToolArgument} annotations for defining data schemas</li>
 * </ul>
 *
 * @author SaySomething Contributors
 * @since 1.2.0
 */
public class ExampleDataPlugin implements McpServerPlugin {

    /**
     * Schema class for user creation request.
     */
    @Getter
    public static class CreateUserRequest {
        // Getters
        @ToolArgument(name = "username", required = true, description = "The username")
        private String username;

        @ToolArgument(name = "email", required = true, description = "User email address")
        private String email;

        @ToolArgument(name = "age", required = false, description = "User age", defaultValue = "18")
        private int age = 18;

        @ToolArgument(name = "isAdmin", required = false, description = "Whether user is admin", defaultValue = "false")
        private boolean isAdmin = false;
    }

    @Override
    public @NotNull String getName() {
        return "example-data-plugin";
    }

    @Override
    public @NotNull String getDescription() {
        return "Demonstrates data deserialization features with schema support";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @NotNull List<ToolSpecification> getTools() {
        return List.of(
                ToolSpecification.builder()
                        .name("create_user")
                        .description("Create a new user with schema-based deserialization")
                        .parameters(JsonObjectSchema.builder()
                                .addStringProperty("username", "The username (required)")
                                .addStringProperty("email", "User email address (required)")
                                .addStringProperty("age", "User age (optional, default: 18)")
                                .addStringProperty("isAdmin", "Whether user is admin (optional, default: false)")
                                .required("username", "email")
                                .build())
                        .build(),

                ToolSpecification.builder()
                        .name("calculate")
                        .description("Perform calculation with manual deserialization")
                        .parameters(JsonObjectSchema.builder()
                                .addStringProperty("operation", "Operation: add, subtract, multiply, divide")
                                .addNumberProperty("a", "First operand")
                                .addNumberProperty("b", "Second operand")
                                .required("operation", "a", "b")
                                .build())
                        .build()
        );
    }

    @Override
    public @Nullable String executeTool(@NotNull String toolName, @NotNull Map<String, Object> arguments) {
        Main.LOGGER.debug("ExampleDataPlugin: Executing tool '{}' with arguments: {}", toolName, arguments);

        try {
            return switch (toolName) {
                case "create_user" -> handleCreateUser(arguments);
                case "calculate" -> handleCalculate(arguments);
                default -> {
                    Main.LOGGER.warn("ExampleDataPlugin: Unknown tool: {}", toolName);
                    yield null;
                }
            };
        } catch (Exception e) {
            Main.LOGGER.error("ExampleDataPlugin: Error in tool '{}': {}", toolName, e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Demonstrates schema-based automatic deserialization.
     */
    private String handleCreateUser(Map<String, Object> arguments) {
        Main.LOGGER.info("ExampleDataPlugin: Creating user with schema deserialization");

        // Use schema-based deserialization
        CreateUserRequest request = ToolArgumentsSchema.deserialize(arguments, CreateUserRequest.class);

        // Validation already done by schema deserializer
        String result = String.format(
                "Created user: %s (%s), age %d, admin: %s",
                request.getUsername(),
                request.getEmail(),
                request.getAge(),
                request.isAdmin()
        );

        Main.LOGGER.info("ExampleDataPlugin: {}", result);
        return result;
    }

    /**
     * Demonstrates manual deserialization with ToolArgumentsDeserializer.
     */
    private String handleCalculate(Map<String, Object> arguments) {
        Main.LOGGER.info("ExampleDataPlugin: Performing calculation");

        // Manual deserialization with validation
        ToolArgumentsDeserializer.requireArguments(arguments, "operation", "a", "b");

        String operation = ToolArgumentsDeserializer.getString(arguments, "operation", "add");
        double a = ToolArgumentsDeserializer.getDouble(arguments, "a", 0.0);
        double b = ToolArgumentsDeserializer.getDouble(arguments, "b", 0.0);

        Main.LOGGER.debug("ExampleDataPlugin: {} {} {}", a, operation, b);

        double result = switch (operation.toLowerCase()) {
            case "add" -> a + b;
            case "subtract" -> a - b;
            case "multiply" -> a * b;
            case "divide" -> {
                if (b == 0) {
                    throw new IllegalArgumentException("Division by zero");
                }
                yield a / b;
            }
            default -> throw new IllegalArgumentException("Unknown operation: " + operation);
        };

        String output = String.format("%s %s %s = %s", a, operation, b, result);
        Main.LOGGER.info("ExampleDataPlugin: {}", output);
        return output;
    }

    @Override
    public void onLoad() {
        Main.LOGGER.info("ExampleDataPlugin: Loaded with data deserialization support");
    }

    @Override
    public void onUnload() {
        Main.LOGGER.info("ExampleDataPlugin: Unloaded");
    }
}

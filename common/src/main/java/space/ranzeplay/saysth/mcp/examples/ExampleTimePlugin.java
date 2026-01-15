package space.ranzeplay.saysth.mcp.examples;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.ranzeplay.saysth.Main;
import space.ranzeplay.saysth.mcp.McpPrompt;
import space.ranzeplay.saysth.mcp.McpResource;
import space.ranzeplay.saysth.mcp.McpServerPlugin;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Example MCP plugin that provides time-related tools.
 *
 * <p>This plugin demonstrates the basic structure and implementation
 * of an MCP server plugin for the SaySomething mod.</p>
 *
 * <p><b>Provided Tools:</b></p>
 * <ul>
 *   <li>get_current_time - Returns current time in a specified timezone</li>
 *   <li>get_server_uptime - Returns how long the server has been running</li>
 * </ul>
 *
 * @author SaySomething Contributors
 * @since 1.2.0
 */
public class ExampleTimePlugin implements McpServerPlugin {

    private long serverStartTime;

    @Override
    public @NotNull String getName() {
        return "example-time-plugin";
    }

    @Override
    public @NotNull String getDescription() {
        return "Provides time-related information and utilities";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public @NotNull List<ToolSpecification> getTools() {
        Main.LOGGER.debug("ExampleTimePlugin: Providing tool specifications");

        return List.of(
            ToolSpecification.builder()
                .name("get_current_time")
                .description("Get the current date and time in a specified timezone")
                    .parameters(JsonObjectSchema.builder()
                            .addStringProperty("timezone", "Timezone identifier (e.g., 'UTC', 'America/New_York', 'Europe/London'). Defaults to 'UTC'.")
                            .addStringProperty("format", "Optional date/time format pattern. Defaults to ISO format.")
                            .build())
                .build(),
            ToolSpecification.builder()
                .name("get_server_uptime")
                .description("Get the amount of time the server has been running")
                .build()
        );
    }

    @Override
    public @Nullable String executeTool(@NotNull String toolName, @NotNull Map<String, Object> arguments) {
        Main.LOGGER.debug("ExampleTimePlugin: Executing tool '{}' with arguments: {}", toolName, arguments);

        try {
            return switch (toolName) {
                case "get_current_time" -> handleGetCurrentTime(arguments);
                case "get_server_uptime" -> handleGetServerUptime();
                default -> {
                    Main.LOGGER.warn("ExampleTimePlugin: Unknown tool requested: {}", toolName);
                    yield null;
                }
            };
        } catch (Exception e) {
            Main.LOGGER.error("ExampleTimePlugin: Error executing tool '{}': {}", toolName, e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Handles the get_current_time tool request.
     *
     * @param arguments tool arguments containing timezone and optional format
     * @return formatted current time string
     */
    private String handleGetCurrentTime(Map<String, Object> arguments) {
        String timezone = (String) arguments.getOrDefault("timezone", "UTC");
        String formatPattern = (String) arguments.get("format");

        Main.LOGGER.debug("ExampleTimePlugin: Getting current time for timezone: {}", timezone);

        try {
            ZoneId zone = ZoneId.of(timezone);
            LocalDateTime now = LocalDateTime.now(zone);

            DateTimeFormatter formatter = formatPattern != null
                ? DateTimeFormatter.ofPattern(formatPattern)
                : DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            String result = now.format(formatter);
            Main.LOGGER.debug("ExampleTimePlugin: Current time result: {}", result);
            return result;

        } catch (Exception e) {
            Main.LOGGER.error("ExampleTimePlugin: Invalid timezone or format: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid timezone or format: " + e.getMessage());
        }
    }

    /**
     * Handles the get_server_uptime tool request.
     *
     * @return human-readable uptime string
     */
    private String handleGetServerUptime() {
        Main.LOGGER.debug("ExampleTimePlugin: Calculating server uptime");

        long uptimeMillis = System.currentTimeMillis() - serverStartTime;
        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        String result = String.format("%d days, %d hours, %d minutes, %d seconds",
            days, hours % 24, minutes % 60, seconds % 60);

        Main.LOGGER.debug("ExampleTimePlugin: Server uptime: {}", result);
        return result;
    }

    @Override
    public @NotNull List<McpResource> getResources() {
        return List.of(
            McpResource.text(
                "time://timezones",
                "Available Timezones",
                "List of all available timezone identifiers"
            )
        );
    }

    @Override
    public @Nullable String readResource(@NotNull String resourceUri) {
        Main.LOGGER.debug("ExampleTimePlugin: Reading resource: {}", resourceUri);

        if ("time://timezones".equals(resourceUri)) {
            // Return a subset of common timezones
            return String.join("\n", ZoneId.getAvailableZoneIds().stream()
                    .filter(zone -> zone.startsWith("America/") ||
                                  zone.startsWith("Europe/") ||
                                  zone.startsWith("Asia/") ||
                                  zone.equals("UTC"))
                    .sorted()
                    .limit(50)
                    .toList());
        }

        Main.LOGGER.warn("ExampleTimePlugin: Unknown resource URI: {}", resourceUri);
        return null;
    }

    @Override
    public @NotNull List<McpPrompt> getPrompts() {
        return List.of(
            McpPrompt.simple(
                "time-greeting",
                "Time-based Greeting",
                "Generate a greeting based on the current time of day"
            )
        );
    }

    @Override
    public void onLoad() throws Exception {
        serverStartTime = System.currentTimeMillis();
        Main.LOGGER.info("ExampleTimePlugin: Loaded successfully at {}",
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Override
    public void onUnload() {
        Main.LOGGER.info("ExampleTimePlugin: Unloaded after {} ms of runtime",
            System.currentTimeMillis() - serverStartTime);
    }
}

package space.ranzeplay.saysth.mcp;

import dev.langchain4j.agent.tool.ToolSpecification;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Base interface for MCP (Model Context Protocol) server plugins.
 * 
 * <p>MCP server plugins extend the capabilities of villagers by providing custom tools,
 * resources, and prompts that can be invoked during conversations with players.</p>
 * 
 * <p><b>Example Implementation:</b></p>
 * <pre>{@code
 * public class WeatherMcpPlugin implements McpServerPlugin {
 *     @Override
 *     public String getName() {
 *         return "weather-service";
 *     }
 * 
 *     @Override
 *     public String getDescription() {
 *         return "Provides real-time weather information";
 *     }
 * 
 *     @Override
 *     public List<ToolSpecification> getTools() {
 *         return List.of(
 *             ToolSpecification.builder()
 *                 .name("get_weather")
 *                 .description("Get current weather for a location")
 *                 .addParameter("location", String.class, "City name")
 *                 .build()
 *         );
 *     }
 * 
 *     @Override
 *     public String executeTool(String toolName, Map<String, Object> arguments) {
 *         if ("get_weather".equals(toolName)) {
 *             String location = (String) arguments.get("location");
 *             return fetchWeather(location);
 *         }
 *         return null;
 *     }
 * }
 * }</pre>
 * 
 * @author SaySomething Contributors
 * @since 1.2.0
 * @see McpPluginRegistry
 * @see ToolSpecification
 */
public interface McpServerPlugin {
    
    /**
     * Gets the unique name of this MCP plugin.
     * 
     * <p>This name is used to identify the plugin in logs and configuration.
     * It should be lowercase, use hyphens instead of spaces, and be descriptive.</p>
     * 
     * @return the plugin name, must not be null or empty
     */
    @NotNull
    String getName();
    
    /**
     * Gets a human-readable description of this plugin's functionality.
     * 
     * <p>This description helps users understand what capabilities the plugin provides.</p>
     * 
     * @return the plugin description, must not be null
     */
    @NotNull
    String getDescription();
    
    /**
     * Gets the version of this plugin.
     * 
     * <p>Version should follow semantic versioning (e.g., "1.0.0").</p>
     * 
     * @return the plugin version, defaults to "1.0.0" if not overridden
     */
    @NotNull
    default String getVersion() {
        return "1.0.0";
    }
    
    /**
     * Gets the list of tools provided by this plugin.
     * 
     * <p>Tools are functions that can be called by the AI model during conversations.
     * Each tool should have a clear name, description, and parameter specifications.</p>
     * 
     * <p><b>Best Practices:</b></p>
     * <ul>
     *   <li>Use descriptive tool names (e.g., "get_weather", not "tool1")</li>
     *   <li>Provide clear descriptions that help the AI understand when to use the tool</li>
     *   <li>Define all required and optional parameters with proper types</li>
     *   <li>Keep tool operations atomic and focused on a single task</li>
     * </ul>
     * 
     * @return list of tool specifications, or empty list if no tools provided
     */
    @NotNull
    default List<ToolSpecification> getTools() {
        return List.of();
    }
    
    /**
     * Executes a tool provided by this plugin.
     * 
     * <p>This method is called when the AI model requests to invoke one of the plugin's tools.
     * The implementation should validate arguments, perform the requested operation,
     * and return a result that can be understood by the AI model.</p>
     * 
     * <p><b>Error Handling:</b></p>
     * <ul>
     *   <li>Return null or an error message if the tool name is not recognized</li>
     *   <li>Validate all required arguments before processing</li>
     *   <li>Log errors for debugging but return user-friendly error messages</li>
     *   <li>Handle exceptions gracefully and return meaningful error descriptions</li>
     * </ul>
     * 
     * @param toolName the name of the tool to execute
     * @param arguments map of argument name to value
     * @return the tool execution result as a string, or null if tool not found
     * @throws IllegalArgumentException if required arguments are missing or invalid
     */
    @Nullable
    String executeTool(@NotNull String toolName, @NotNull Map<String, Object> arguments);
    
    /**
     * Gets the list of resources provided by this plugin.
     * 
     * <p>Resources are data sources that can be referenced during conversations,
     * such as documentation, knowledge bases, or configuration files.</p>
     * 
     * @return list of resource specifications, or empty list if no resources provided
     */
    @NotNull
    default List<McpResource> getResources() {
        return List.of();
    }
    
    /**
     * Reads the content of a resource provided by this plugin.
     * 
     * @param resourceUri the URI of the resource to read
     * @return the resource content, or null if resource not found
     */
    @Nullable
    default String readResource(@NotNull String resourceUri) {
        return null;
    }
    
    /**
     * Gets the list of prompts provided by this plugin.
     * 
     * <p>Prompts are pre-defined conversation starters or templates
     * that can be used to guide interactions.</p>
     * 
     * @return list of prompt specifications, or empty list if no prompts provided
     */
    @NotNull
    default List<McpPrompt> getPrompts() {
        return List.of();
    }
    
    /**
     * Called when the plugin is loaded and initialized.
     * 
     * <p>Use this method to set up any required resources, connections,
     * or configuration needed by the plugin.</p>
     * 
     * @throws Exception if initialization fails
     */
    default void onLoad() throws Exception {
        // Default: no initialization needed
    }
    
    /**
     * Called when the plugin is unloaded or disabled.
     * 
     * <p>Use this method to clean up resources, close connections,
     * and perform any necessary shutdown operations.</p>
     */
    default void onUnload() {
        // Default: no cleanup needed
    }
}

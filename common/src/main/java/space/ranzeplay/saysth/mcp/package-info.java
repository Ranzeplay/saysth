/**
 * MCP (Model Context Protocol) Server Plugin SDK for SaySomething.
 * 
 * <p>This package provides a comprehensive SDK for developers to create custom
 * MCP server plugins that extend the capabilities of villagers in the SaySomething mod.</p>
 * 
 * <h2>Overview</h2>
 * 
 * <p>The MCP Server Plugin SDK allows developers to:</p>
 * <ul>
 *   <li>Create custom tools that AI-powered villagers can use</li>
 *   <li>Provide resources (data sources) for villagers to reference</li>
 *   <li>Define prompts (conversation templates) for specific scenarios</li>
 *   <li>Extend villager behavior with custom functionality</li>
 * </ul>
 * 
 * <h2>Core Components</h2>
 * 
 * <h3>{@link space.ranzeplay.saysth.mcp.McpServerPlugin}</h3>
 * <p>The main interface that all plugins must implement. Defines the contract for
 * plugin lifecycle, tools, resources, and prompts.</p>
 * 
 * <h3>{@link space.ranzeplay.saysth.mcp.McpPluginRegistry}</h3>
 * <p>Singleton registry that manages all registered plugins. Handles plugin
 * registration, lifecycle management, and tool execution routing.</p>
 * 
 * <h3>{@link space.ranzeplay.saysth.mcp.McpResource}</h3>
 * <p>Represents a data resource (like documentation or knowledge bases) that
 * can be referenced during conversations.</p>
 * 
 * <h3>{@link space.ranzeplay.saysth.mcp.McpPrompt}</h3>
 * <p>Represents a conversation template that can guide interactions between
 * players and villagers.</p>
 * 
 * <h3>{@link space.ranzeplay.saysth.mcp.ToolArgumentsDeserializer}</h3>
 * <p>Utility class for extracting and converting tool arguments from the generic
 * Map into strongly-typed values with validation and type safety.</p>
 * 
 * <h3>{@link space.ranzeplay.saysth.mcp.ToolArgumentsSchema}</h3>
 * <p>Schema-based deserializer that automatically converts tool arguments into
 * annotated objects using {@link space.ranzeplay.saysth.mcp.ToolArgument} annotations.</p>
 * 
 * <h3>{@link space.ranzeplay.saysth.mcp.ToolArgument}</h3>
 * <p>Annotation for defining tool argument schemas with metadata for validation,
 * documentation, and default values.</p>
 * 
 * <h2>Quick Start</h2>
 * 
 * <p>Creating a simple plugin:</p>
 * <pre>{@code
 * public class MyPlugin implements McpServerPlugin {
 *     @Override
 *     public String getName() {
 *         return "my-plugin";
 *     }
 *     
 *     @Override
 *     public String getDescription() {
 *         return "My custom plugin for SaySomething";
 *     }
 *     
 *     @Override
 *     public List<ToolSpecification> getTools() {
 *         return List.of(
 *             ToolSpecification.builder()
 *                 .name("my_tool")
 *                 .description("Does something useful")
 *                 .addParameter("input", String.class, "Input parameter")
 *                 .build()
 *         );
 *     }
 *     
 *     @Override
 *     public String executeTool(String toolName, Map<String, Object> arguments) {
 *         if ("my_tool".equals(toolName)) {
 *             String input = (String) arguments.get("input");
 *             return processInput(input);
 *         }
 *         return null;
 *     }
 * }
 * }</pre>
 * 
 * <p>Registering the plugin:</p>
 * <pre>{@code
 * McpPluginRegistry registry = McpPluginRegistry.getInstance();
 * registry.registerPlugin(new MyPlugin());
 * }</pre>
 * 
 * <h2>Best Practices</h2>
 * 
 * <ol>
 *   <li><b>Validation:</b> Always validate tool arguments before processing</li>
 *   <li><b>Error Handling:</b> Return user-friendly error messages, not stack traces</li>
 *   <li><b>Logging:</b> Use comprehensive logging for debugging (via {@code Main.LOGGER})</li>
 *   <li><b>Cleanup:</b> Properly clean up resources in {@code onUnload()}</li>
 *   <li><b>Documentation:</b> Provide clear tool descriptions to help the AI use them correctly</li>
 * </ol>
 * 
 * <h2>Examples</h2>
 * 
 * <p>See {@link space.ranzeplay.saysth.mcp.examples.ExampleTimePlugin} for a complete
 * working example of an MCP server plugin.</p>
 * 
 * <h2>Additional Resources</h2>
 * 
 * <ul>
 *   <li>Full Development Guide: {@code docs/MCP_PLUGIN_DEVELOPMENT.md}</li>
 *   <li>LangChain4j Documentation: https://docs.langchain4j.dev/</li>
 *   <li>Model Context Protocol: https://modelcontextprotocol.io/</li>
 * </ul>
 * 
 * @since 1.2.0
 * @author SaySomething Contributors
 */
package space.ranzeplay.saysth.mcp;

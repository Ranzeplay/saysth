package space.ranzeplay.saysth.mcp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.ranzeplay.saysth.Main;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for managing MCP server plugins.
 * 
 * <p>This singleton class handles plugin registration, lifecycle management,
 * and provides access to plugin capabilities.</p>
 * 
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * // Register a plugin
 * McpPluginRegistry registry = McpPluginRegistry.getInstance();
 * registry.registerPlugin(new MyCustomPlugin());
 * 
 * // Execute a tool from any registered plugin
 * String result = registry.executeTool("my-plugin", "get_data", arguments);
 * }</pre>
 * 
 * @author SaySomething Contributors
 * @since 1.2.0
 */
public class McpPluginRegistry {
    
    private static final McpPluginRegistry INSTANCE = new McpPluginRegistry();
    
    /**
     * Map of plugin name to plugin instance.
     */
    private final Map<String, McpServerPlugin> plugins = new ConcurrentHashMap<>();
    
    /**
     * Map tracking plugin load status.
     */
    private final Map<String, Boolean> pluginLoadStatus = new ConcurrentHashMap<>();
    
    private McpPluginRegistry() {
        Main.LOGGER.info("Initializing MCP Plugin Registry");
    }
    
    /**
     * Gets the singleton instance of the plugin registry.
     * 
     * @return the registry instance
     */
    public static McpPluginRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Registers a new MCP server plugin.
     * 
     * <p>If a plugin with the same name already exists, it will be unloaded
     * and replaced with the new plugin.</p>
     * 
     * <p><b>Logging:</b> Logs registration success, failures, and any exceptions.</p>
     * 
     * @param plugin the plugin to register, must not be null
     * @throws IllegalArgumentException if plugin is null or has an invalid name
     */
    public void registerPlugin(@NotNull McpServerPlugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        
        String name = plugin.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Plugin name cannot be null or empty");
        }
        
        Main.LOGGER.info("Registering MCP plugin: {} v{}", name, plugin.getVersion());
        Main.LOGGER.debug("Plugin description: {}", plugin.getDescription());
        
        try {
            // Unload existing plugin if present
            if (plugins.containsKey(name)) {
                Main.LOGGER.warn("Plugin '{}' is already registered. Replacing with new instance.", name);
                unregisterPlugin(name);
            }
            
            // Load the new plugin
            Main.LOGGER.debug("Loading plugin '{}'", name);
            plugin.onLoad();
            
            // Register the plugin
            plugins.put(name, plugin);
            pluginLoadStatus.put(name, true);
            
            // Log plugin capabilities
            int toolCount = plugin.getTools().size();
            int resourceCount = plugin.getResources().size();
            int promptCount = plugin.getPrompts().size();
            
            Main.LOGGER.info("Successfully registered plugin '{}' with {} tool(s), {} resource(s), {} prompt(s)",
                    name, toolCount, resourceCount, promptCount);
            
            if (toolCount > 0) {
                Main.LOGGER.debug("Tools provided by '{}': {}", name, 
                        plugin.getTools().stream().map(dev.langchain4j.agent.tool.ToolSpecification::name).toList());
            }
            
        } catch (Exception e) {
            pluginLoadStatus.put(name, false);
            Main.LOGGER.error("Failed to register plugin '{}': {}", name, e.getMessage(), e);
            throw new RuntimeException("Failed to register plugin: " + name, e);
        }
    }
    
    /**
     * Unregisters an MCP server plugin.
     * 
     * <p>This will call the plugin's {@link McpServerPlugin#onUnload()} method
     * and remove it from the registry.</p>
     * 
     * @param pluginName the name of the plugin to unregister
     * @return true if the plugin was unregistered, false if it was not found
     */
    public boolean unregisterPlugin(@NotNull String pluginName) {
        if (pluginName == null || pluginName.trim().isEmpty()) {
            Main.LOGGER.warn("Cannot unregister plugin with null or empty name");
            return false;
        }
        
        McpServerPlugin plugin = plugins.get(pluginName);
        if (plugin == null) {
            Main.LOGGER.warn("Plugin '{}' not found in registry", pluginName);
            return false;
        }
        
        Main.LOGGER.info("Unregistering plugin '{}'", pluginName);
        
        try {
            plugin.onUnload();
            plugins.remove(pluginName);
            pluginLoadStatus.remove(pluginName);
            Main.LOGGER.info("Successfully unregistered plugin '{}'", pluginName);
            return true;
        } catch (Exception e) {
            Main.LOGGER.error("Error while unloading plugin '{}': {}", pluginName, e.getMessage(), e);
            // Still remove from registry even if unload failed
            plugins.remove(pluginName);
            pluginLoadStatus.remove(pluginName);
            return true;
        }
    }
    
    /**
     * Gets a registered plugin by name.
     * 
     * @param pluginName the plugin name
     * @return the plugin instance, or null if not found
     */
    @Nullable
    public McpServerPlugin getPlugin(@NotNull String pluginName) {
        return plugins.get(pluginName);
    }
    
    /**
     * Gets all registered plugins.
     * 
     * @return unmodifiable collection of all plugins
     */
    @NotNull
    public Collection<McpServerPlugin> getAllPlugins() {
        return Collections.unmodifiableCollection(plugins.values());
    }
    
    /**
     * Gets the names of all registered plugins.
     * 
     * @return unmodifiable set of plugin names
     */
    @NotNull
    public Set<String> getPluginNames() {
        return Collections.unmodifiableSet(plugins.keySet());
    }
    
    /**
     * Checks if a plugin is registered.
     * 
     * @param pluginName the plugin name to check
     * @return true if the plugin is registered, false otherwise
     */
    public boolean isPluginRegistered(@NotNull String pluginName) {
        return plugins.containsKey(pluginName);
    }
    
    /**
     * Checks if a plugin was successfully loaded.
     * 
     * @param pluginName the plugin name to check
     * @return true if loaded successfully, false if failed or not registered
     */
    public boolean isPluginLoaded(@NotNull String pluginName) {
        return pluginLoadStatus.getOrDefault(pluginName, false);
    }
    
    /**
     * Executes a tool from a specific plugin.
     * 
     * <p><b>Error Handling:</b></p>
     * <ul>
     *   <li>Returns null if the plugin is not found</li>
     *   <li>Returns null if the tool is not found in the plugin</li>
     *   <li>Logs all errors for debugging</li>
     *   <li>Catches and logs exceptions during tool execution</li>
     * </ul>
     * 
     * @param pluginName the name of the plugin
     * @param toolName the name of the tool to execute
     * @param arguments the tool arguments
     * @return the tool execution result, or null if plugin/tool not found or execution failed
     */
    @Nullable
    public String executeTool(@NotNull String pluginName, @NotNull String toolName, @NotNull Map<String, Object> arguments) {
        Main.LOGGER.debug("Executing tool '{}' from plugin '{}' with arguments: {}", toolName, pluginName, arguments);
        
        McpServerPlugin plugin = plugins.get(pluginName);
        if (plugin == null) {
            Main.LOGGER.warn("Cannot execute tool '{}': plugin '{}' not found", toolName, pluginName);
            return null;
        }
        
        try {
            String result = plugin.executeTool(toolName, arguments);
            
            if (result == null) {
                Main.LOGGER.warn("Tool '{}' in plugin '{}' returned null. Tool may not exist or failed.", 
                        toolName, pluginName);
            } else {
                Main.LOGGER.debug("Tool '{}' executed successfully. Result length: {} characters", 
                        toolName, result.length());
            }
            
            return result;
        } catch (Exception e) {
            Main.LOGGER.error("Error executing tool '{}' from plugin '{}': {}", 
                    toolName, pluginName, e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Reads a resource from a specific plugin.
     * 
     * @param pluginName the name of the plugin
     * @param resourceUri the URI of the resource to read
     * @return the resource content, or null if plugin/resource not found
     */
    @Nullable
    public String readResource(@NotNull String pluginName, @NotNull String resourceUri) {
        Main.LOGGER.debug("Reading resource '{}' from plugin '{}'", resourceUri, pluginName);
        
        McpServerPlugin plugin = plugins.get(pluginName);
        if (plugin == null) {
            Main.LOGGER.warn("Cannot read resource '{}': plugin '{}' not found", resourceUri, pluginName);
            return null;
        }
        
        try {
            String content = plugin.readResource(resourceUri);
            
            if (content == null) {
                Main.LOGGER.warn("Resource '{}' not found in plugin '{}'", resourceUri, pluginName);
            } else {
                Main.LOGGER.debug("Resource '{}' read successfully. Content length: {} characters", 
                        resourceUri, content.length());
            }
            
            return content;
        } catch (Exception e) {
            Main.LOGGER.error("Error reading resource '{}' from plugin '{}': {}", 
                    resourceUri, pluginName, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Gets all tools from all registered plugins.
     * 
     * @return list of all tool specifications from all plugins
     */
    @NotNull
    public List<dev.langchain4j.agent.tool.ToolSpecification> getAllTools() {
        List<dev.langchain4j.agent.tool.ToolSpecification> allTools = new ArrayList<>();
        
        for (McpServerPlugin plugin : plugins.values()) {
            try {
                allTools.addAll(plugin.getTools());
            } catch (Exception e) {
                Main.LOGGER.error("Error getting tools from plugin '{}': {}", 
                        plugin.getName(), e.getMessage(), e);
            }
        }
        
        return allTools;
    }
    
    /**
     * Unloads all registered plugins.
     * 
     * <p>This should be called during application shutdown to properly
     * clean up all plugin resources.</p>
     */
    public void unloadAll() {
        Main.LOGGER.info("Unloading all MCP plugins");
        
        List<String> pluginNames = new ArrayList<>(plugins.keySet());
        for (String pluginName : pluginNames) {
            unregisterPlugin(pluginName);
        }
        
        Main.LOGGER.info("All MCP plugins unloaded");
    }
}

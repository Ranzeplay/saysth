package space.ranzeplay.saysth.mcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a resource that can be accessed through MCP.
 * 
 * <p>Resources are data sources like documentation, knowledge bases, or files
 * that the AI model can reference during conversations.</p>
 * 
 * <p><b>Example:</b></p>
 * <pre>{@code
 * McpResource gameRules = new McpResource(
 *     "game://rules",
 *     "Game Rules",
 *     "Complete rules and guidelines for the game",
 *     "text/plain"
 * );
 * }</pre>
 * 
 * @author SaySomething Contributors
 * @since 1.2.0
 */
@Data
@AllArgsConstructor
public class McpResource {
    
    /**
     * The unique URI identifying this resource.
     * 
     * <p>Should follow a URI format, e.g., "game://rules" or "file:///data/config.json"</p>
     */
    @NotNull
    private String uri;
    
    /**
     * Human-readable name of the resource.
     */
    @NotNull
    private String name;
    
    /**
     * Description of what the resource contains.
     */
    @NotNull
    private String description;
    
    /**
     * MIME type of the resource content (e.g., "text/plain", "application/json").
     * 
     * <p>Can be null if the content type is not specified.</p>
     */
    @Nullable
    private String mimeType;
    
    /**
     * Creates a resource with text/plain MIME type.
     * 
     * @param uri the resource URI
     * @param name the resource name
     * @param description the resource description
     * @return a new McpResource instance
     */
    public static McpResource text(@NotNull String uri, @NotNull String name, @NotNull String description) {
        return new McpResource(uri, name, description, "text/plain");
    }
    
    /**
     * Creates a resource with application/json MIME type.
     * 
     * @param uri the resource URI
     * @param name the resource name
     * @param description the resource description
     * @return a new McpResource instance
     */
    public static McpResource json(@NotNull String uri, @NotNull String name, @NotNull String description) {
        return new McpResource(uri, name, description, "application/json");
    }
}

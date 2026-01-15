package space.ranzeplay.saysth.mcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Represents a prompt template that can be used through MCP.
 * 
 * <p>Prompts are pre-defined conversation starters or templates that help
 * guide interactions with the AI model.</p>
 * 
 * <p><b>Example:</b></p>
 * <pre>{@code
 * McpPrompt greetingPrompt = new McpPrompt(
 *     "friendly-greeting",
 *     "Friendly Greeting",
 *     "A warm and welcoming greeting for new players",
 *     List.of()
 * );
 * }</pre>
 * 
 * @author SaySomething Contributors
 * @since 1.2.0
 */
@Data
@AllArgsConstructor
public class McpPrompt {
    
    /**
     * Unique identifier for the prompt.
     */
    @NotNull
    private String name;
    
    /**
     * Human-readable title of the prompt.
     */
    @NotNull
    private String title;
    
    /**
     * Description of what the prompt does or when to use it.
     */
    @NotNull
    private String description;
    
    /**
     * List of arguments that can be passed to customize the prompt.
     * 
     * <p>Each entry is a map with keys like "name", "description", and "required".</p>
     */
    @NotNull
    private List<Map<String, Object>> arguments;
    
    /**
     * Creates a simple prompt without arguments.
     * 
     * @param name unique identifier for the prompt
     * @param title human-readable title
     * @param description description of the prompt
     * @return a new McpPrompt instance
     */
    public static McpPrompt simple(@NotNull String name, @NotNull String title, @NotNull String description) {
        return new McpPrompt(name, title, description, List.of());
    }
}

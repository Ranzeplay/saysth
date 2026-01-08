package space.ranzeplay.saysth.mcp;

import java.lang.annotation.*;

/**
 * Annotation to define the schema for a tool argument.
 * 
 * <p>This annotation can be used on fields of a data class to specify
 * argument metadata that helps with validation and documentation.</p>
 * 
 * <p><b>Example:</b></p>
 * <pre>{@code
 * public class WeatherRequest {
 *     @ToolArgument(name = "location", required = true, description = "City name or coordinates")
 *     private String location;
 *     
 *     @ToolArgument(name = "units", required = false, description = "Temperature units", 
 *                   defaultValue = "celsius")
 *     private String units;
 *     
 *     @ToolArgument(name = "days", required = false, description = "Number of forecast days",
 *                   defaultValue = "7")
 *     private int days;
 * }
 * }</pre>
 * 
 * @author SaySomething Contributors
 * @since 1.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface ToolArgument {
    
    /**
     * The name of the argument as it appears in the tool call.
     * 
     * @return the argument name
     */
    String name();
    
    /**
     * Description of what this argument represents.
     * 
     * @return the argument description
     */
    String description() default "";
    
    /**
     * Whether this argument is required.
     * 
     * @return true if required, false otherwise
     */
    boolean required() default false;
    
    /**
     * Default value for the argument if not provided.
     * 
     * @return the default value as a string
     */
    String defaultValue() default "";
}

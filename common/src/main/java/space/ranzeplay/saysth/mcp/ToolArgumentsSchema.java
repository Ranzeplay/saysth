package space.ranzeplay.saysth.mcp;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import space.ranzeplay.saysth.Main;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Schema-based deserializer for tool arguments using {@link ToolArgument} annotations.
 * 
 * <p>This class automatically deserializes tool arguments into strongly-typed objects
 * based on field annotations, including validation of required fields and application
 * of default values.</p>
 * 
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * public class WeatherRequest {
 *     @ToolArgument(name = "location", required = true, description = "City name")
 *     private String location;
 *     
 *     @ToolArgument(name = "units", defaultValue = "celsius")
 *     private String units = "celsius";
 * }
 * 
 * // In executeTool:
 * WeatherRequest request = ToolArgumentsSchema.deserialize(
 *     arguments, 
 *     WeatherRequest.class
 * );
 * }</pre>
 * 
 * @author SaySomething Contributors
 * @since 1.2.0
 */
public class ToolArgumentsSchema {
    
    private static final Gson GSON = new Gson();
    
    /**
     * Deserializes tool arguments into a schema-annotated object.
     * 
     * <p>This method creates a new instance of the target class and populates
     * its fields based on {@link ToolArgument} annotations, validating required
     * fields and applying defaults.</p>
     * 
     * @param <T> the target type
     * @param arguments the tool arguments map
     * @param schemaClass the class with {@link ToolArgument} annotations
     * @return populated instance of the schema class
     * @throws IllegalArgumentException if required arguments are missing or deserialization fails
     */
    @NotNull
    public static <T> T deserialize(@NotNull Map<String, Object> arguments, 
                                   @NotNull Class<T> schemaClass) {
        Main.LOGGER.debug("ToolArgumentsSchema: Deserializing arguments for {}", schemaClass.getSimpleName());
        
        try {
            T instance = schemaClass.getDeclaredConstructor().newInstance();
            
            for (Field field : schemaClass.getDeclaredFields()) {
                ToolArgument annotation = field.getAnnotation(ToolArgument.class);
                if (annotation == null) {
                    continue;
                }
                
                String argumentName = annotation.name();
                Object value = arguments.get(argumentName);
                
                Main.LOGGER.debug("ToolArgumentsSchema: Processing field '{}' (argument '{}')", 
                        field.getName(), argumentName);
                
                // Handle required fields
                if (annotation.required() && value == null) {
                    String error = "Required argument '" + argumentName + "' is missing";
                    Main.LOGGER.error("ToolArgumentsSchema: {}", error);
                    throw new IllegalArgumentException(error);
                }
                
                // Apply default value if needed
                if (value == null && !annotation.defaultValue().isEmpty()) {
                    value = annotation.defaultValue();
                    Main.LOGGER.debug("ToolArgumentsSchema: Applying default value '{}' to '{}'", 
                            value, argumentName);
                }
                
                // Skip if still null
                if (value == null) {
                    Main.LOGGER.debug("ToolArgumentsSchema: Skipping null optional argument '{}'", 
                            argumentName);
                    continue;
                }
                
                // Set the field value
                field.setAccessible(true);
                Object convertedValue = convertValue(value, field.getType());
                field.set(instance, convertedValue);
                
                Main.LOGGER.debug("ToolArgumentsSchema: Set field '{}' to value: {}", 
                        field.getName(), convertedValue);
            }
            
            Main.LOGGER.debug("ToolArgumentsSchema: Successfully deserialized {}", 
                    schemaClass.getSimpleName());
            return instance;
            
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (NoSuchMethodException e) {
            Main.LOGGER.error("ToolArgumentsSchema: {} must have a no-argument constructor", 
                    schemaClass.getSimpleName());
            throw new IllegalArgumentException(
                    schemaClass.getSimpleName() + " must have a public no-argument constructor", e);
        } catch (IllegalAccessException e) {
            Main.LOGGER.error("ToolArgumentsSchema: Cannot access constructor of {}", 
                    schemaClass.getSimpleName());
            throw new IllegalArgumentException(
                    "Cannot access constructor of " + schemaClass.getSimpleName() + 
                    ". Ensure it has a public no-argument constructor.", e);
        } catch (InstantiationException e) {
            Main.LOGGER.error("ToolArgumentsSchema: Cannot instantiate {}", 
                    schemaClass.getSimpleName());
            throw new IllegalArgumentException(
                    "Cannot instantiate " + schemaClass.getSimpleName() + 
                    ". Ensure it is not abstract and has a public no-argument constructor.", e);
        } catch (Exception e) {
            Main.LOGGER.error("ToolArgumentsSchema: Failed to deserialize {}: {}", 
                    schemaClass.getSimpleName(), e.getMessage(), e);
            throw new IllegalArgumentException("Failed to deserialize arguments: " + e.getMessage(), e);
        }
    }
    
    /**
     * Converts a value to the target field type.
     * 
     * @param value the value to convert
     * @param targetType the target field type
     * @return converted value
     */
    private static Object convertValue(Object value, Class<?> targetType) {
        // If already the correct type
        if (targetType.isInstance(value)) {
            return value;
        }
        
        // Handle primitive types and their wrappers
        if (targetType == int.class || targetType == Integer.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        }
        
        if (targetType == long.class || targetType == Long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.parseLong(value.toString());
        }
        
        if (targetType == double.class || targetType == Double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.parseDouble(value.toString());
        }
        
        if (targetType == float.class || targetType == Float.class) {
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
            return Float.parseFloat(value.toString());
        }
        
        if (targetType == boolean.class || targetType == Boolean.class) {
            if (value instanceof Boolean) {
                return value;
            }
            return Boolean.parseBoolean(value.toString());
        }
        
        if (targetType == String.class) {
            return value.toString();
        }
        
        // For complex types, use JSON deserialization
        try {
            String json = GSON.toJson(value);
            T result = GSON.fromJson(json, targetType);
            if (result != null) {
                return result;
            }
        } catch (Exception e) {
            Main.LOGGER.warn("ToolArgumentsSchema: Failed to convert value to {} via JSON", 
                    targetType.getSimpleName());
        }
        
        // Last resort: throw exception for type mismatch
        throw new IllegalArgumentException(
                String.format("Cannot convert value of type %s to %s", 
                        value.getClass().getSimpleName(), 
                        targetType.getSimpleName()));
    }
}

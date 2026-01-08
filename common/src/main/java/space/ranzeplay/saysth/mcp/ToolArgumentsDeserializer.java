package space.ranzeplay.saysth.mcp;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for deserializing tool arguments into strongly-typed objects.
 * 
 * <p>This class provides convenient methods to extract and convert arguments
 * from the generic Map&lt;String, Object&gt; into specific types, with proper
 * error handling and validation.</p>
 * 
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // Simple type extraction
 * String name = ToolArgumentsDeserializer.getString(arguments, "name", "default");
 * int age = ToolArgumentsDeserializer.getInt(arguments, "age", 0);
 * 
 * // Complex object deserialization
 * UserData user = ToolArgumentsDeserializer.deserialize(
 *     arguments.get("user"), 
 *     UserData.class
 * );
 * 
 * // List deserialization
 * List<String> tags = ToolArgumentsDeserializer.getList(
 *     arguments, 
 *     "tags", 
 *     String.class
 * );
 * }</pre>
 * 
 * @author SaySomething Contributors
 * @since 1.2.0
 */
public class ToolArgumentsDeserializer {
    
    private static final Gson GSON = new Gson();
    
    /**
     * Gets a string argument from the arguments map.
     * 
     * @param arguments the arguments map
     * @param key the argument key
     * @param defaultValue default value if key not found or value is null
     * @return the string value or default
     */
    @NotNull
    public static String getString(@NotNull Map<String, Object> arguments, 
                                   @NotNull String key, 
                                   @NotNull String defaultValue) {
        Object value = arguments.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }
    
    /**
     * Gets a required string argument from the arguments map.
     * 
     * @param arguments the arguments map
     * @param key the argument key
     * @return the string value
     * @throws IllegalArgumentException if the key is not found or value is null
     */
    @NotNull
    public static String getRequiredString(@NotNull Map<String, Object> arguments, 
                                          @NotNull String key) {
        Object value = arguments.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Required argument '" + key + "' is missing");
        }
        return value.toString();
    }
    
    /**
     * Gets an integer argument from the arguments map.
     * 
     * @param arguments the arguments map
     * @param key the argument key
     * @param defaultValue default value if key not found or conversion fails
     * @return the integer value or default
     */
    public static int getInt(@NotNull Map<String, Object> arguments, 
                            @NotNull String key, 
                            int defaultValue) {
        Object value = arguments.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Gets a long argument from the arguments map.
     * 
     * @param arguments the arguments map
     * @param key the argument key
     * @param defaultValue default value if key not found or conversion fails
     * @return the long value or default
     */
    public static long getLong(@NotNull Map<String, Object> arguments, 
                              @NotNull String key, 
                              long defaultValue) {
        Object value = arguments.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Gets a double argument from the arguments map.
     * 
     * @param arguments the arguments map
     * @param key the argument key
     * @param defaultValue default value if key not found or conversion fails
     * @return the double value or default
     */
    public static double getDouble(@NotNull Map<String, Object> arguments, 
                                   @NotNull String key, 
                                   double defaultValue) {
        Object value = arguments.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Gets a boolean argument from the arguments map.
     * 
     * @param arguments the arguments map
     * @param key the argument key
     * @param defaultValue default value if key not found
     * @return the boolean value or default
     */
    public static boolean getBoolean(@NotNull Map<String, Object> arguments, 
                                    @NotNull String key, 
                                    boolean defaultValue) {
        Object value = arguments.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        
        return Boolean.parseBoolean(value.toString());
    }
    
    /**
     * Gets a list argument from the arguments map.
     * 
     * @param <T> the element type
     * @param arguments the arguments map
     * @param key the argument key
     * @param elementType the class of list elements
     * @return the list or empty list if not found
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(@NotNull Map<String, Object> arguments, 
                                     @NotNull String key, 
                                     @NotNull Class<T> elementType) {
        Object value = arguments.get(key);
        if (value == null) {
            return List.of();
        }
        
        if (value instanceof List) {
            return (List<T>) value;
        }
        
        // Try to deserialize from JSON string
        try {
            String json = GSON.toJson(value);
            Type listType = com.google.gson.reflect.TypeToken.getParameterized(List.class, elementType).getType();
            return GSON.fromJson(json, listType);
        } catch (Exception e) {
            return List.of();
        }
    }
    
    /**
     * Deserializes an argument value into a specific type.
     * 
     * <p>This method is useful for complex nested objects that need to be
     * reconstructed from the argument map.</p>
     * 
     * @param <T> the target type
     * @param value the value to deserialize
     * @param targetType the class to deserialize into
     * @return the deserialized object or null if deserialization fails
     */
    @Nullable
    public static <T> T deserialize(@Nullable Object value, @NotNull Class<T> targetType) {
        if (value == null) {
            return null;
        }
        
        // If already the correct type, return directly
        if (targetType.isInstance(value)) {
            return targetType.cast(value);
        }
        
        // Try to deserialize via JSON
        try {
            String json = GSON.toJson(value);
            return GSON.fromJson(json, targetType);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
    
    /**
     * Deserializes an argument from the map into a specific type.
     * 
     * @param <T> the target type
     * @param arguments the arguments map
     * @param key the argument key
     * @param targetType the class to deserialize into
     * @return the deserialized object or null if not found or deserialization fails
     */
    @Nullable
    public static <T> T getObject(@NotNull Map<String, Object> arguments, 
                                 @NotNull String key, 
                                 @NotNull Class<T> targetType) {
        Object value = arguments.get(key);
        return deserialize(value, targetType);
    }
    
    /**
     * Gets an argument with custom conversion logic.
     * 
     * @param <T> the target type
     * @param arguments the arguments map
     * @param key the argument key
     * @param converter function to convert the value
     * @param defaultValue default value if key not found or conversion fails
     * @return the converted value or default
     */
    @Nullable
    public static <T> T getWithConverter(@NotNull Map<String, Object> arguments,
                                        @NotNull String key,
                                        @NotNull Function<Object, T> converter,
                                        @Nullable T defaultValue) {
        Object value = arguments.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return converter.apply(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Validates that a required argument is present.
     * 
     * @param arguments the arguments map
     * @param key the argument key
     * @throws IllegalArgumentException if the argument is not present
     */
    public static void requireArgument(@NotNull Map<String, Object> arguments, 
                                      @NotNull String key) {
        if (!arguments.containsKey(key) || arguments.get(key) == null) {
            throw new IllegalArgumentException("Required argument '" + key + "' is missing");
        }
    }
    
    /**
     * Validates that multiple required arguments are present.
     * 
     * @param arguments the arguments map
     * @param keys the argument keys to validate
     * @throws IllegalArgumentException if any required argument is missing
     */
    public static void requireArguments(@NotNull Map<String, Object> arguments, 
                                       @NotNull String... keys) {
        for (String key : keys) {
            requireArgument(arguments, key);
        }
    }
}

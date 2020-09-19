package utils;

/**
 *
 * @author Miodrag Spasic
 */
public class StringUtils {
    
    public static String capitalize(String input){
        if (input != null && !input.isEmpty()) {
            return input.substring(0, 1).toUpperCase() + input.substring(1);
        }
        return "Unknown";
    }
    
    public static String getNameFromUser(String input){
        String result = "";
        if (input != null && input.contains(".")) {
            for (String part : input.split("[.]")) {
                result = result.concat(capitalize(part)).concat(" ");
            }
            return result.trim();
        }
        return (input == null || input.isEmpty()) ? "Unknown" : input;
    }
}

package dev.ancaghenade.shipmentpicturelambdavalidator;

import java.util.regex.Pattern;

public class TextParser {

  public static String replaceSufix(String input, String replacement) {
    String[] parts = input.split("-");

    // Find the last UUID
    String lastUUID = "";
    for (int i = parts.length - 1; i >= 0; i--) {
      if (isUUID(parts[i])) {
        lastUUID = parts[i];
        break;
      }
    }

    // Extract the part before the last UUID
    String partBeforeLastUUID = input.substring(0, input.lastIndexOf(lastUUID) - 1);

    // Replace the part after the last UUID with a new string
    String result = partBeforeLastUUID + "-" + replacement;
    return result;
  }

  // Check if a string is a valid UUID
  public static boolean isUUID(String input) {
    String uuidPattern = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    return Pattern.matches(uuidPattern, input);
  }
}

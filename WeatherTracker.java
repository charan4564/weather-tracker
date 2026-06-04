import java.io.*;
import java.net.*;
import java.util.Scanner;

public class WeatherTracker {

    // Your API key from OpenWeatherMap (free)
    static final String API_KEY = "32e1c669ac13a04a0f8797c68800d8ae";
    static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("========================================");
        System.out.println("        WEATHER TRACKER APPLICATION     ");
        System.out.println("========================================");

        while (true) {
            System.out.println("\nOptions:");
            System.out.println("  1. Search weather for a city");
            System.out.println("  2. Exit");
            System.out.print("\nEnter your choice: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("2")) {
                System.out.println("\nGoodbye!");
                break;
            } else if (choice.equals("1")) {
                System.out.print("Enter city name: ");
                String city = scanner.nextLine().trim();
                if (!city.isEmpty()) {
                    fetchWeather(city);
                }
            } else {
                System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
        scanner.close();
    }

    // City ID map for cities that don't work by name
    static java.util.Map<String, String> cityIds = new java.util.HashMap<String, String>() {{
        //put("kurnool",   "1267995");
        put("kurnool",   "Kurnool,IN");
        put("hyderabad", "1269843");
        put("bengaluru", "1277333");
        put("bangalore", "1277333");
        put("mumbai",    "1275339");
        put("delhi",     "1273294");
        put("chennai",   "1264527");
        put("kolkata",   "1275004");
        put("pune",      "1259229");
    }};

    static void fetchWeather(String city) {
        try {
            // Check if city has a known ID (more reliable than name search)
            String cityLower = city.toLowerCase();
            String urlString;
            if (cityIds.containsKey(cityLower)) {
                urlString = BASE_URL + "?q=" + cityIds.get(cityLower)
                        + "&appid=" + API_KEY + "&units=metric";
            } else {
                urlString = BASE_URL + "?q=" + URLEncoder.encode(city, "UTF-8")
                        + "&appid=" + API_KEY + "&units=metric";
            }

            // Make HTTP request
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            // Read the response
            InputStream inputStream = (responseCode == 200)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String json = response.toString();

            if (responseCode == 200) {
                displayWeather(city, json);
            } else {
                System.out.println("\n  City \"" + city + "\" not found. Please check the spelling.");
            }

        } catch (Exception e) {
            System.out.println("\n  Error: Could not connect. Check your internet connection.");
        }
    }

    static void displayWeather(String city, String json) {
        // Parse values from JSON manually using simple string extraction
        String temperature   = extractValue(json, "\"temp\":");
        String feelsLike     = extractValue(json, "\"feels_like\":");
        String humidity      = extractValue(json, "\"humidity\":");
        String windSpeed     = extractValue(json, "\"speed\":");
        String description   = extractStringValue(json, "\"description\":");
        String countryCode   = extractStringValue(json, "\"country\":");

        // Convert wind from m/s to km/h
        double windKmh = 0;
        try { windKmh = Double.parseDouble(windSpeed) * 3.6; } catch (Exception e) {}

        System.out.println("\n========================================");
        System.out.println("  Weather in " + city.toUpperCase() + ", " + countryCode);
        System.out.println("========================================");
        System.out.println("  Condition    : " + capitalize(description));
        System.out.println("  Temperature  : " + temperature + " °C");
        System.out.println("  Feels Like   : " + feelsLike + " °C");
        System.out.println("  Humidity     : " + humidity + " %");
        System.out.printf ("  Wind Speed   : %.1f km/h%n", windKmh);
        System.out.println("========================================");
    }

    // Extract a numeric value from JSON like "temp":27.5
    static String extractValue(String json, String key) {
        try {
            int start = json.indexOf(key) + key.length();
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end))
                    || json.charAt(end) == '.' || json.charAt(end) == '-')) {
                end++;
            }
            return json.substring(start, end).trim();
        } catch (Exception e) {
            return "N/A";
        }
    }

    // Extract a string value from JSON like "description":"clear sky"
    static String extractStringValue(String json, String key) {
        try {
            int start = json.indexOf(key) + key.length();
            start = json.indexOf("\"", start) + 1;
            int end = json.indexOf("\"", start);
            return json.substring(start, end).trim();
        } catch (Exception e) {
            return "N/A";
        }
    }

    static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}

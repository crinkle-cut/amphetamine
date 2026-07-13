package net.mine_diver.amphetamine.util;

import com.google.gson.*;

public class JsonHelper {
    private static final Gson GSON = new Gson();

    public static JsonObject deserialize(java.io.Reader reader) {
        return GSON.fromJson(reader, JsonObject.class);
    }

    public static String getString(JsonObject json, String key) {
        if (!json.has(key))
            throw new JsonSyntaxException("Missing " + key + ", expected to find a string");
        return json.get(key).getAsString();
    }

    public static String getString(JsonObject json, String key, String defaultValue) {
        return json.has(key) ? json.get(key).getAsString() : defaultValue;
    }

    public static int getInt(JsonObject json, String key) {
        if (!json.has(key))
            throw new JsonSyntaxException("Missing " + key + ", expected to find an int");
        return json.get(key).getAsInt();
    }

    public static int getInt(JsonObject json, String key, int defaultValue) {
        return json.has(key) ? json.get(key).getAsInt() : defaultValue;
    }

    public static float getFloat(JsonObject json, String key) {
        if (!json.has(key))
            throw new JsonSyntaxException("Missing " + key + ", expected to find a float");
        return json.get(key).getAsFloat();
    }

    public static JsonArray getArray(JsonObject json, String key) {
        if (!json.has(key))
            throw new JsonSyntaxException("Missing " + key + ", expected to find an array");
        return json.get(key).getAsJsonArray();
    }

    public static JsonArray getArray(JsonObject json, String key, JsonArray defaultValue) {
        return json.has(key) ? json.get(key).getAsJsonArray() : defaultValue;
    }

    public static JsonObject getObject(JsonObject json, String key) {
        if (!json.has(key))
            throw new JsonSyntaxException("Missing " + key + ", expected to find an object");
        return json.get(key).getAsJsonObject();
    }

    public static JsonObject getObject(JsonObject json, String key, JsonObject defaultValue) {
        return json.has(key) ? json.get(key).getAsJsonObject() : defaultValue;
    }

    public static boolean hasString(JsonObject json, String key) {
        return json.has(key) && json.get(key).isJsonPrimitive() && json.get(key).getAsJsonPrimitive().isString();
    }

    public static JsonObject asObject(JsonElement element, String name) {
        if (!element.isJsonObject())
            throw new JsonSyntaxException("Expected " + name + " to be a JsonObject");
        return element.getAsJsonObject();
    }

    public static String asString(JsonElement element, String name) {
        if (!element.isJsonPrimitive())
            throw new JsonSyntaxException("Expected " + name + " to be a string");
        return element.getAsString();
    }

    public static float asFloat(JsonElement element, String name) {
        if (!element.isJsonPrimitive())
            throw new JsonSyntaxException("Expected " + name + " to be a float");
        return element.getAsFloat();
    }
}

package me.flame.menus.serializers.gson.adventure;

import com.google.gson.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

public class ComponentSerializer implements JsonSerializer<Component>, JsonDeserializer<Component> {
    @Override
    public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
        return getJsonObject(src);
    }

    private static @NotNull JsonObject getJsonObject(Component src) {
        JsonObject jsonObject = new JsonObject();

        // Serialize the message
        jsonObject.addProperty("message", src.toString());

        // Serialize the color
        TextColor color = src.color();
        if (color != null) jsonObject.addProperty("color", color.asHexString());

        // Serialize the decorations
        JsonArray decorations = new JsonArray();
        src.decorations().keySet().forEach((decoration) -> decorations.add(decoration.name()));
        jsonObject.add("decorations", decorations);

        List<Component> children = src.children();
        if (!children.isEmpty()) {
            JsonArray appendsJson = new JsonArray();
            children.forEach(child -> appendsJson.add(getJsonObject(child)));
            jsonObject.add("appends", appendsJson);
        }
        return jsonObject;
    }

    @Override
    public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return getResult(json).get();
    }

    private static @NotNull ComponentHolder getResult(JsonElement json) {
        JsonObject jsonObject = json.getAsJsonObject();
        String message = jsonObject.get("message").getAsString();

        TextColor color = TextColor.fromHexString(jsonObject.get("color").getAsString());
        ComponentHolder component = new ComponentHolder(Component.text(message).color(color));

        JsonArray array = jsonObject.getAsJsonArray("decorations");
        array.forEach(element -> component.set(component.get().decorate(TextDecoration.valueOf(element.getAsString()))));

        JsonArray list = jsonObject.getAsJsonArray("appends");
        if (!list.isJsonNull() && list.size() != 0) {
            list.forEach(element -> component.set(component.get().append(getResult(element).get())));
        }
        return component;
    }

    @AllArgsConstructor
    private static class ComponentHolder {
        Component component;
        
        void set(Component component) { this.component = component; }
        Component get() { return this.component; }
    }
}
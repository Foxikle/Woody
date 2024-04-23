package me.flame.menus.serializers.gson.adventure;

import com.google.gson.*;
import lombok.AllArgsConstructor;
import me.flame.menus.adventure.CompHolder;
import me.flame.menus.adventure.Lore;
import net.kyori.adventure.text.Component;

import java.lang.reflect.Type;

public class LoreSerializer implements JsonSerializer<Lore>, JsonDeserializer<Lore> {
    private static final JsonSerializer<Component> COMPONENT_JSON_SERIALIZER = new ComponentSerializer();

    @Override
    public Lore deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }

    @Override
    public JsonElement serialize(Lore src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        IntegerContainer container = new IntegerContainer(0);
        src.forEach((textHolder) -> {
            if (textHolder instanceof CompHolder && CompHolder.isNativeAdventureSupport()) {
                jsonObject.add("text" + container.number, COMPONENT_JSON_SERIALIZER.serialize(((CompHolder) textHolder).component(), typeOfSrc, context));
            } else {
                jsonObject.addProperty("text" + container.number, textHolder.toString());
            }
            container.number++;
        });
        return null;
    }

    @AllArgsConstructor
    private static class IntegerContainer {
        int number;
    }
}

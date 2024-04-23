package me.flame.menus.serializers.gson;

import com.google.gson.*;

import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.PaginatedMenu;
import me.flame.menus.menu.animation.Animation;
import me.flame.menus.modifiers.Modifier;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class PaginatedMenuSerializer implements JsonSerializer<PaginatedMenu>, JsonDeserializer<PaginatedMenu> {
    @Override
    public JsonElement serialize(PaginatedMenu menu, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("title", menu.title().toString());
        jsonObject.addProperty("type", Objects.requireNonNull(menu.getType()).name());
        jsonObject.addProperty("modifiers", menu.getModifiers().stream().map(Modifier::name).reduce((a, b) -> a + "," + b).orElse(""));
        jsonObject.addProperty("dynamicSizing", menu.isDynamicSizing());
        jsonObject.addProperty("hasAnimationsStarted", menu.hasAnimationsStarted());
        jsonObject.addProperty("rows", menu.rows());
        jsonObject.addProperty("size", menu.size());

        JsonArray pagesJson = new JsonArray();
        JsonArray dataJson = new JsonArray();
        menu.pages().forEach(page -> {
            pagesJson.add(jsonSerializationContext.serialize(page));

            List<MenuItem> list = menu.getItemList();
            list.forEach(item -> dataJson.add(jsonSerializationContext.serialize(item)));
        });


        jsonObject.add("data", dataJson);

        // Add the animations field
        JsonArray animationsJson = new JsonArray();
        for (Animation animation : menu.getAnimations()) animationsJson.add(jsonSerializationContext.serialize(animation));
        jsonObject.add("animations", animationsJson);

        return jsonObject;
    }

    @Override
    public PaginatedMenu deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }
}
package me.flame.menus.serializers.gson;

import com.google.gson.*;
import me.flame.menus.items.MenuItem;
import me.flame.menus.menu.IMenu;
import me.flame.menus.menu.animation.Animation;
import me.flame.menus.modifiers.Modifier;

import java.lang.reflect.Type;
import java.util.List;

public class MenuSerializer implements JsonSerializer<IMenu> {
    @Override
    public JsonElement serialize(IMenu menu, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("title", menu.title().toString());
        jsonObject.addProperty("type", menu.getType().name());
        jsonObject.addProperty("modifiers", menu.getModifiers().stream().map(Modifier::name).reduce((a, b) -> a + "," + b).orElse(""));
        jsonObject.addProperty("dynamicSizing", menu.isDynamicSizing());
        jsonObject.addProperty("hasAnimationsStarted", menu.hasAnimationsStarted());
        jsonObject.addProperty("rows", menu.rows());
        jsonObject.addProperty("size", menu.size());

        JsonArray dataJson = new JsonArray();
        List<MenuItem> list = menu.getItemList();
        list.forEach(item -> dataJson.add(jsonSerializationContext.serialize(item)));

        jsonObject.add("data", dataJson);

        // Add the animations field
        JsonArray animationsJson = new JsonArray();
        for (Animation animation : menu.getAnimations()) animationsJson.add(jsonSerializationContext.serialize(animation));
        jsonObject.add("animations", animationsJson);

        return jsonObject;
    }
}
package me.flame.menus.serializers.gson;

import com.google.gson.*;
import me.flame.menus.adventure.Lore;
import me.flame.menus.items.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class MenuItemSerializer implements JsonSerializer<MenuItem>, JsonDeserializer<MenuItem> {

    @Override
    public JsonElement serialize(MenuItem menuItem, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("uniqueId", menuItem.getUniqueId().toString());
        jsonObject.addProperty("type", menuItem.getType().name());
        jsonObject.addProperty("amount", menuItem.getItemStack().getAmount());

        // Add the meta field
        ItemMeta itemMeta = menuItem.getItemStack().getItemMeta();
        if (itemMeta != null) {
            JsonObject metaJson = new JsonObject();
            metaJson.addProperty("displayName", itemMeta.getDisplayName());


            metaJson.addProperty("lore", gson.toJsonTree(new Lore(itemMeta)));
            jsonObject.add("meta", metaJson);
        }
        jsonObject.addProperty("async", menuItem.isAsync());
        return jsonObject;
    }

    @Override
    public MenuItem deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        UUID uniqueId = UUID.fromString(jsonObject.get("uniqueId").getAsString());
        Material materialType = Material.valueOf(jsonObject.get("type").getAsString());
        int amount = jsonObject.get("amount").getAsInt();

        // Get the meta field
        ItemMeta itemMeta = null;
        if (jsonObject.has("meta")) {
            JsonObject metaJson = jsonObject.getAsJsonObject("meta");
            String displayName = metaJson.get("displayName").getAsString();
            List<String> lore = List.of(metaJson.get("lore").getAsString().split(","));
            itemMeta = Bukkit.getItemFactory().getItemMeta(materialType);

            if (itemMeta == null) {
                throw new IllegalArgumentException("TODO; but illegal");
            }

            itemMeta.setDisplayName(displayName);
            itemMeta.setLore(lore);
        }

        // Get the clickAction field
        JsonObject clickActionJson = jsonObject.getAsJsonObject("clickAction");
        boolean async = clickActionJson.get("async").getAsBoolean();


        ItemStack itemStack = new ItemStack(materialType, amount);
        if (itemMeta != null) {
            itemStack.setItemMeta(itemMeta);
        }

        return MenuItem.of(itemStack, null, uniqueId);
    }
}
package me.flame.menus.serializers.gson;

import com.google.gson.*;
import me.flame.menus.menu.animation.Animation;
import me.flame.menus.menu.animation.Frame;

import java.lang.reflect.Type;

public class AnimationSerializer implements JsonSerializer<Animation>, JsonDeserializer<Animation> {
    @Override
    public Animation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }

    @Override
    public JsonElement serialize(Animation src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("startDelay", src.getDelay());
        jsonObject.addProperty("repeatDelay", src.getRepeat());

        JsonArray array = new JsonArray();
        for (Frame frame : src.getFrames()) array.add(context.serialize(frame));
        jsonObject.add("frames", array);
        return null;
    }
}

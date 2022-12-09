package de.yuga.spacebattle.rest;

import com.google.gson.*;
import de.yuga.spacebattle.rest.dto.account.forum.ForumMessage;

import java.lang.reflect.Type;

public class ForumMessageAdapter implements JsonSerializer<ForumMessage> {

    @Override
    public JsonElement serialize(ForumMessage request, Type typeOfSrc, JsonSerializationContext context) {

        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("content", new JsonPrimitive("********"));
        return jsonObject;
    }
}

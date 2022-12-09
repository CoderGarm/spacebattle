package de.yuga.spacebattle.rest;

import com.google.gson.*;
import de.yuga.spacebattle.rest.dto.account.chat.ChatMessage;

import java.lang.reflect.Type;

public class ChatMessageAdapter implements JsonSerializer<ChatMessage> {

    @Override
    public JsonElement serialize(ChatMessage request, Type typeOfSrc, JsonSerializationContext context) {

        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("content", new JsonPrimitive("********"));
        return jsonObject;
    }
}

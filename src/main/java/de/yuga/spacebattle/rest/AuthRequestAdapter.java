package de.yuga.spacebattle.rest;

import com.google.gson.*;
import de.yuga.spacebattle.rest.dto.account.AuthRequest;

import java.lang.reflect.Type;

public class AuthRequestAdapter implements JsonSerializer<AuthRequest> {

    @Override
    public JsonElement serialize(AuthRequest request, Type typeOfSrc, JsonSerializationContext context) {

        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("username", new JsonPrimitive(request.getUsername()));
        jsonObject.add("password", new JsonPrimitive("******"));
        return jsonObject;
    }
}

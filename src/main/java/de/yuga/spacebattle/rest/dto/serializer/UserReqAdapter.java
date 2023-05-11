package de.yuga.spacebattle.rest.dto.serializer;

import com.google.gson.*;
import de.yuga.spacebattle.rest.dto.account.UserReq;

import java.lang.reflect.Type;

public class UserReqAdapter implements JsonSerializer<UserReq> {

    @Override
    public JsonElement serialize(UserReq request, Type typeOfSrc, JsonSerializationContext context) {

        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("username", new JsonPrimitive(request.getUsername()));
        jsonObject.add("password", new JsonPrimitive("******"));
        jsonObject.add("email", new JsonPrimitive(request.getEmail()));
        jsonObject.add("noEMailWanted", new JsonPrimitive(request.isNoEMailWanted()));
        return jsonObject;
    }
}

package de.yuga.spacebattle.rest;

import com.google.gson.*;
import de.yuga.spacebattle.rest.dto.enums.HasIcon;

import java.lang.reflect.Type;

public class HasIconAdapter implements JsonSerializer<HasIcon> {

    @Override
    public JsonElement serialize(HasIcon request, Type typeOfSrc, JsonSerializationContext context) {

        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("typeName", new JsonPrimitive(request.getTypeName()));
        return jsonObject;
    }
}

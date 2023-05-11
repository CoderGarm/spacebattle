package de.yuga.spacebattle.rest.dto.serializer;

import com.google.gson.*;
import de.yuga.spacebattle.rest.dto.enums.HasTypeName;

import java.lang.reflect.Type;

public class HasTypeNameAdapter implements JsonSerializer<HasTypeName> {

    @Override
    public JsonElement serialize(HasTypeName request, Type typeOfSrc, JsonSerializationContext context) {

        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("typeName", new JsonPrimitive(request.getTypeName()));
        return jsonObject;
    }
}

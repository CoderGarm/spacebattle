package de.yuga.spacebattle.rest.dto.serializer;

import com.google.gson.*;
import de.yuga.spacebattle.rest.dto.orbitals.StarSystem;

import java.lang.reflect.Type;

public class StarSystemAdapter implements JsonSerializer<StarSystem> {

    @Override
    public JsonElement serialize(StarSystem request, Type typeOfSrc, JsonSerializationContext context) {

        final JsonObject jsonObject = new JsonObject();
        jsonObject.add("name", new JsonPrimitive(request.getName()));
        return jsonObject;
    }
}

package de.yuga.spacebattle.rest.dto.serializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.yuga.spacebattle.rest.dto.misc.CoordsBlob;

import java.lang.reflect.Type;

public class CoordAdapter implements JsonSerializer<CoordsBlob> {

    @Override
    public JsonElement serialize(CoordsBlob request, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive("noop");
    }
}

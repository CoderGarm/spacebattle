package de.yuga.spacebattle.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.yuga.spacebattle.backend.services.MasterOfTheUniverseService;

import java.lang.reflect.Type;

public class CoordAdapter implements JsonSerializer<MasterOfTheUniverseService.CoordsBlob> {

    @Override
    public JsonElement serialize(MasterOfTheUniverseService.CoordsBlob request, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive("noop");
    }
}

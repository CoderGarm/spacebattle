package de.yuga.spacebattle.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReport;

import java.lang.reflect.Type;

public class BattleReportAdapter implements JsonSerializer<BattleReport> {

    @Override
    public JsonElement serialize(BattleReport request, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive("noop");
    }
}

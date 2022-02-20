package de.yuga.spacebattle.backend.converter;

import de.yuga.spacebattle.backend.combat.round.CombatRound;

import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;

public class CombatRoundConverter implements AttributeConverter<CombatRound, Integer> {

    @Override
    public Integer convertToDatabaseColumn(@Nullable final CombatRound attribute) {
        return attribute != null ? attribute.getNo() : null;
    }

    @Override
    public CombatRound convertToEntityAttribute(@Nullable final Integer dbData) {
        return dbData != null ? new CombatRound(dbData) : null;
    }
}

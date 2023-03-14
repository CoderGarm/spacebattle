package de.yuga.spacebattle.misc.fandom.spacecraft.dto;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public enum FieldName {
    Name("Name"),
    Typ("Typ"),
    Zugehoerigkeit("Zugehörigkeit"),
    Einfuehrung("Einführung"),
    Masse("Masse"),
    Laenge("Länge"),
    Breite("Breite"),
    Hoehe("Höhe"),
    Beschleunigung("Beschleunigung"),
    Crew("Crew"),
    Elektronik("Elektronik"),
    Bewaffnung("Bewaffnung"),
    Magazinladung("Magazinladung"),
    Beiboote("Beiboote");

    @Nonnull
    private final String fieldName;

    FieldName(@Nonnull final String fieldName) {
        this.fieldName = Preconditions.checkNotNull(fieldName, "fieldName must not be empty");
    }

    @Nonnull
    public String getFieldName() {
        return fieldName;
    }

    @Nullable
    public static FieldName getBy(@Nonnull final String fieldName) {
        Preconditions.checkNotNull(fieldName, "fieldName must not be empty");

        return Arrays.stream(FieldName.values()).filter(v -> v.fieldName.equals(fieldName)).findFirst().orElse(null);
    }
}

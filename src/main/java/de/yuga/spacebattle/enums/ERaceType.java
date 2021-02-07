package de.yuga.spacebattle.enums;

import org.checkerframework.checker.nullness.qual.NonNull;

public enum ERaceType {

    HUMAN("Mensch"),
    KANDORIAN("Kandorianer");

    @NonNull
    final String name;

    ERaceType(@NonNull final String name) {
        this.name = name;
    }

    public String getRaceName() {
        return name;
    }
}

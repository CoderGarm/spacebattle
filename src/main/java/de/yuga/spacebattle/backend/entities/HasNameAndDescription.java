package de.yuga.spacebattle.backend.entities;

import javax.annotation.Nonnull;

public interface HasNameAndDescription {

    @Nonnull
    String getName();

    @Nonnull
    String getDescription();
}

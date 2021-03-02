package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

public enum EIconType {

    WHITE("w"),
    BLACK("b"),
    COLOR("c"),
    COLOR2("c2");

    @Nonnull
    final String complement;

    EIconType(@Nonnull final String complement) {
        Preconditions.checkNotNull(complement, "complement shouldn't be null!");

        this.complement = complement;
    }

    @Nonnull
    public String getComplement() {
        return complement;
    }
}

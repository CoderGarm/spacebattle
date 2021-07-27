package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public enum EWebUserRole {

    USER("ROLE_USER");

    private final String name;

    EWebUserRole(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public static EWebUserRole getRoleByName(@Nonnull final String eWebUserRole) {
        Preconditions.checkNotNull(eWebUserRole, "eWebUserRole shouldn't be null!");

        return Arrays.stream(EWebUserRole.values()).filter(e -> e.getName().equals(eWebUserRole)).findFirst().get();
    }
}

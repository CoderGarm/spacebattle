package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public enum EGameUserRole {

    /**
     * The alliance admin - something like the chief of a tribe.
     */
    ALLIANCE_ADMIN("ALLIANCE_ADMIN"),
    WIKI_ADMIN("WIKI_ADMIN"),
    FORUM_READ("FORUM_READ"),
    FORUM_WRITE("FORUM_WRITE"),
    ;

    private final String name;

    EGameUserRole(@Nonnull final String name) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");

        this.name = name;
    }


    public String getName() {
        return this.name;
    }

    @Nullable
    public static EGameUserRole getRoleByName(@Nonnull final String eGameUserRole) {
        Preconditions.checkNotNull(eGameUserRole, "eGameUserRole shouldn't be null!");

        return Arrays.stream(EGameUserRole.values()).filter(e -> e.getName().equals(eGameUserRole)).findFirst().orElse(null);
    }
}

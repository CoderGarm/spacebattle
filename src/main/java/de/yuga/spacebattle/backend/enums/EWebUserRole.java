package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum EWebUserRole {

    USER("USER"),
    ADMIN("ADMIN", USER);

    private final String name;

    @Nonnull
    private final EWebUserRole[] allowedRoles;

    EWebUserRole(@Nonnull final String name) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");

        this.name = name;
        this.allowedRoles = new EWebUserRole[0];
    }

    EWebUserRole(@Nonnull final String name, @Nonnull final EWebUserRole... allowedRoles) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(allowedRoles, "allowedRoles shouldn't be null!");

        this.name = name;
        this.allowedRoles = allowedRoles;
    }

    public String getName() {
        return this.name;
    }

    public Set<EWebUserRole> getAllowedRoles() {
        final HashSet<EWebUserRole> objects = new HashSet<>();
        objects.add(this);
        objects.addAll(Arrays.asList(allowedRoles));
        return ImmutableSet.copyOf(objects);
    }

    @Nullable
    public static EWebUserRole getRoleByName(@Nonnull final String eWebUserRole) {
        Preconditions.checkNotNull(eWebUserRole, "eWebUserRole shouldn't be null!");

        return Arrays.stream(EWebUserRole.values()).filter(e -> e.getName().equals(eWebUserRole)).findFirst().get();
    }
}

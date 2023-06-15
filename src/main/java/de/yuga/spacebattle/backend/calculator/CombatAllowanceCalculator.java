package de.yuga.spacebattle.backend.calculator;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.Owner;

import javax.annotation.Nonnull;
import java.util.Set;

public class CombatAllowanceCalculator {

    private CombatAllowanceCalculator() {

    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isCombatAllowed(@Nonnull final Set<? extends Owner> owners) {
        Preconditions.checkNotNull(owners, "owners must not be empty");

        return owners.size() == 2;
    }
}

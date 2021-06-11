package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class ShipClassCreateDTO extends ShipClassEditDTO {

    private final static Logger LOGGER = LoggerFactory.getLogger(ShipClassEditDTO.class);

    @Nonnull
    private final Collection<Hull> possibleHulls = new HashSet<>();

    public ShipClassCreateDTO(@Nonnull final User user,
                              @Nonnull final List<Armor> allArmorByUser,
                              @Nonnull final List<ElectronicWarfare> allElectronicWarfareByUser,
                              @Nonnull final List<Propulsion> allPropulsionByUser,
                              @Nonnull final List<Sidewall> allSidewallByUser,
                              @Nonnull final List<Weapon> allWeaponByUser,
                              @Nonnull final List<AmmunitionModule> allAmmunitionModulesByUser,
                              @Nonnull final List<PassiveModule> allPassiveModuleByUser,
                              @Nonnull final List<Hull> allHullByUser) {
        super(user, allArmorByUser, allElectronicWarfareByUser, allPropulsionByUser, allSidewallByUser, allWeaponByUser, allAmmunitionModulesByUser, allPassiveModuleByUser);
        Preconditions.checkNotNull(allHullByUser, "allHullByUser shouldn't be null!");

        this.possibleHulls.addAll(allHullByUser);
    }

    @Nonnull
    public Collection<Hull> getPossibleHulls() {
        return possibleHulls;
    }

    public void setHull(@Nullable Hull hull) {
        super.setHull(hull);
    }

    public void setHulls(@Nullable final Collection<Hull> hulls) {
        if (hulls == null || hulls.isEmpty()) {
            LOGGER.info("hulls is empty");
            return;
        }
        hulls.stream().findFirst().ifPresent(this::setHull);
    }
}

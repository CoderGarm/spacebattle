package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AmmunitionFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.SupportFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class ShipClassEditDTO {

    private final static Logger LOGGER = LoggerFactory.getLogger(ShipClassEditDTO.class);

    @Nonnull
    private final User owner;

    @Nonnull
    private final Collection<Armor> possibleArmors = new HashSet<>();

    @Nonnull
    private final Collection<ElectronicWarfare> possibleElectronicWarfare = new HashSet<>();

    @Nonnull
    private final Collection<Propulsion> possiblePropulsion = new HashSet<>();

    @Nonnull
    private final Collection<Sidewall> possibleSidewalls = new HashSet<>();

    @Nonnull
    private final Collection<Weapon> possibleWeapons = new HashSet<>();

    @Nonnull
    private final Collection<AmmunitionModule> possibleAmmunitionModules = new HashSet<>();

    @Nonnull
    private final Collection<PassiveModule> possiblePassiveModule = new HashSet<>();

    @Nullable
    private Hull hull;

    @Nullable
    private String name;

    @Nullable
    private Armor selectedArmor;

    @Nullable
    private ElectronicWarfare selectedElectronicWarfare;

    @Nullable
    private Propulsion selectedPropulsion;

    @Nullable
    private Sidewall selectedSidewall;

    @Nonnull
    private final Set<AlignedFitting> selectedAlignedFittings = new HashSet<>();

    @Nonnull
    private final Set<AmmunitionFitting> selectedAmmunitionFittings = new HashSet<>();

    @Nonnull
    private final Set<SupportFitting> selectedSupportFittings = new HashSet<>();

    @Nullable
    private ShipClass predecessorShipClasses;

    /**
     * The super for {@link ShipClassCreateDTO}.
     */
    protected ShipClassEditDTO(@Nonnull final User owner,
                               @Nonnull final List<Armor> allArmorByUser,
                               @Nonnull final List<ElectronicWarfare> allElectronicWarfareByUser,
                               @Nonnull final List<Propulsion> allPropulsionByUser,
                               @Nonnull final List<Sidewall> allSidewallByUser,
                               @Nonnull final List<Weapon> allWeaponByUser,
                               @Nonnull final List<AmmunitionModule> allAmmunitionModulesByUser,
                               @Nonnull final List<PassiveModule> allPassiveModuleByUser) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");
        Preconditions.checkNotNull(allArmorByUser, "allArmorByUser shouldn't be null!");
        Preconditions.checkNotNull(allElectronicWarfareByUser, "allElectronicWarfareByUser shouldn't be null!");
        Preconditions.checkNotNull(allPropulsionByUser, "allPropulsionByUser shouldn't be null!");
        Preconditions.checkNotNull(allSidewallByUser, "allSidewallByUser shouldn't be null!");
        Preconditions.checkNotNull(allWeaponByUser, "allWeaponByUser shouldn't be null!");
        Preconditions.checkNotNull(allAmmunitionModulesByUser, "allAmmunitionModulesByUser shouldn't be null!");
        Preconditions.checkNotNull(allPassiveModuleByUser, "allPassiveModuleByUser shouldn't be null!");

        this.owner = owner;
        possibleArmors.addAll(allArmorByUser);
        possibleElectronicWarfare.addAll(allElectronicWarfareByUser);
        possiblePropulsion.addAll(allPropulsionByUser);
        possibleSidewalls.addAll(allSidewallByUser);
        possibleWeapons.addAll(allWeaponByUser);
        possibleAmmunitionModules.addAll(allAmmunitionModulesByUser);
        possiblePassiveModule.addAll(allPassiveModuleByUser);
        predecessorShipClasses = null;
    }

    public ShipClassEditDTO(@Nonnull final User owner,
                            @Nonnull final List<Armor> allArmorByUser,
                            @Nonnull final List<ElectronicWarfare> allElectronicWarfareByUser,
                            @Nonnull final List<Propulsion> allPropulsionByUser,
                            @Nonnull final List<Sidewall> allSidewallByUser,
                            @Nonnull final List<Weapon> allWeaponByUser,
                            @Nonnull final List<AmmunitionModule> allAmmunitionModulesByUser,
                            @Nonnull final List<PassiveModule> allPassiveModuleByUser,
                            @Nonnull final ShipClass shipClass) {
        this(owner, allArmorByUser, allElectronicWarfareByUser, allPropulsionByUser, allSidewallByUser, allWeaponByUser, allAmmunitionModulesByUser, allPassiveModuleByUser);

        this.name = shipClass.getName();
        this.hull = shipClass.getHull();
        selectedArmor = shipClass.getArmor();
        selectedElectronicWarfare = shipClass.getElectronicWarfare();
        selectedPropulsion = shipClass.getPropulsion();
        selectedSidewall = shipClass.getSidewall();
        selectedAlignedFittings.addAll(shipClass.getFittings());
        selectedAmmunitionFittings.addAll(shipClass.getAmmunitionFittings());
        selectedSupportFittings.addAll(shipClass.getSupportFittings());
        predecessorShipClasses = shipClass;
    }

    @Nonnull
    public Set<AlignedFitting> getFittings() {
        return selectedAlignedFittings;
    }

    public void resetModules() {
        name = null;
        hull = null;
        selectedArmor = null;
        selectedElectronicWarfare = null;
        selectedPropulsion = null;
        selectedSidewall = null;
        selectedAlignedFittings.clear();
        selectedAmmunitionFittings.clear();
        selectedSupportFittings.clear();
    }

    public void setHull(@Nullable Hull hull) {
        this.hull = hull;
    }

    @Nullable
    public Hull getHull() {
        return hull;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    /**
     * Creates a ship class to get the ship class from the create view with all the so far setted properties.
     *
     * @return the ship class
     */
    @Nonnull
    public ShipClass getShipClass() {
        final ShipClass shipClass;
        if (StringUtils.isNotBlank(name) && hull != null) {
            shipClass = new ShipClass(owner, name, hull, predecessorShipClasses);
        } else {
            shipClass = new ShipClass();
        }
        if (hull != null) {
            shipClass.setHull(hull);
        }
        shipClass.setArmor(selectedArmor);
        shipClass.setPropulsion(selectedPropulsion);
        shipClass.setElectronicWarfare(selectedElectronicWarfare);
        shipClass.setSidewall(selectedSidewall);
        // removing fittings with an amount of zero - this is necessary here to remove a fitting which was added before
        shipClass.setFittings(selectedAlignedFittings.stream().filter(a -> a.getAmount() > 0).collect(Collectors.toSet()));
        shipClass.setAmmunitionFittings(selectedAmmunitionFittings);
        shipClass.setSupportFittings(selectedSupportFittings);
        return shipClass;
    }

    /**
     * To set the user selection of fittings.
     *
     * @param alignedFittings the fittings to set
     */
    public void setFittings(@Nullable final Set<AlignedFitting> alignedFittings) {
        selectedAlignedFittings.clear();
        if (alignedFittings == null || alignedFittings.isEmpty()) {
            return;
        }
        selectedAlignedFittings.addAll(alignedFittings);
    }

    /**
     * To set the user selection of modules.
     *
     * @param modules the modules to set
     */
    public void setModules(@Nullable final ModuleContainerDTO modules) {

        if (modules == null) {
            return;
        }
        selectedArmor = modules.getSelectedArmor();
        selectedElectronicWarfare = modules.getSelectedElectronicWarfare();
        selectedPropulsion = modules.getSelectedPropulsion();
        selectedSidewall = modules.getSelectedSidewall();
        selectedAlignedFittings.addAll(modules.getSelectedAlignedFittings());
        selectedAmmunitionFittings.addAll(modules.getSelectedAmmunitionFittings());
        selectedSupportFittings.addAll(modules.getSelectedSupportFittings());
    }

    public ModuleContainerDTO getModules() {
        return new ModuleContainerDTO(new ArrayList<>(possibleArmors),
                new ArrayList<>(possibleElectronicWarfare),
                new ArrayList<>(possiblePropulsion),
                new ArrayList<>(possibleSidewalls),
                new ArrayList<>(possibleWeapons),
                new ArrayList<>(possibleAmmunitionModules),
                new ArrayList<>(possiblePassiveModule),
                selectedArmor,
                selectedElectronicWarfare,
                selectedPropulsion,
                selectedSidewall,
                selectedAlignedFittings,
                selectedAmmunitionFittings,
                selectedSupportFittings);
    }
}

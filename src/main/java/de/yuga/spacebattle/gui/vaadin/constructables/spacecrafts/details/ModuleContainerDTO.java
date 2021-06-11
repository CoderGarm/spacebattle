package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.AmmunitionFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.details.SupportFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModuleContainerDTO {

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
    private Armor selectedArmor;

    @Nullable
    private ElectronicWarfare selectedElectronicWarfare;

    @Nullable
    private Propulsion selectedPropulsion;

    @Nullable
    private Sidewall selectedSidewall;

    @Nonnull
    private Set<AlignedFitting> selectedAlignedFittings = new HashSet<>();

    @Nonnull
    private Set<AmmunitionFitting> selectedAmmunitionFittings = new HashSet<>();

    @Nonnull
    private Set<SupportFitting> selectedSupportFittings = new HashSet<>();

    public ModuleContainerDTO(@Nonnull final List<Armor> allArmorByUser,
                              @Nonnull final List<ElectronicWarfare> allElectronicWarfareByUser,
                              @Nonnull final List<Propulsion> allPropulsionByUser,
                              @Nonnull final List<Sidewall> allSidewallByUser,
                              @Nonnull final List<Weapon> allWeaponByUser,
                              @Nonnull final List<AmmunitionModule> allAmmunitionModulesByUser,
                              @Nonnull final List<PassiveModule> allPassiveModuleByUser,
                              @Nullable final Armor selectedArmor,
                              @Nullable final ElectronicWarfare selectedElectronicWarfare,
                              @Nullable final Propulsion selectedPropulsion,
                              @Nullable final Sidewall selectedSidewall,
                              @Nonnull final Set<AlignedFitting> selectedAlignedFittings,
                              @Nonnull final Set<AmmunitionFitting> selectedAmmunitionFittings,
                              @Nonnull final Set<SupportFitting> selectedSupportFittings) {
        Preconditions.checkNotNull(allArmorByUser, "allArmorByUser shouldn't be null!");
        Preconditions.checkNotNull(allElectronicWarfareByUser, "allElectronicWarfareByUser shouldn't be null!");
        Preconditions.checkNotNull(allPropulsionByUser, "allPropulsionByUser shouldn't be null!");
        Preconditions.checkNotNull(allSidewallByUser, "allSidewallByUser shouldn't be null!");
        Preconditions.checkNotNull(allWeaponByUser, "allWeaponByUser shouldn't be null!");
        Preconditions.checkNotNull(allAmmunitionModulesByUser, "allAmmunitionModulesByUser shouldn't be null!");
        Preconditions.checkNotNull(allPassiveModuleByUser, "allPassiveModuleByUser shouldn't be null!");
        Preconditions.checkNotNull(selectedAmmunitionFittings, "selectedAmmunitionFittings shouldn't be null!");
        Preconditions.checkNotNull(selectedSupportFittings, "selectedSupportFittings shouldn't be null!");

        possibleArmors.addAll(allArmorByUser);
        possibleElectronicWarfare.addAll(allElectronicWarfareByUser);
        possiblePropulsion.addAll(allPropulsionByUser);
        possibleSidewalls.addAll(allSidewallByUser);
        possibleWeapons.addAll(allWeaponByUser);
        possibleAmmunitionModules.addAll(allAmmunitionModulesByUser);
        possiblePassiveModule.addAll(allPassiveModuleByUser);

        this.selectedArmor = selectedArmor;
        this.selectedElectronicWarfare = selectedElectronicWarfare;
        this.selectedPropulsion = selectedPropulsion;
        this.selectedSidewall = selectedSidewall;
        this.selectedAlignedFittings.addAll(selectedAlignedFittings);
        this.selectedAmmunitionFittings.addAll(selectedAmmunitionFittings);
        this.selectedSupportFittings.addAll(selectedSupportFittings);
    }

    @Nonnull
    public Collection<Armor> getPossibleArmors() {
        return possibleArmors;
    }

    @Nonnull
    public Collection<ElectronicWarfare> getPossibleElectronicWarfare() {
        return possibleElectronicWarfare;
    }

    @Nonnull
    public Collection<Propulsion> getPossiblePropulsion() {
        return possiblePropulsion;
    }

    @Nonnull
    public Collection<Sidewall> getPossibleSidewalls() {
        return possibleSidewalls;
    }

    @Nonnull
    public Collection<Weapon> getPossibleWeapons() {
        return possibleWeapons;
    }

    @Nonnull
    public Collection<AmmunitionModule> getPossibleAmmunitionModules() {
        return possibleAmmunitionModules;
    }

    @Nonnull
    public Collection<PassiveModule> getPossiblePassiveModule() {
        return possiblePassiveModule;
    }

    @Nullable
    public Armor getSelectedArmor() {
        return selectedArmor;
    }

    public void setSelectedArmor(@Nullable Armor selectedArmor) {
        this.selectedArmor = selectedArmor;
    }

    @Nullable
    public ElectronicWarfare getSelectedElectronicWarfare() {
        return selectedElectronicWarfare;
    }

    public void setSelectedElectronicWarfare(@Nullable ElectronicWarfare selectedElectronicWarfare) {
        this.selectedElectronicWarfare = selectedElectronicWarfare;
    }

    @Nullable
    public Propulsion getSelectedPropulsion() {
        return selectedPropulsion;
    }

    public void setSelectedPropulsion(@Nullable Propulsion selectedPropulsion) {
        this.selectedPropulsion = selectedPropulsion;
    }

    @Nullable
    public Sidewall getSelectedSidewall() {
        return selectedSidewall;
    }

    public void setSelectedSidewall(@Nullable Sidewall selectedSidewall) {
        this.selectedSidewall = selectedSidewall;
    }

    @Nonnull
    public Set<AlignedFitting> getSelectedAlignedFittings() {
        return selectedAlignedFittings;
    }

    public void addSelectedAlignedFittings(@Nonnull Set<AlignedFitting> fittings) {
        Preconditions.checkNotNull(fittings, "fittings shouldn't be null!");

        selectedAlignedFittings.removeAll(fittings);
        selectedAlignedFittings.addAll(fittings);
    }

    /**
     * Sets the given set as new.
     *
     * @param fittings the new set
     */
    public void setSelectedAlignedFittings(@Nonnull Set<AlignedFitting> fittings) {
        Preconditions.checkNotNull(fittings, "fittings shouldn't be null!");

        selectedAlignedFittings = fittings;
    }

    @Nonnull
    public Set<AmmunitionFitting> getSelectedAmmunitionFittings() {
        return selectedAmmunitionFittings;
    }

    public void addSelectedAmmunitionFittings(@Nonnull Set<AmmunitionFitting> fittings) {
        Preconditions.checkNotNull(fittings, "fittings shouldn't be null!");

        selectedAmmunitionFittings.removeAll(fittings);
        selectedAmmunitionFittings.addAll(fittings);
    }

    /**
     * Sets the given set as new.
     *
     * @param fittings the new set
     */
    public void setSelectedAmmunitionFittings(@Nonnull Set<AmmunitionFitting> fittings) {
        Preconditions.checkNotNull(fittings, "fittings shouldn't be null!");

        selectedAmmunitionFittings = fittings;
    }

    @Nonnull
    public Set<SupportFitting> getSelectedSupportFittings() {
        return selectedSupportFittings;
    }

    public void addSelectedSupportFittings(@Nonnull Set<SupportFitting> fittings) {
        Preconditions.checkNotNull(fittings, "fittings shouldn't be null!");

        selectedSupportFittings.removeAll(fittings);
        selectedSupportFittings.addAll(fittings);
    }

    /**
     * Sets the given set as new.
     *
     * @param fittings the new set
     */
    public void setSelectedSupportFittings(@Nonnull Set<SupportFitting> fittings) {
        Preconditions.checkNotNull(fittings, "fittings shouldn't be null!");

        selectedSupportFittings = fittings;
    }


    @Deprecated(since = "the validation must be work on a better way, this seems strange")
    public void prepareShipClassValidation(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        shipClass.setArmor(selectedArmor);
        shipClass.setElectronicWarfare(selectedElectronicWarfare);
        shipClass.setPropulsion(selectedPropulsion);
        shipClass.setSidewall(selectedSidewall);
        shipClass.setFittings(selectedAlignedFittings);
        shipClass.setAmmunitionFittings(selectedAmmunitionFittings);
        shipClass.setSupportFittings(selectedSupportFittings);
    }
}

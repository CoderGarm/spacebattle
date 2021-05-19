package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details.AlignedFitting;
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

    public ModuleContainerDTO(@Nonnull final List<Armor> allArmorByUser,
                              @Nonnull final List<ElectronicWarfare> allElectronicWarfareByUser,
                              @Nonnull final List<Propulsion> allPropulsionByUser,
                              @Nonnull final List<Sidewall> allSidewallByUser,
                              @Nonnull final List<Weapon> allWeaponByUser,
                              @Nullable final Armor selectedArmor,
                              @Nullable final ElectronicWarfare selectedElectronicWarfare,
                              @Nullable final Propulsion selectedPropulsion,
                              @Nullable final Sidewall selectedSidewall,
                              @Nonnull final Set<AlignedFitting> selectedAlignedFittings) {
        Preconditions.checkNotNull(allArmorByUser, "allArmorByUser shouldn't be null!");
        Preconditions.checkNotNull(allElectronicWarfareByUser, "allElectronicWarfareByUser shouldn't be null!");
        Preconditions.checkNotNull(allPropulsionByUser, "allPropulsionByUser shouldn't be null!");
        Preconditions.checkNotNull(allSidewallByUser, "allSidewallByUser shouldn't be null!");
        Preconditions.checkNotNull(allWeaponByUser, "allWeaponByUser shouldn't be null!");

        possibleArmors.addAll(allArmorByUser);
        possibleElectronicWarfare.addAll(allElectronicWarfareByUser);
        possiblePropulsion.addAll(allPropulsionByUser);
        possibleSidewalls.addAll(allSidewallByUser);
        possibleWeapons.addAll(allWeaponByUser);

        this.selectedArmor = selectedArmor;
        this.selectedElectronicWarfare = selectedElectronicWarfare;
        this.selectedPropulsion = selectedPropulsion;
        this.selectedSidewall = selectedSidewall;
        this.selectedAlignedFittings.addAll(selectedAlignedFittings);
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

    public void addSelectedAlignedFittings(@Nonnull Set<AlignedFitting> selectedAlignedFittings) {
        Preconditions.checkNotNull(selectedAlignedFittings, "selectedAlignedFittings shouldn't be null!");

        this.selectedAlignedFittings.removeAll(selectedAlignedFittings);
        this.selectedAlignedFittings.addAll(selectedAlignedFittings);
    }

    /**
     * Sets the given set as new.
     *
     * @param selectedAlignedFittings the new set
     */
    public void setSelectedAlignedFittings(@Nonnull Set<AlignedFitting> selectedAlignedFittings) {
        Preconditions.checkNotNull(selectedAlignedFittings, "selectedAlignedFittings shouldn't be null!");

        this.selectedAlignedFittings = selectedAlignedFittings;
    }

    @Deprecated(since = "the validation must be work on a better way, this seems strange")
    public void prepareShipClassValidation(@Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        shipClass.setArmor(this.selectedArmor);
        shipClass.setElectronicWarfare(this.selectedElectronicWarfare);
        shipClass.setPropulsion(this.selectedPropulsion);
        shipClass.setSidewall(this.selectedSidewall);
        shipClass.setFittings(this.selectedAlignedFittings);

    }
}

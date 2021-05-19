package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.google.common.base.Preconditions;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.NotifySBUserException;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.details.AlignedFitting;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.ElectronicWarfare;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Propulsion;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Sidewall;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.ModuleContainerDTO;
import de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details.StarShipSvgHelper;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.WeaponAlignmentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ModuleMultiEdit extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ModuleMultiEdit, ModuleContainerDTO>, ModuleContainerDTO>, HasValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleMultiEdit.class);

    @Nonnull
    private final BaseModuleSelector armorSelection = new BaseModuleSelector("Armor selection");

    @Nonnull
    private final BaseModuleSelector propulsionSelection = new BaseModuleSelector("Propulsion selection");

    @Nonnull
    private final BaseModuleSelector electronicWarfareSelection = new BaseModuleSelector("Electronic warfare selection");

    @Nonnull
    private final BaseModuleSelector sidewallSelection = new BaseModuleSelector("Sidewall selection");

    @Nonnull
    private final StarShipSvgHelper starShipSvgHelper;

    @Nullable
    private ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ModuleMultiEdit, ModuleContainerDTO>> valueChangeListener;

    @Nonnull
    private final WeaponAlignmentMultiEdit bow = new WeaponAlignmentMultiEdit(EWeaponAlignment.BOW);

    @Nonnull
    private final WeaponAlignmentMultiEdit stern = new WeaponAlignmentMultiEdit(EWeaponAlignment.STERN);

    @Nonnull
    private final WeaponAlignmentMultiEdit broadsides = new WeaponAlignmentMultiEdit(EWeaponAlignment.BROADSIDE);

    /**
     * The original input data.
     */
    @Nullable
    private ModuleContainerDTO moduleCountDTO;

    public ModuleMultiEdit(@Nonnull final StarShipSvgHelper starShipSvgHelper) {
        Preconditions.checkNotNull(starShipSvgHelper, "starShipSvgHelper shouldn't be null!");

        this.starShipSvgHelper = starShipSvgHelper;
        setClassName("module-display");

        armorSelection.addValueChangeListener(event -> {
            final Armor value = new BaseModuleCaster<Armor>().getValueAsList(event.getValue());
            if (moduleCountDTO != null) {
                moduleCountDTO.setSelectedArmor(value);
            }
            fireChangeEvent();
        });

        propulsionSelection.addValueChangeListener(event -> {
            final Propulsion value = new BaseModuleCaster<Propulsion>().getValueAsList(event.getValue());
            if (moduleCountDTO != null) {
                moduleCountDTO.setSelectedPropulsion(value);
            }
            fireChangeEvent();
        });

        electronicWarfareSelection.addValueChangeListener(event -> {
            final ElectronicWarfare value = new BaseModuleCaster<ElectronicWarfare>().getValueAsList(event.getValue());
            if (moduleCountDTO != null) {
                moduleCountDTO.setSelectedElectronicWarfare(value);
            }
            fireChangeEvent();
        });

        sidewallSelection.addValueChangeListener(event -> {
            final Sidewall value = new BaseModuleCaster<Sidewall>().getValueAsList(event.getValue());
            if (moduleCountDTO != null) {
                moduleCountDTO.setSelectedSidewall(value);
            }
            fireChangeEvent();
        });

        bow.addValueChangeListener(event -> {
            final Set<AlignedFitting> collect = mapAlignmentDTO(event.getValue());
            if (moduleCountDTO != null) {
                moduleCountDTO.addSelectedAlignedFittings(collect);
            }
            fireChangeEvent();
        });

        stern.addValueChangeListener(event -> {
            final Set<AlignedFitting> collect = mapAlignmentDTO(event.getValue());
            if (moduleCountDTO != null) {
                moduleCountDTO.addSelectedAlignedFittings(collect);
            }
            fireChangeEvent();
        });

        broadsides.addValueChangeListener(event -> {
            final Set<AlignedFitting> collect = mapAlignmentDTO(event.getValue());
            if (moduleCountDTO != null) {
                moduleCountDTO.addSelectedAlignedFittings(collect);
            }
            fireChangeEvent();
        });

        add(armorSelection, propulsionSelection, electronicWarfareSelection, sidewallSelection, bow, stern, broadsides);
    }

    private void fireChangeEvent() {
        updateWeaponSlots();

        if (valueChangeListener != null) {
            final AbstractField.ComponentValueChangeEvent<ModuleMultiEdit, ModuleContainerDTO> changeEvent =
                    new AbstractField.ComponentValueChangeEvent<>(this, this, getValue(), true);
            valueChangeListener.valueChanged(changeEvent);
        }
    }

    private void updateWeaponSlots() {
        final Set<WeaponAlignmentDTO> bowWeapons = bow.getValue();
        final Set<WeaponAlignmentDTO> sternWeapons = stern.getValue();
        final Set<WeaponAlignmentDTO> broadsideWeapons = broadsides.getValue();

        bowWeapons.stream().map(WeaponAlignmentDTO::getCountNumeric).reduce(Integer::sum).ifPresent(sum -> starShipSvgHelper.calculateBowSlots(3, sum));
        broadsideWeapons.stream().map(WeaponAlignmentDTO::getCountNumeric).reduce(Integer::sum).ifPresent(sum -> starShipSvgHelper.calculateBroadsideSlots(3, sum));
        sternWeapons.stream().map(WeaponAlignmentDTO::getCountNumeric).reduce(Integer::sum).ifPresent(sum -> starShipSvgHelper.calculateSternSlots(3, sum));
    }

    @Override
    public void setValue(@Nullable final ModuleContainerDTO value) {
        if (moduleCountDTO == null) {
            moduleCountDTO = value;
        }
        if (value == null) {
            getChildren().filter(c -> c instanceof HasValue).forEach(c -> ((HasValue) c).clear());
            starShipSvgHelper.calculateBowSlots(3, 0);
            starShipSvgHelper.calculateBroadsideSlots(4, 0);
            starShipSvgHelper.calculateSternSlots(3, 0);
            return;
        }

        final Collection<BaseModule> possibleArmors = getCollectionAsBaseModule(value.getPossibleArmors());
        armorSelection.setValue(possibleArmors);
        final Armor armor = value.getSelectedArmor();
        if (armor != null) {
            armorSelection.preselect(armor);
        }

        final Collection<BaseModule> possiblePropulsion = getCollectionAsBaseModule(value.getPossiblePropulsion());
        propulsionSelection.setValue(possiblePropulsion);
        final Propulsion propulsion = value.getSelectedPropulsion();
        if (propulsion != null) {
            propulsionSelection.preselect(propulsion);
        }

        final Collection<BaseModule> possibleElectronicWarfare = getCollectionAsBaseModule(value.getPossibleElectronicWarfare());
        electronicWarfareSelection.setValue(possibleElectronicWarfare);
        final ElectronicWarfare electronicWarfare = value.getSelectedElectronicWarfare();
        if (electronicWarfare != null) {
            electronicWarfareSelection.preselect(electronicWarfare);
        }

        final Collection<BaseModule> possibleSidewalls = getCollectionAsBaseModule(value.getPossibleSidewalls());
        sidewallSelection.setValue(possibleSidewalls);
        final Sidewall sidewall = value.getSelectedSidewall();
        if (sidewall != null) {
            sidewallSelection.preselect(sidewall);
        }

        final Set<WeaponAlignmentDTO> bowValue = value.getPossibleWeapons().stream()
                .filter(a -> a.getAllowedWeaponAlignments().contains(EWeaponAlignment.BOW))
                .map(a -> {
                    final AlignedFitting alignedFitting = value.getSelectedAlignedFittings().stream()
                            .filter(e -> e.getWeapon().equals(a) && EWeaponAlignment.BOW == e.getWeaponAlignment())
                            .findFirst()
                            .orElse(null);
                    final int amount = alignedFitting != null ? alignedFitting.getAmount() : 0;
                    return new WeaponAlignmentDTO(a, amount);
                }).collect(Collectors.toSet());

        final Set<WeaponAlignmentDTO> sternValue = value.getPossibleWeapons().stream()
                .filter(a -> a.getAllowedWeaponAlignments().contains(EWeaponAlignment.STERN))
                .map(a -> {
                    final AlignedFitting alignedFitting = value.getSelectedAlignedFittings().stream()
                            .filter(e -> e.getWeapon().equals(a) && EWeaponAlignment.STERN == e.getWeaponAlignment())
                            .findFirst()
                            .orElse(null);
                    final int amount = alignedFitting != null ? alignedFitting.getAmount() : 0;
                    return new WeaponAlignmentDTO(a, amount);
                }).collect(Collectors.toSet());


        final Set<WeaponAlignmentDTO> broadsideValue = value.getPossibleWeapons().stream()
                .filter(a -> a.getAllowedWeaponAlignments().contains(EWeaponAlignment.BROADSIDE))
                .map(a -> {
                    final AlignedFitting alignedFitting = value.getSelectedAlignedFittings().stream()
                            .filter(e -> e.getWeapon().equals(a) && EWeaponAlignment.BROADSIDE == e.getWeaponAlignment())
                            .findFirst()
                            .orElse(null);
                    final int amount = alignedFitting != null ? alignedFitting.getAmount() : 0;
                    return new WeaponAlignmentDTO(a, amount);
                }).collect(Collectors.toSet());

        bow.setValue(bowValue);
        stern.setValue(sternValue);
        broadsides.setValue(broadsideValue);
        updateWeaponSlots();
    }

    @Nonnull
    @Override
    public ModuleContainerDTO getValue() {
        if (moduleCountDTO == null) {
            throw new NotifySBUserException("You should call getValue before setting a value.");
        }

        final Set<WeaponAlignmentDTO> alignedFittings = bow.getValue();
        alignedFittings.addAll(stern.getValue());
        alignedFittings.addAll(broadsides.getValue());

        final Set<AlignedFitting> selectedFittings = mapAlignmentDTO(alignedFittings);
        moduleCountDTO.setSelectedAlignedFittings(selectedFittings);
        return moduleCountDTO;
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ModuleMultiEdit, ModuleContainerDTO>> listener) {

        valueChangeListener = listener;
        return (Registration) () -> valueChangeListener = null;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        // todo wofür wird das genutzt?
        getChildren().filter(c -> c instanceof HasValue).forEach(c -> ((HasValue) c).setReadOnly(readOnly));
    }

    @Override
    public boolean isReadOnly() {
        // todo wofür wird das genutzt?
        final long count = getChildren().filter(c -> c instanceof HasValue).filter(c -> !((HasValue) c).isReadOnly()).count();
        return count <= 0;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return false;
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        LOGGER.info("error message: " + errorMessage);
    }

    @Override
    public String getErrorMessage() {
        // not necessary
        return null;
    }

    @Override
    public void setInvalid(boolean invalid) {
    }

    @Override
    public boolean isInvalid() {
        return false;
    }

    @Nonnull
    private Set<BaseModule> getCollectionAsBaseModule(@Nonnull final Collection<? extends BaseModule> collection) {
        Preconditions.checkNotNull(collection, "collection shouldn't be null!");

        return collection.stream().map(BaseModule.class::cast).collect(Collectors.toSet());
    }

    @Nonnull
    private Set<AlignedFitting> mapAlignmentDTO(@Nonnull final Set<WeaponAlignmentDTO> value) {
        Preconditions.checkNotNull(value, "value shouldn't be null!");

        return value.stream()
                .filter(a -> a.getSelectedWeaponAlignment() != null)
                .map(a -> new AlignedFitting(a.getSelectedWeaponAlignment(), a.getWeapon(), a.getCountNumeric()))
                .collect(Collectors.toSet());
    }

    /**
     * Some kind of hack until the module selector is more specific to the module type.
     *
     * @param <T> the class which should casted into
     */
    @Deprecated(since = "Module edit is more specific")
    private static class BaseModuleCaster<T extends BaseModule> {

        private T getValueAsList(@Nonnull final Collection<BaseModule> value) {
            Preconditions.checkNotNull(value, "value shouldn't be null!");

            final List<BaseModule> baseModules = new ArrayList<>(value);
            if (baseModules.isEmpty()) {
                return null;
            }
            return (T) baseModules.get(0);
        }
    }
}

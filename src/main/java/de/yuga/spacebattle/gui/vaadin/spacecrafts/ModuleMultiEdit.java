package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.ModuleAmountWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ModuleMultiEdit extends VerticalLayout implements HasValue<AbstractField.ComponentValueChangeEvent<ModuleMultiEdit, Map<Module, Integer>>, Map<Module, Integer>>, HasValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleMultiEdit.class);

    @Nonnull
    private final Map<Module, ModuleAmountEdit> componentMap = new HashMap<>();

    @Nullable
    private ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ModuleMultiEdit, Map<Module, Integer>>> listener;

    public ModuleMultiEdit() {
    }

    /**
     * Will remove not longer existent displays, add new displays or update displays which are already part of this multi display.
     *
     * @param modules a map of modules
     */
    public void update(@Nullable final Map<Module, Integer> modules) {
        if (modules == null) {
            ModuleAmountEdit[] moduleDisplaysArray = new ModuleAmountEdit[componentMap.values().size()];
            componentMap.values().toArray(moduleDisplaysArray);
            remove(moduleDisplaysArray);
            componentMap.clear();
            return;
        }
        componentMap.forEach((module, moduleDisplay) -> {
            if (!modules.containsKey(module)) {
                remove(moduleDisplay);
            }
        });

        modules.forEach((module, amount) -> {
            ModuleAmountEdit moduleEdit = componentMap.get(module);
            if (moduleEdit != null) {
                moduleEdit.update(new ModuleAmountWrapper(module, amount));
            } else {
                moduleEdit = new ModuleAmountEdit();
                if (module.getModuleType().isMandatory()) {
                    moduleEdit.setRequiredIndicatorVisible(true);
                }
                moduleEdit.update(new ModuleAmountWrapper(module, amount));
                componentMap.put(module, moduleEdit);
                final ModuleMultiEdit moduleMultiEdit = this;
                ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ModuleAmountEdit, ModuleAmountWrapper>> valueChangeListener = new ValueChangeListener() {
                    @Override
                    public void valueChanged(ValueChangeEvent event) { // todo this is shitty shit shit
                        Map<Module, Integer> collect = componentMap.values().stream().collect(Collectors.toMap(o -> o.getValue().getModule(), o -> o.getValue().getAmountNumeric()));
                        final AbstractField.ComponentValueChangeEvent<ModuleMultiEdit, Map<Module, Integer>> changeEvent =
                                new AbstractField.ComponentValueChangeEvent<ModuleMultiEdit, Map<Module, Integer>>(moduleMultiEdit, moduleMultiEdit, collect, false);
                        listener.valueChanged(changeEvent);
                    }
                };
                moduleEdit.addValueChangeListener(valueChangeListener);
                add(moduleEdit);
            }
        });
    }

    @Nonnull
    public Collection<ModuleAmountWrapper> getModules() {
        return componentMap.keySet().stream().map(module -> componentMap.get(module).getValue()).collect(Collectors.toList());
    }

    @Override
    public void setValue(@Nullable final Map<Module, Integer> value) {
        this.update(value);
    }

    @Nonnull
    @Override
    public Map<Module, Integer> getValue() {
        return getModules().stream().map(ModuleAmountWrapper::getAsEntry).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ModuleMultiEdit, Map<Module, Integer>>> listener) {

        this.listener = listener;
        return new Registration() {
            @Override
            public void remove() {

            }
        };
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        componentMap.values().forEach(moduleAmountEdit -> moduleAmountEdit.setReadOnly(readOnly));
    }

    @Override
    public boolean isReadOnly() {
        final long count = componentMap.values().stream().filter(moduleAmountEdit -> !moduleAmountEdit.isReadOnly()).count();
        return count <= 0;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        componentMap.values().forEach(moduleAmountEdit -> moduleAmountEdit.setRequiredIndicatorVisible(requiredIndicatorVisible));
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        final long count = componentMap.values().stream().filter(moduleAmountEdit -> !moduleAmountEdit.isRequiredIndicatorVisible()).count();
        return count <= 0;
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        LOGGER.info(errorMessage);
    }

    @Override
    public String getErrorMessage() {
        // not necessary
        return null;
    }

    @Override
    public void setInvalid(boolean invalid) {
        componentMap.values().forEach(moduleAmountEdit -> moduleAmountEdit.setInvalid(invalid));
    }

    @Override
    public boolean isInvalid() {
        final long count = componentMap.values().stream().filter(moduleAmountEdit -> !moduleAmountEdit.isInvalid()).count();
        return count <= 0;
    }
}

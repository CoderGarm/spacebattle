package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import de.yuga.spacebattle.gui.vaadin.spacecrafts.details.ModuleCountWrapper;
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
    private final Map<Module, ModuleCountEdit> componentMap = new HashMap<>();

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
            ModuleCountEdit[] moduleDisplaysArray = new ModuleCountEdit[componentMap.values().size()];
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
            ModuleCountEdit moduleEdit = componentMap.get(module);
            if (moduleEdit != null) {
                moduleEdit.update(new ModuleCountWrapper(module, amount));
            } else {
                moduleEdit = new ModuleCountEdit();
                if (module.getModuleType().isMandatory()) {
                    moduleEdit.setRequiredIndicatorVisible(true);
                }
                moduleEdit.update(new ModuleCountWrapper(module, amount));
                componentMap.put(module, moduleEdit);
                final ModuleMultiEdit moduleMultiEdit = this;
                ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ModuleCountEdit, ModuleCountWrapper>> valueChangeListener = new ValueChangeListener() {
                    @Override
                    public void valueChanged(ValueChangeEvent event) { // todo this is shitty shit shit
                        Map<Module, Integer> collect = componentMap.values().stream().collect(Collectors.toMap(o -> o.getValue().getModule(), o -> o.getValue().getCountNumeric()));
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
    public Collection<ModuleCountWrapper> getModules() {
        return componentMap.keySet().stream().map(module -> componentMap.get(module).getValue()).collect(Collectors.toList());
    }

    @Override
    public void setValue(@Nullable final Map<Module, Integer> value) {
        this.update(value);
    }

    @Nonnull
    @Override
    public Map<Module, Integer> getValue() {
        return getModules().stream().map(ModuleCountWrapper::getAsEntry).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
        componentMap.values().forEach(moduleCountEdit -> moduleCountEdit.setReadOnly(readOnly));
    }

    @Override
    public boolean isReadOnly() {
        final long count = componentMap.values().stream().filter(moduleCountEdit -> !moduleCountEdit.isReadOnly()).count();
        return count <= 0;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        componentMap.values().forEach(moduleCountEdit -> moduleCountEdit.setRequiredIndicatorVisible(requiredIndicatorVisible));
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        final long count = componentMap.values().stream().filter(moduleCountEdit -> !moduleCountEdit.isRequiredIndicatorVisible()).count();
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
        componentMap.values().forEach(moduleCountEdit -> moduleCountEdit.setInvalid(invalid));
    }

    @Override
    public boolean isInvalid() {
        final long count = componentMap.values().stream().filter(moduleCountEdit -> !moduleCountEdit.isInvalid()).count();
        return count <= 0;
    }
}

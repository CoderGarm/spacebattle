package de.yuga.spacebattle.gui.vaadin.spacecrafts;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ModuleMultiDisplay extends VerticalLayout {

    @Nonnull
    private final Map<Module, ModuleDisplay> componentMap = new HashMap<>();

    public ModuleMultiDisplay() {
    }

    /**
     * Will remove not longer existent displays, add new displays or update displays which are already part of this multi display.
     * Or simply clear the full view if the map is null.
     *
     * @param modules a map of modules
     */
    public void update(@Nullable final Map<Module, Integer> modules) {
        if (modules == null) {
            ModuleDisplay[] moduleDisplaysArray = new ModuleDisplay[componentMap.values().size()];
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
            ModuleDisplay moduleDisplay1 = componentMap.get(module);
            if (moduleDisplay1 != null) {
                moduleDisplay1.update(module, amount);
            } else {
                ModuleDisplay moduleDisplay = new ModuleDisplay();
                moduleDisplay.update(module, amount);
                componentMap.put(module, moduleDisplay);
                add(moduleDisplay);
            }
        });
    }
}

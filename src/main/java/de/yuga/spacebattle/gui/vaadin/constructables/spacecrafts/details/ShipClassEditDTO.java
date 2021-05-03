package de.yuga.spacebattle.gui.vaadin.constructables.spacecrafts.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.spacecrafts.Hull;
import de.yuga.spacebattle.backend.entities.spacecrafts.Module;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShipClassEditDTO {

    private final static Logger LOGGER = LoggerFactory.getLogger(ShipClassEditDTO.class);

    @Nonnull
    private final User owner;

    private int id = -1;

    /**
     * Holds the modules which are possible.
     * Every integer bigger than 0 means a user selected upgrade and only these ones should be computed.
     */
    @Nonnull
    private final Map<Module, Integer> modules = new HashMap<>();

    @Nullable
    private Hull hull;

    @Nullable
    private String name;

    protected ShipClassEditDTO(@Nonnull final User owner,
                               @Nonnull final Map<Module, Integer> modules) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");
        Preconditions.checkNotNull(modules, "modules shouldn't be null!");

        this.owner = owner;
        this.modules.putAll(modules);
    }

    public ShipClassEditDTO(@Nonnull final User owner,
                            @Nonnull final List<Module> moduleList,
                            @Nonnull final ShipClass shipClass) {
        Preconditions.checkNotNull(owner, "owner shouldn't be null!");
        Preconditions.checkNotNull(moduleList, "moduleList shouldn't be null!");
        Preconditions.checkNotNull(shipClass, "shipClass shouldn't be null!");

        final Map<Module, Integer> availableModules = moduleList.stream().collect(Collectors.toMap(o -> o, o -> 0));
        final Map<Module, Integer> modulesInShipClass = shipClass.getModules();
        availableModules.putAll(modulesInShipClass);

        this.id = shipClass.getId();
        this.owner = owner;
        this.name = shipClass.getName();
        this.hull = shipClass.getHull();
        this.modules.putAll(availableModules);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Nonnull
    public Map<Module, Integer> getModules() {
        return modules;
    }

    public void resetModules() {
        final Map<Module, Integer> availableModules = modules.keySet().stream().collect(Collectors.toMap(o -> o, o -> 0));
        modules.putAll(availableModules);
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

    @Nonnull
    public ShipClass getShipClass() {
        final ShipClass shipClass;
        if (StringUtils.isNotBlank(name) && hull != null) {
            shipClass = new ShipClass(owner, name, hull);
        } else {
            shipClass = new ShipClass();
        }
        if (hull != null) {
            shipClass.setHull(hull);
        }
        if (id != -1) {
            shipClass.setId(id);
        }
        final Map<Module, Integer> userSelectedModules = modules.entrySet().stream().filter(moduleIntegerEntry -> moduleIntegerEntry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        shipClass.setModules(userSelectedModules);
        return shipClass;
    }

    public void setModules(@Nullable final Map<Module, Integer> moduleIntegerMap) {
        if (moduleIntegerMap == null || moduleIntegerMap.isEmpty()) {
            resetModules();
            return;
        }
        modules.putAll(moduleIntegerMap);
    }
}

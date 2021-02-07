package de.yuga.spacebattle.logic.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.entities.researches.Research;
import de.yuga.spacebattle.entities.spacecrafts.Module;
import de.yuga.spacebattle.enums.EModuleType;
import de.yuga.spacebattle.enums.ERaceType;
import de.yuga.spacebattle.repositories.spacecraft.ModuleRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Service
public class ModuleService {

    @Nonnull
    private final ModuleRepository moduleRepository;

    public ModuleService(@Nonnull final ModuleRepository moduleRepository) {
        Preconditions.checkNotNull(moduleRepository, "moduleRepository shouldn't be null!");

        this.moduleRepository = moduleRepository;
    }

    @Nonnull
    public List<Module> findAll() {
        return moduleRepository.findAllModules();
    }

    @Nullable
    public Module find(@Nonnull final Integer idModule) {
        Preconditions.checkNotNull(idModule, "idModule shouldn't be null!");
        return moduleRepository.findById(idModule).orElse(null);
    }

    /**
     * Creates a new {@link Module}.
     *
     * @param name            the name of the research
     * @param moduleType      the type of {@link EModuleType}
     * @param description     the description
     * @param useCapacity     the amount of construction capacity used
     * @param value           the base effect value, e.g. damage, compare {@link EModuleType}
     * @param level           the level of this module (e.g. laser Mk I, laser Mk II) which increases the effective value {@link Module#getEffectiveValue(ERaceType)}
     * @param unlockedThrough the research to unlock this module
     * @return the new module
     */
    @Nonnull
    public Module createModule(@Nonnull final String name,
                               @Nonnull final EModuleType moduleType,
                               @Nonnull final String description,
                               final int useCapacity,
                               final int value,
                               final int level,
                               @Nonnull final Research unlockedThrough) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(moduleType, "moduleType shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");

        return moduleRepository.save(new Module(name, moduleType, description, useCapacity, value, level, unlockedThrough));
    }
}

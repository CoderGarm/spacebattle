package de.yuga.spacebattle.backend.services.spacecraft;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import de.yuga.spacebattle.backend.enums.EDamageType;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import de.yuga.spacebattle.backend.repositories.spacecraft.*;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@Service
public class ModuleService {

    @Nonnull
    private final ArmorRepository armorRepository;

    @Nonnull
    private final WeaponRepository weaponRepository;

    @Nonnull
    private final SidewallRepository sidewallRepository;

    @Nonnull
    private final PropulsionRepository propulsionRepository;

    @Nonnull
    private final ElectronicWarfareRepository electronicWarfareRepository;

    public ModuleService(@Nonnull final ArmorRepository armorRepository,
                         @Nonnull final WeaponRepository weaponRepository,
                         @Nonnull final SidewallRepository sidewallRepository,
                         @Nonnull final PropulsionRepository propulsionRepository,
                         @Nonnull final ElectronicWarfareRepository electronicWarfareRepository) {
        Preconditions.checkNotNull(armorRepository, "armorRepository shouldn't be null!");
        Preconditions.checkNotNull(weaponRepository, "weaponRepository shouldn't be null!");
        Preconditions.checkNotNull(sidewallRepository, "sidewallRepository shouldn't be null!");
        Preconditions.checkNotNull(propulsionRepository, "propulsionRepository shouldn't be null!");
        Preconditions.checkNotNull(electronicWarfareRepository, "electronicWarfareRepository shouldn't be null!");

        this.armorRepository = armorRepository;
        this.weaponRepository = weaponRepository;
        this.sidewallRepository = sidewallRepository;
        this.propulsionRepository = propulsionRepository;
        this.electronicWarfareRepository = electronicWarfareRepository;
    }


    /**
     * Creates a new {@link Armor}.
     *
     * @param name            the name of the research
     * @param description     the description
     * @param useCapacity     the amount of construction capacity used
     * @param value           the base effect value, e.g. damage
     * @param techLevel       the techLevel of this module
     * @param unlockedThrough the research to unlock this module
     * @return the new module
     */
    @Nonnull
    @Deprecated(since = "productive environment")
    public Armor createArmor(@Nonnull final String name,
                             @Nonnull final String description,
                             @Nonnull final Research unlockedThrough,
                             final int useCapacity,
                             final int value,
                             final int techLevel) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");

        return armorRepository.save(new Armor(name, description, unlockedThrough, useCapacity, value, techLevel));
    }

    /**
     * Creates a new {@link ElectronicWarfare}.
     *
     * @param name            the name of the research
     * @param description     the description
     * @param useCapacity     the amount of construction capacity used
     * @param value           the base effect value, e.g. damage
     * @param techLevel       the techLevel of this module
     * @param unlockedThrough the research to unlock this module
     * @return the new module
     */
    @Nonnull
    @Deprecated(since = "productive environment")
    public ElectronicWarfare createElectronicWarfare(@Nonnull final String name,
                                                     @Nonnull final String description,
                                                     @Nonnull final Research unlockedThrough,
                                                     final int useCapacity,
                                                     final int value,
                                                     final int techLevel) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");

        return electronicWarfareRepository.save(new ElectronicWarfare(name, description, unlockedThrough, useCapacity, value, techLevel));
    }

    /**
     * Creates a new {@link Sidewall}.
     *
     * @param name            the name of the research
     * @param description     the description
     * @param useCapacity     the amount of construction capacity used
     * @param value           the base effect value, e.g. damage
     * @param techLevel       the techLevel of this module
     * @param unlockedThrough the research to unlock this module
     * @return the new module
     */
    @Nonnull
    @Deprecated(since = "productive environment")
    public Sidewall createSidewall(@Nonnull final String name,
                                   @Nonnull final String description,
                                   @Nonnull final Research unlockedThrough,
                                   final int useCapacity,
                                   final int value,
                                   final int techLevel) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");

        return sidewallRepository.save(new Sidewall(name, description, unlockedThrough, useCapacity, value, techLevel));
    }

    /**
     * Creates a new {@link Weapon}.
     *
     * @param name            the name of the research
     * @param description     the description
     * @param useCapacity     the amount of construction capacity used
     * @param value           the base effect value, e.g. damage
     * @param techLevel       the techLevel of this module
     * @param unlockedThrough the research to unlock this module
     * @return the new module
     */
    @Nonnull
    @Deprecated(since = "productive environment")
    public Weapon createWeapon(@Nonnull final String name,
                               @Nonnull final String description,
                               @Nonnull final Research unlockedThrough,
                               final int useCapacity,
                               final int value,
                               final int techLevel,
                               final int range,
                               @Nullable final Double sideWallPenetration,
                               @Nonnull final EDamageType damageType,
                               @Nonnull final EWeaponType weaponType,
                               @Nonnull final Set<EWeaponAlignment> allowedWeaponAlignments) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");
        Preconditions.checkNotNull(damageType, "damageType shouldn't be null!");
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");
        Preconditions.checkNotNull(allowedWeaponAlignments, "allowedWeaponAlignments shouldn't be null!");

        return weaponRepository.save(new Weapon(name, description, unlockedThrough, useCapacity, value, techLevel, range, sideWallPenetration, damageType, weaponType, allowedWeaponAlignments));
    }

    /**
     * Creates a new {@link Propulsion}.
     *
     * @param name            the name of the research
     * @param description     the description
     * @param useCapacity     the amount of construction capacity used
     * @param value           the base effect value, e.g. damage
     * @param level           the level of this module
     * @param unlockedThrough the research to unlock this module
     * @return the new module
     */
    @Nonnull
    @Deprecated(since = "productive environment")
    public Propulsion createPropulsion(@Nonnull final String name,
                                       @Nonnull final String description,
                                       @Nonnull final Research unlockedThrough,
                                       final int useCapacity,
                                       final int value,
                                       final int level,
                                       final boolean ftlCapable) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");
        Preconditions.checkNotNull(unlockedThrough, "unlockedThrough shouldn't be null!");

        return propulsionRepository.save(new Propulsion(name, description, unlockedThrough, useCapacity, value, level, ftlCapable));
    }

    public List<Armor> findAllArmorByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return armorRepository.findAllByUser(user);
    }

    public List<Propulsion> findAllPropulsionByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return propulsionRepository.findAllByUser(user);
    }

    public List<ElectronicWarfare> findAllElectronicWarfareByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return electronicWarfareRepository.findAllByUser(user);
    }

    public List<Sidewall> findAllSidewallByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return sidewallRepository.findAllByUser(user);
    }

    public List<Weapon> findAllWeaponByUser(@Nonnull final User user) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");

        return weaponRepository.findAllByUser(user);
    }
}

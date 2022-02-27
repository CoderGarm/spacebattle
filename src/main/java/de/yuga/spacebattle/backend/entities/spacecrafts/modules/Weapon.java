package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.converter.DistanceConverter;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.dto.physics.Distance;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;
import de.yuga.spacebattle.backend.enums.EAlignmentType;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import org.hibernate.annotations.Check;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@NamedQueries({
        @NamedQuery(name = "Weapon.getAll", query = "SELECT a FROM Weapon a"),
        @NamedQuery(name = "Weapon.getAllByResearches", query = "SELECT a FROM Weapon a WHERE a.unlockedThrough IN (:researches) OR a.unlockedThrough IS NULL")
})
@Entity
@Table(name = "weapon")
@Check(constraints = "weaponType = 'BEAM' || weaponType = 'POINT_DEFENSE'")
@AttributeOverride(name = "id", column = @Column(name = "idWeapon"))
public class Weapon extends BaseModuleWithEffectValue {

    /**
     * Defines the range of this weapon in meter.
     */
    @Nonnull
    @Convert(converter = DistanceConverter.class)
    private Distance damageProjectionRange;

    /**
     * The amount of damage emitters.<br>
     * <p>
     * In case of a laser cluster it is normally eight and an emitter have a reload time of 16 seconds.<br>
     * In case of an auto-cannon it is only one emitter present.<br>
     * <br>
     * The emitters will fire every combat round and will represent a chance to destroy an incoming missile.
     * </p>
     * <br>
     * <p>
     * If this weapon is a graser or a laser it can destroy missiles, too, but then it should have only one emitter.
     * </p>
     */
    private int amountDamageEmitter;

    /**
     * Defines the main use-case for this weapon.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EWeaponType weaponType;

    /**
     * Holds the information about the alignment ability.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EAlignmentType alignmentType;

    public Weapon() {
    }

    public Weapon(@Nonnull final String name,
                  @Nonnull final String description,
                  @Nonnull final Research unlockedThrough,
                  final int useCapacity,
                  final int effectValue,
                  final int techLevel,
                  @Nonnull final Distance damageProjectionRange,
                  final int amountDamageEmitter,
                  @Nonnull final EWeaponType weaponType,
                  @Nonnull final EAlignmentType alignmentType,
                  @Nonnull final CrewRequirement crewRequirement) {
        super(name, description, unlockedThrough, useCapacity, effectValue, techLevel, crewRequirement);
        Preconditions.checkNotNull(damageProjectionRange, "damageProjectionRange shouldn't be null!");
        Preconditions.checkNotNull(weaponType, "eWeaponType shouldn't be null!");
        Preconditions.checkNotNull(alignmentType, "alignmentType shouldn't be null!");

        this.damageProjectionRange = damageProjectionRange;
        this.amountDamageEmitter = amountDamageEmitter;
        this.weaponType = weaponType;
        this.alignmentType = alignmentType;
    }

    @Nonnull
    public Distance getDamageProjectionRange() {
        return damageProjectionRange;
    }

    public int getAmountDamageEmitter() {
        return amountDamageEmitter;
    }

    @Nonnull
    public EWeaponType getWeaponType() {
        return weaponType;
    }

    @Nonnull
    public EAlignmentType getAlignmentType() {
        return alignmentType;
    }

    @Nonnull
    public Set<EWeaponAlignment> getAllowedWeaponAlignments() {
        final Set<EWeaponAlignment> allowedWeaponAlignments = new HashSet<>();
        if (EAlignmentType.CHASE_ALIGNMENT == alignmentType) {
            allowedWeaponAlignments.add(EWeaponAlignment.BOW);
        }
        if (EAlignmentType.CHASE_ALIGNMENT == alignmentType) {
            allowedWeaponAlignments.add(EWeaponAlignment.STERN);
        }
        if (EAlignmentType.BATTLE_ALIGNMENT == alignmentType) {
            allowedWeaponAlignments.add(EWeaponAlignment.BROADSIDE);

        }
        return allowedWeaponAlignments;
    }
}

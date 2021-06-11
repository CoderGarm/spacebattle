package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;
import de.yuga.spacebattle.backend.enums.EAlignmentType;
import de.yuga.spacebattle.backend.enums.EDamageType;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.EWeaponType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
@AttributeOverride(name = "id", column = @Column(name = "idWeapon"))
public class Weapon extends BaseModule {

    /**
     * Defines the range of this weapon.
     */
    private int effectiveRange;

    /**
     * Defines the capability of this weapon to penetrate the shield.
     * The means the maneuver capability to find a gap in the tank to fire into it, for instance.
     */
    @Nullable
    @Column(columnDefinition = "decimal(19, 5)")
    private Double sideWallPenetration;

    /**
     * The way of damage projection.
     */
    @Nonnull
    @NotNull(message = "The damageType must not be null.")
    @Enumerated(EnumType.STRING)
    private EDamageType damageType;

    @Nonnull
    @NotNull(message = "The weaponType must not be null.")
    @Enumerated(EnumType.STRING)
    private EWeaponType weaponType;

    /**
     * Holds the information about the alignment ability.
     */
    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EAlignmentType alignmentType;

    /**
     * An empty ammunition module means that the weapon needs no ammunition.
     */
    @Nullable
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idAmmunitionModule")
    private AmmunitionModule ammunitionModule;

    public Weapon() {
    }

    public Weapon(@Nonnull final String name,
                  @Nonnull final String description,
                  @Nonnull final Research unlockedThrough,
                  @Nullable final AmmunitionModule ammunitionModule,
                  final int useCapacity,
                  final int effectValue,
                  final int techLevel,
                  final int effectiveRange,
                  @Nullable final Double sideWallPenetration,
                  @Nonnull final EDamageType damageType,
                  @Nonnull final EWeaponType weaponType,
                  @Nonnull final EAlignmentType alignmentType) {
        super(name, description, unlockedThrough, useCapacity, effectValue, techLevel);
        Preconditions.checkNotNull(damageType, "eDamageType shouldn't be null!");
        Preconditions.checkNotNull(weaponType, "eWeaponType shouldn't be null!");
        Preconditions.checkNotNull(alignmentType, "alignmentType shouldn't be null!");

        this.effectiveRange = effectiveRange;
        this.sideWallPenetration = sideWallPenetration;
        this.damageType = damageType;
        this.weaponType = weaponType;
        this.ammunitionModule = ammunitionModule;
        this.alignmentType = alignmentType;
    }

    public int getEffectiveRange() {
        return effectiveRange;
    }

    @Nullable
    public Double getSideWallPenetration() {
        return sideWallPenetration;
    }

    @Nonnull
    public EDamageType getDamageType() {
        return damageType;
    }

    @Nonnull
    public EWeaponType getWeaponType() {
        return weaponType;
    }

    @Nonnull
    public Set<EWeaponAlignment> getAllowedWeaponAlignments() {
        final Set<EWeaponAlignment> allowedWeaponAlignments = new HashSet<>();
        if (EAlignmentType.HUNTING_ALIGNMENT == alignmentType) {
            allowedWeaponAlignments.add(EWeaponAlignment.BOW);
        }
        if (EAlignmentType.HUNTING_ALIGNMENT == alignmentType) {
            allowedWeaponAlignments.add(EWeaponAlignment.STERN);
        }
        if (EAlignmentType.BATTLE_ALIGNMENT == alignmentType) {
            allowedWeaponAlignments.add(EWeaponAlignment.BROADSIDE);

        }
        return allowedWeaponAlignments;
    }

    @Nullable
    public AmmunitionModule getAmmunitionModule() {
        return ammunitionModule;
    }
}

package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;
import de.yuga.spacebattle.backend.enums.EDamageType;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Type;

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
@Check(constraints = "(allowedForBow = true AND allowedForStern = true AND allowedForBroadsides = false) OR (allowedForBow = false AND allowedForStern = false AND allowedForBroadsides = true)")
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
    @NotNull(message = "eDamageType must not be null")
    @Enumerated(EnumType.STRING)
    private EDamageType damageType;

    @Nonnull
    @NotNull(message = "weaponType must not be null")
    @Enumerated(EnumType.STRING)
    private EWeaponType weaponType;

    /**
     * Holds the information for the bow alignment ability.
     * todo check is bow/stern/broadsides is better displayed by "weapon for hund / weapon for battle"
     */
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean allowedForBow = false;

    /**
     * Holds the information for the stern alignment ability.
     */
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean allowedForStern = false;

    /**
     * Holds the information for the broadsides alignment ability.
     */
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean allowedForBroadsides = false;

    /**
     * Conclusion of the alignment fields - just for convenience.
     */
    @Transient
    private Set<EWeaponAlignment> allowedWeaponAlignments = new HashSet<>();

    public Weapon() {
    }

    public Weapon(@Nonnull final String name,
                  @Nonnull final String description,
                  @Nonnull final Research unlockedThrough,
                  final int useCapacity,
                  final int effectValue,
                  final int techLevel,
                  final int effectiveRange,
                  @Nullable final Double sideWallPenetration,
                  @Nonnull final EDamageType damageType,
                  @Nonnull final EWeaponType weaponType,
                  @Nonnull final Set<EWeaponAlignment> allowedWeaponAlignments) {
        super(name, description, unlockedThrough, useCapacity, effectValue, techLevel);
        Preconditions.checkNotNull(damageType, "eDamageType shouldn't be null!");
        Preconditions.checkNotNull(weaponType, "eWeaponType shouldn't be null!");
        Preconditions.checkNotNull(allowedWeaponAlignments, "allowedWeaponAlignments shouldn't be null!");

        this.effectiveRange = effectiveRange;
        this.sideWallPenetration = sideWallPenetration;
        this.damageType = damageType;
        this.weaponType = weaponType;
        this.allowedWeaponAlignments = allowedWeaponAlignments;
        if (allowedWeaponAlignments.contains(EWeaponAlignment.BOW)) {
            allowedForBow = true;
        }
        if (allowedWeaponAlignments.contains(EWeaponAlignment.STERN)) {
            allowedForStern = true;
        }
        if (allowedWeaponAlignments.contains(EWeaponAlignment.BROADSIDE)) {
            allowedForBroadsides = true;
        }
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
        if (allowedForBow) {
            allowedWeaponAlignments.add(EWeaponAlignment.BOW);
        }
        if (allowedForStern) {
            allowedWeaponAlignments.add(EWeaponAlignment.STERN);
        }
        if (allowedForBroadsides) {
            allowedWeaponAlignments.add(EWeaponAlignment.BROADSIDE);

        }
        return allowedWeaponAlignments;
    }
}

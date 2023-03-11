package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;
import de.yuga.spacebattle.backend.enums.*;
import org.hibernate.annotations.Check;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@NamedQueries({
        @NamedQuery(name = "Launcher.getAll", query = "SELECT a FROM Launcher a"),
        @NamedQuery(name = "Launcher.getAllByResearches", query = "SELECT a FROM Launcher a WHERE a.unlockedThrough IN (:researches) OR a.unlockedThrough IS NULL")
})
@Entity
@Table(name = "launcher")
@Check(constraints = "weaponType = 'MISSILE' OR weaponType = 'COUNTER_MISSILE'")
@AttributeOverride(name = "id", column = @Column(name = "idLauncher"))
public class Launcher extends BaseModule {

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

    @Nonnull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "allowedMissiles",
            joinColumns = @JoinColumn(name = "idLauncher", referencedColumnName = "idLauncher"),
            inverseJoinColumns = @JoinColumn(name = "idMissile", referencedColumnName = "idMissile")
    )
    private Set<Missile> allowedMissiles = new HashSet<>();

    public Launcher() {
    }

    public Launcher(@Nonnull final String name,
                    @Nonnull final String description,
                    @Nonnull final Research unlockedThrough,
                    final int useCapacity,
                    @Nonnull final EHullType hullType,
                    @Nonnull final ETechLevel techLevel,
                    @Nonnull final EAlignmentType alignmentType,
                    @Nonnull final CrewRequirement crewRequirement,
                    @Nonnull final EWeaponType weaponType,
                    @Nonnull final Set<Missile> allowedMissiles) {
        super(name, description, unlockedThrough, useCapacity, hullType, techLevel, crewRequirement, Launcher.class);
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");
        Preconditions.checkNotNull(alignmentType, "alignmentType shouldn't be null!");

        this.alignmentType = alignmentType;
        this.weaponType = weaponType;
        this.allowedMissiles = allowedMissiles;
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
    public Set<Missile> getAllowedMissiles() {
        return allowedMissiles;
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

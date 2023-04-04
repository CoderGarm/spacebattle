package de.yuga.spacebattle.backend.entities.spacecrafts.modules;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.dto.crew.CrewRequirement;
import de.yuga.spacebattle.backend.entities.misc.HasCostsByOwn;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.NamedTechLevel;
import de.yuga.spacebattle.backend.enums.EShipClassType;
import de.yuga.spacebattle.backend.enums.EWeaponType;
import org.hibernate.annotations.Check;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@NamedQueries({
        @NamedQuery(name = "Launcher.getAll", query = "SELECT a FROM Launcher a"),
        @NamedQuery(name = "Launcher.getAllByResearches", query = "SELECT a FROM Launcher a LEFT JOIN ResearchLevel rl ON (rl.research = a.namedTechLevel.unlockedThrough AND rl.user.id = :idUser) WHERE rl IS NOT NULL AND rl.level >= a.unlockedThroughLevel")
})
@Entity
@Table(name = "launcher")
@Check(constraints = "weaponType = 'MISSILE' OR weaponType = 'COUNTER_MISSILE'")
@AttributeOverride(name = "id", column = @Column(name = "idLauncher"))
public class Launcher extends HasCostsByOwn {

    @Nonnull
    @NotNull
    @Enumerated(EnumType.STRING)
    private EWeaponType weaponType;

    @Nonnull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "allowedMissiles",
            joinColumns = @JoinColumn(name = "idLauncher", referencedColumnName = "idLauncher"),
            inverseJoinColumns = @JoinColumn(name = "idMissile", referencedColumnName = "idMissile")
    )
    private Set<Missile> allowedMissiles = new HashSet<>();

    public Launcher() {
    }

    public Launcher(@Nonnull final NamedTechLevel baseModule,
                    @Nonnull final String technicalTypeName,
                    final int unlockedThroughLevel,
                    final int tonnage,
                    @Nonnull final EShipClassType shipClassType,
                    @Nonnull final CrewRequirement crewRequirement,
                    @Nonnull final EWeaponType weaponType,
                    @Nonnull final Set<Missile> allowedMissiles) {
        super(baseModule, technicalTypeName, unlockedThroughLevel, 1, tonnage, shipClassType, crewRequirement);
        Preconditions.checkNotNull(weaponType, "weaponType shouldn't be null!");

        this.weaponType = weaponType;
        this.allowedMissiles = allowedMissiles;
    }

    @Nonnull
    public EWeaponType getWeaponType() {
        return weaponType;
    }

    @Nonnull
    public Set<Missile> getAllowedMissiles() {
        return allowedMissiles;
    }

    @Nonnull
    public Missile getHeaviestMissile() {
        /* todo fix missile selection */
        return allowedMissiles.stream().sorted(Comparator.comparingLong(Missile::getDamageValue)).reduce((o1, o2) -> o2).orElseThrow(NullPointerException::new);
    }
}

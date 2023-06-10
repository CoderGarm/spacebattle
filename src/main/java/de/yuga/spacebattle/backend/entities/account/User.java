package de.yuga.spacebattle.backend.entities.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.converter.EGameUserRolesConverter;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.enums.EGameUserRole;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import de.yuga.spacebattle.backend.enums.OwnerType;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NamedQueries({
        @NamedQuery(name = "User.getAll", query = "SELECT u FROM User u WHERE u.id != 2 AND u.dType = de.yuga.spacebattle.backend.enums.OwnerType.USER"), /* fixme exclude defeated opponent id */
        @NamedQuery(name = "User.findByLikeUsername", query = "SELECT u FROM User u WHERE UPPER(u.username) LIKE UPPER(:username)"),
        @NamedQuery(name = "User.findByUsernameAndEmail", query = "SELECT u FROM User u WHERE UPPER(u.username) = UPPER(:username) AND UPPER(u.userSetting.email) = UPPER(:email)"),
        @NamedQuery(name = "User.getWithKnownStarSystems", query = "SELECT u FROM User u LEFT JOIN FETCH u.knownStarSystems r WHERE u.id = :idUser"),
        @NamedQuery(name = "User.findByUsernameExact", query = "SELECT u.id FROM User u WHERE UPPER(u.username) = UPPER(:username)"), /* todo reduce to boolean */
        @NamedQuery(name = "User.findByEMailExact", query = "SELECT u.id FROM User u WHERE UPPER(u.userSetting.email) = UPPER(:email)"), /* todo reduce to boolean */
        @NamedQuery(name = "User.findByUsername", query = "SELECT u FROM User u WHERE UPPER(u.username) = UPPER(:username)"),
        @NamedQuery(name = "User.findAllianceAdminByAlliance", query = "SELECT u FROM User u WHERE u.alliance = :alliance AND :gameUserRole IN (u.gameUserRoles)")
})
@Entity
@DiscriminatorValue(OwnerType.USER)
public class User extends Owner {

    @NotNull
    @Nonnull
    @Enumerated(EnumType.STRING)
    private EWebUserRole userRole;

    @Nonnull
    @Convert(converter = EGameUserRolesConverter.class)
    private final Set<EGameUserRole> gameUserRoles = new HashSet<>();

    @Nullable
    @ManyToOne
    @JoinColumn(name = "idAlliance")
    private Alliance alliance;

    /**
     * Represents all star systems which information was bought by the user in order to colonize them.<br>
     * <p>
     * <b>Attention: </b>
     * Currently this implies that the new owner will get all information about the system without buying it especially.<br>
     * <br>
     * Compare:<br>
     * - {@link ColonizationService#startColonizingPlanet(User, Planet)}<br>
     * - {@link PlanetService#createPlanet(String, StarSystem, Orbit)}<br>
     * </p>
     */
    @Nonnull
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinTable(name = "knownStarSystem",
            joinColumns = @JoinColumn(name = "idOwner"),
            inverseJoinColumns = @JoinColumn(name = "idStarSystem"),
            uniqueConstraints = @UniqueConstraint(name = "knownStarSystem_UC", columnNames = {"idOwner", "idStarSystem"}))
    private final Set<StarSystem> knownStarSystems = new HashSet<>();

    @Nonnull
    @OneToMany(cascade = CascadeType.MERGE, orphanRemoval = true, mappedBy = "user")
    private final Set<Colonization> colonizations = new HashSet<>();

    @Nonnull
    @NotNull
    @JoinColumn(name = "idUser")
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private UserSetting userSetting;

    public User() {
    }

    public User(@Nonnull final String username,
                @Nonnull final String password,
                @Nonnull final String email,
                @Nonnull final EWebUserRole role,
                final boolean noEMailWanted,
                @Nullable final EGameUserRole... gameUserRoles) {
        super(username);
        Preconditions.checkNotNull(password, "password shouldn't be null!");
        Preconditions.checkNotNull(email, "email shouldn't be null!");
        Preconditions.checkNotNull(role, "role shouldn't be null!");

        this.userSetting = new UserSetting(this, email, password, noEMailWanted);
        this.userRole = role;
        if (gameUserRoles != null) {
            this.gameUserRoles.addAll(Arrays.stream(gameUserRoles).collect(Collectors.toSet()));
        }
    }

    @Nonnull
    public EWebUserRole getUserRole() {
        return userRole;
    }

    @Nonnull
    public Set<EGameUserRole> getGameUserRoles() {
        return gameUserRoles;
    }

    public void addGameUserRoles(@Nonnull final EGameUserRole gameUserRole) {
        Preconditions.checkNotNull(gameUserRole, "gameUserRole shouldn't be null!");

        this.gameUserRoles.add(gameUserRole);
    }

    public void removeGameUserRoles(@Nonnull final EGameUserRole gameUserRole) {
        Preconditions.checkNotNull(gameUserRole, "gameUserRole shouldn't be null!");

        this.gameUserRoles.remove(gameUserRole);
    }

    @Nullable
    public Alliance getAlliance() {
        return alliance;
    }

    public void setAlliance(@Nullable final Alliance alliance) {
        this.alliance = alliance;
    }

    @Nonnull
    public Set<Colonization> getColonizations() {
        return colonizations;
    }

    public void addKnownStarSystems(@Nonnull final StarSystem starSystem) {
        knownStarSystems.add(starSystem);
    }

    @Nonnull
    public Set<StarSystem> getKnownStarSystems() {
        return knownStarSystems;
    }

    @Nonnull
    public UserSetting getUserSetting() {
        return userSetting;
    }
}

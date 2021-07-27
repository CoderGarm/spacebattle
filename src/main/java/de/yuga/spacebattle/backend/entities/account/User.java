package de.yuga.spacebattle.backend.entities.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.orbitals.Orbit;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.services.account.PasswordConverter;
import de.yuga.spacebattle.backend.services.orbitals.PlanetService;
import de.yuga.spacebattle.backend.services.turn.ColonizationService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NamedQueries({
        @NamedQuery(name = "User.getAll", query = "SELECT u FROM User u"),
        @NamedQuery(name = "User.findByLikeUsername", query = "SELECT u FROM User u WHERE UPPER(u.username) LIKE UPPER(:username)"),
        @NamedQuery(name = "User.findByUsernameAndEmail", query = "SELECT u FROM User u WHERE UPPER(u.username) = UPPER(:username) AND UPPER(u.email) = UPPER(:email)"),
        @NamedQuery(name = "User.login", query = "SELECT u FROM User u LEFT JOIN FETCH u.ownedPlanets p LEFT JOIN FETCH u.alliance a LEFT JOIN FETCH u.researches r WHERE UPPER(u.username) = :username AND UPPER(u.password) = UPPER(:password)"),
        @NamedQuery(name = "User.getWithResearchesAndJobs", query = "SELECT u FROM User u LEFT JOIN FETCH u.researches r LEFT JOIN FETCH u.jobs j WHERE u.id = :idUser"),
        @NamedQuery(name = "User.getWithResearches", query = "SELECT u FROM User u LEFT JOIN FETCH u.researches r WHERE u.id = :idUser"),
        @NamedQuery(name = "User.getWithKnownStarSystems", query = "SELECT u FROM User u LEFT JOIN FETCH u.knownStarSystems r WHERE u.id = :idUser"),
        @NamedQuery(name = "User.getColonizations", query = "SELECT u FROM User u LEFT JOIN FETCH u.colonizations r WHERE u = :user"),
        @NamedQuery(name = "User.findByUsernameExact", query = "SELECT u.id FROM User u WHERE UPPER(u.username) = UPPER(:username)"),
        @NamedQuery(name = "User.findByEMailExact", query = "SELECT u.id FROM User u WHERE UPPER(u.email) = UPPER(:email)"),
        @NamedQuery(name = "User.findByUsername", query = "SELECT u FROM User u WHERE UPPER(u.username) = UPPER(:username)")
})
@Entity
@Table(name = "user",
        uniqueConstraints = {
                @UniqueConstraint(name = "USERNAME_UK", columnNames = {"username"}),
                @UniqueConstraint(name = "EMAIL_UK", columnNames = {"email"})
        })
@AttributeOverride(name = "id", column = @Column(name = "idUser"))
public class User extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @Pattern(regexp = "[a-zA-Z0-9]{3,30}", message = "must contain of 3 to 30 characters of numbers or letters")
    @Size(min = 3, max = 30)
    @Column(unique = true)
    private String username;

    @Nonnull
    @NotNull
    @Pattern(regexp = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,30})", message = "must contain of 8 to 30 characters of numbers, letters, capital letters and special characters")
    @Convert(converter = PasswordConverter.class)
    private String password;

    @Nonnull
    @NotNull
    @Email
    @Size(min = 3, max = 50)
    private String email;

    @Nullable
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idAlliance")
    private Alliance alliance;

    @Nonnull
    @NotNull
    @OneToMany(cascade = CascadeType.MERGE, orphanRemoval = true, mappedBy = "owner")
    private final Set<Planet> ownedPlanets = new HashSet<>();

    /**
     * All already researched researches for the user.
     */
    @Nonnull
    @NotNull
    @ElementCollection
    @MapKeyJoinColumn(name = "idResearch", referencedColumnName = "idResearch")
    @Column(name = "level")
    @CollectionTable(name = "unlockedResearch", joinColumns = @JoinColumn(name = "idUser"))
    private final Map<Research, Integer> researches = new HashMap<>();

    /**
     * The currently running jobs for the user.
     */
    @Nonnull
    @OneToMany(cascade = CascadeType.MERGE, orphanRemoval = true, mappedBy = "owner")
    private final Set<Job> jobs = new HashSet<>();

    /**
     * The ship classes which was created by the user.
     */
    @Nonnull
    @OneToMany(cascade = CascadeType.MERGE, orphanRemoval = true, mappedBy = "owner")
    private final Set<ShipClass> shipClasses = new HashSet<>();

    /**
     * Represents all star systems which information was bought by the user in order to colonize them.<br>
     * <p>
     * <b>Attention: </b>
     * Currently this implies that the new owner will get all information about the system without buying it especially.<br>
     * <br>
     * Compare:<br>
     * - {@link ColonizationService#startColonizingPlanet(User, Planet)}<br>
     * - {@link PlanetService#createPlanet(String, StarSystem, Orbit)}<br>
     * - {@link PlanetService#createPlanet(String, StarSystem, Integer, Integer)}
     * </p>
     */
    @Nonnull
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinTable(name = "knownStarSystem",
            joinColumns = @JoinColumn(name = "idOwner"),
            inverseJoinColumns = @JoinColumn(name = "idStarSystem"),
            uniqueConstraints = @UniqueConstraint(name = "knownStarSystem_UC", columnNames = {"idOwner", "idStarSystem"}))
    private final Set<StarSystem> knownStarSystems = new HashSet<>();

    /**
     * The ship classes which was created by the user.
     */
    @Nonnull
    @OneToMany(cascade = CascadeType.MERGE, orphanRemoval = true, mappedBy = "user")
    private final Set<Colonization> colonizations = new HashSet<>();

    @Transient
    private final String avatar = "https://mir-s3-cdn-cf.behance.net/project_modules/disp/ce54bf11889067.562541ef7cde4.png";

    public User() {
    }

    public User(@Nonnull final String username,
                @Nonnull final String password,
                @Nonnull final String email) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");
        Preconditions.checkNotNull(password, "password shouldn't be null!");
        Preconditions.checkNotNull(email, "email shouldn't be null!");

        this.username = username;
        this.password = password;
        this.email = email;
    }

    @Nonnull
    public String getUsername() {
        return username;
    }

    public void setUsername(@Nonnull final String username) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");

        this.username = username;
    }

    @Nonnull
    public String getPassword() {
        return password;
    }

    public void setPassword(@Nonnull final String password) {
        Preconditions.checkNotNull(password, "password shouldn't be null!");

        this.password = password;
    }

    @Nonnull
    public String getEmail() {
        return email;
    }

    public void setEmail(@Nonnull String email) {
        this.email = email;
    }

    @Nullable
    public Alliance getAlliance() {
        return alliance;
    }

    public void setAlliance(@Nonnull final Alliance alliance) {
        Preconditions.checkNotNull(alliance, "alliance shouldn't be null!");

        this.alliance = alliance;
    }

    @Nonnull
    public Set<Planet> getOwnedPlanets() {
        return ownedPlanets;
    }

    @Nonnull
    public Map<Research, Integer> getResearches() {
        return researches;
    }

    @Nonnull
    public Set<Job> getJobs() {
        return jobs;
    }

    @Nonnull
    public Set<ShipClass> getShipClasses() {
        return shipClasses;
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

    public String getAvatar() {
        return avatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }

}

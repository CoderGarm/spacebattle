package de.yuga.spacebattle.backend.entities.account;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.ShipClass;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.enums.ERaceType;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;
import java.util.stream.Collectors;


@NamedQueries({
        @NamedQuery(name = "User.getAll", query = "SELECT u FROM User u"),
        @NamedQuery(name = "User.checkParameter", query = "SELECT u FROM User u WHERE UPPER(u.username) = :username AND UPPER(u.email) = :email"),
        @NamedQuery(name = "User.login", query = "SELECT u FROM User u LEFT JOIN FETCH u.ownedPlanets p LEFT JOIN FETCH u.alliance a LEFT JOIN FETCH u.researches r WHERE UPPER(u.username) = :username AND UPPER(u.password) = :password")
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
    @NotNull(message = "username must not be null")
    @Size(min = 1, max = 30)
    @Column(unique = true)
    private String username;

    @Nonnull
    @NotNull(message = "password must not be null")
    @Size(min = 1, max = 50)
    //@Convert(converter = PasswordConverter.class)
    private String password;

    @Nonnull
    @NotNull(message = "eMail must not be null")
    @Size(min = 1, max = 50)
    private String email;

    @Nonnull
    @NotNull(message = "raceType must not be null")
    @Enumerated(EnumType.STRING)
    private ERaceType raceType;

    @JsonIgnore
    @Nullable
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idAlliance")
    private Alliance alliance;

    @Nonnull
    @NotNull(message = "ownedPlanets must not be null")
    @OneToMany(cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "owner")
    private final Set<Planet> ownedPlanets = new HashSet<>();

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyJoinColumn(name = "idResearch", referencedColumnName = "idResearch")
    @Column(name = "level")
    @CollectionTable(name = "unlockedResearch", joinColumns = @JoinColumn(name = "idUser"))
    private final Map<Research, Integer> researches = new HashMap<>();

    @Nonnull
    @OneToMany(cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "owner")
    private final Set<Job> jobs = new HashSet<>();

    @Nonnull
    @OneToMany(cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "owner")
    private final Set<ShipClass> shipClasses = new HashSet<>();

    public User() {
    }

    public User(@Nonnull final String username,
                @Nonnull final String password,
                @Nonnull final String email,
                @Nonnull final ERaceType raceType) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");
        Preconditions.checkNotNull(password, "password shouldn't be null!");
        Preconditions.checkNotNull(email, "email shouldn't be null!");
        Preconditions.checkNotNull(raceType, "raceType shouldn't be null!");

        this.username = username;
        this.password = password;
        this.raceType = raceType;
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

    public void setRaceType(@Nonnull ERaceType raceType) {
        Preconditions.checkNotNull(raceType, "raceType shouldn't be null!");

        this.raceType = raceType;
    }

    @Nonnull
    public String getEmail() {
        return email;
    }

    public void setEmail(@Nonnull String email) {
        this.email = email;
    }

    @Nonnull
    public ERaceType getRaceType() {
        return raceType;
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

    public void addResearch(@Nonnull final Research research) {
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        if (researches.containsKey(research)) {
            Integer level = researches.get(research);
            researches.put(research, ++level);
        } else {
            researches.put(research, 1);
        }
    }

    @Nonnull
    public Set<Job> getJobs() {
        return jobs;
    }

    @Nonnull
    public Set<ShipClass> getShipClasses() {
        return shipClasses;
    }

    /**
     * Returns the possible planet which is designated for holding research jobs.
     *
     * @return the planet or even not the planet
     */
    public Optional<Planet> getResearchInstitute() {

        List<Construction> collect = getOwnedPlanets().parallelStream()
                .map(planet -> planet.getConstructions().stream()
                        .filter(construction -> construction.getBuilding().getResourceType() == EResourceType.RESEARCH)
                        .findFirst().get())
                .sorted(Comparator.comparingInt(AbstractEntityKey::getId))
                .collect(Collectors.toList());

        if (collect.isEmpty()) {
            return Optional.empty();
        } else {
            Construction construction = collect.get(0);
            return Optional.of(construction.getPlanet());
        }
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

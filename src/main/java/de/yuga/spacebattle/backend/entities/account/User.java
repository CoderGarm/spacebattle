package de.yuga.spacebattle.backend.entities.account;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.Research;
import de.yuga.spacebattle.backend.enums.ERaceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


@NamedQueries({
        @NamedQuery(name = "User.getAll", query = "SELECT u FROM User u")
})
@Entity
@Table(name = "user")
@AttributeOverride(name = "id", column = @Column(name = "idUser"))
public class User extends AbstractEntityKey {

    @Nonnull
    @NotNull(message = "username must not be null")
    @Size(min = 1, max = 30)
    @Column(unique = true)
    private String username;

    // todo attributeconverter
    @Nonnull
    @NotNull(message = "password must not be null")
    @Size(min = 1, max = 50)
    private String password;

    @Nonnull
    @NotNull(message = "racetype must not be null")
    @Enumerated(EnumType.STRING)
    private ERaceType raceType;

    @JsonIgnore
    @Nullable
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "idAlliance")
    private Alliance alliance;

    @Nonnull
    @NotNull(message = "ownedPlanets must not be null")
    @OneToMany(cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinTable(name = "ownedPlanet",
            joinColumns = @JoinColumn(name = "idUser"),
            inverseJoinColumns = @JoinColumn(name = "idPlanet"))
    private final Set<Planet> ownedPlanets = new HashSet<>();

    @Nonnull
    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyJoinColumn(name = "idResearch", referencedColumnName = "idResearch")
    @Column(name = "level")
    @CollectionTable(name = "unlockedResearch", joinColumns = @JoinColumn(name = "idUser"))
    private final Map<Research, Integer> researches = new HashMap<>();

    public User() {
    }

    public User(@Nonnull final String username,
                @Nonnull final String password,
                @Nonnull final ERaceType raceType) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");
        Preconditions.checkNotNull(password, "password shouldn't be null!");
        Preconditions.checkNotNull(raceType, "raceType shouldn't be null!");

        this.username = username;
        this.password = password;
        this.raceType = raceType;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append(", id=").append(id);
        sb.append("username='").append(username).append('\'');
        //sb.append(", password='").append(password).append('\'');
        sb.append(", raceType=").append(raceType);
        sb.append(", alliance=").append(alliance);
        sb.append(", ownedPlanets=").append(ownedPlanets);
        sb.append(", researches=").append(researches);
        sb.append('}');
        return sb.toString();
    }
}

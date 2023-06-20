package de.yuga.spacebattle.backend.entities.combined.account;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import jakarta.persistence.*;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NamedQueries({
        @NamedQuery(name = "Alliance.getAll", query = "SELECT a FROM Alliance a"),
        @NamedQuery(name = "Alliance.findByNameExact", query = "SELECT a.id FROM Alliance a WHERE UPPER(a.name) = UPPER(:name)"),
        @NamedQuery(name = "Alliance.findByCodeExact", query = "SELECT a.id FROM Alliance a WHERE UPPER(a.code) = UPPER(:code)"),
        @NamedQuery(name = "Alliance.findAllWithMembers", query = "SELECT DISTINCT a FROM Alliance a LEFT JOIN FETCH a.members"),
        @NamedQuery(name = "Alliance.findByIdWithMembers", query = "SELECT a FROM Alliance a LEFT JOIN FETCH a.members WHERE a.id = :idAlliance"),
})
@Entity
@Table(name = "alliance")
@AttributeOverride(name = "id", column = @Column(name = "idAlliance"))
public class Alliance extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @Size(min = 1, max = 30)
    @Column(unique = true)
    private String name;

    @Nonnull
    @NotNull
    @Size(min = 1, max = 30)
    @Column(unique = true)
    private String code;

    @Nonnull
    @NotNull
    @OneToMany(mappedBy = "alliance")
    private final Set<User> members = new HashSet<>();

    @Nonnull
    @OneToMany
    @JoinColumn(name = "idAlliance")
    private final Set<AllianceApplication> applications = new HashSet<>();

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idFounder")
    private User founder;

    @Nonnull
    @NotNull
    private final LocalDateTime createdAt = LocalDateTime.now();

    public Alliance() {
    }

    /**
     * Please save the founder afterwards!
     */
    public Alliance(@Nonnull final String name, @Nonnull final String code, @Nonnull final User founder) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(code, "code shouldn't be null!");
        Preconditions.checkNotNull(founder, "founder shouldn't be null!");

        this.name = name;
        this.code = code;
        this.founder = founder;
        this.members.add(founder);
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setName(@Nonnull final String username) {
        Preconditions.checkNotNull(username, "username shouldn't be null!");

        this.name = username;
    }

    @Nonnull
    public String getCode() {
        return code;
    }

    public void setCode(@Nonnull final String password) {
        Preconditions.checkNotNull(password, "password shouldn't be null!");

        this.code = password;
    }

    @Nonnull
    public Set<User> getMembers() {
        return members;
    }

    @Nonnull
    public Set<AllianceApplication> getApplications() {
        return applications;
    }

    @Nonnull
    public User getFounder() {
        return founder;
    }

    @Nonnull
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Alliance)) return false;

        Alliance alliance = (Alliance) o;

        return id == alliance.getId();
    }

    @Override
    public int hashCode() {
        return 37 * id;
    }
}

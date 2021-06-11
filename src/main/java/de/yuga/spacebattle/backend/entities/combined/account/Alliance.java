package de.yuga.spacebattle.backend.entities.combined.account;


import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.account.User;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@NamedQueries({
        @NamedQuery(name = "Alliance.getAll", query = "SELECT a FROM Alliance a")
})
@Entity
@Table(name = "alliance")
@AttributeOverride(name = "id", column = @Column(name = "idAlliance"))
public class Alliance extends AbstractEntityKey {

    @Nonnull
    @NotNull(message = "name must not be null")
    @Size(min = 1, max = 30)
    @Column(unique = true)
    private String name;

    @Nonnull
    @NotNull(message = "code must not be null")
    @Size(min = 1, max = 30)
    @Column(unique = true)
    private String code;

    @Nonnull
    @NotNull(message = "members must not be null")
    @OneToMany(mappedBy = "alliance", fetch = FetchType.EAGER)
    private final Set<User> members = new HashSet<>();

    public Alliance() {
    }

    public Alliance(@Nonnull String name,
                    @Nonnull String code) {
        Preconditions.checkNotNull(name, "name shouldn't be null!");
        Preconditions.checkNotNull(code, "code shouldn't be null!");

        this.name = name;
        this.code = code;
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
}

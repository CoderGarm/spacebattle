package de.yuga.spacebattle.backend.entities.account;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.entities.misc.HasOwner;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(name = "USERNAME_UK", columnNames = {"username"})
})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dType", discriminatorType = DiscriminatorType.STRING)
@AttributeOverride(name = "id", column = @Column(name = "idUser"))
public class Owner extends AbstractEntityKey implements HasOwner {

    /**
     * If an uncolonized planet should be handled as it would be colonized by an NPC but isn't.
     */
    @Nonnull
    public static final Owner UNCOLONIZED = new Owner("Uncolonized");

    @Nonnull
    @NotNull
    @Column(insertable = false, updatable = false, nullable = false)
    @SuppressWarnings("NotNullFieldNotInitialized")
    private String dType;

    @Nonnull
    @NotNull
    @Pattern(regexp = "[a-zA-Z0-9]{3,30}", message = "must contain of 3 to 30 characters of numbers or letters")
    @Size(min = 3, max = 30)
    @Column(unique = true)
    private String username;

    public Owner() {
    }

    public Owner(@Nonnull final String username) {
        this.username = Preconditions.checkNotNull(username, "username must not be empty");
    }

    @Nonnull
    public String getUsername() {
        return username;
    }

    @Nullable
    @Override
    public Owner getOwner() {
        return this;
    }

    @Nullable
    @Override
    public User getHumanOwner() {
        if (!(this instanceof User)) {
            return null;
        }
        return (User) this;
    }

    @Nullable
    @Override
    public NonPlayerCharacter getNpcOwner() {
        if (!(this instanceof NonPlayerCharacter)) {
            return null;
        }
        return (NonPlayerCharacter) this;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Owner owner = (Owner) o;

        return new EqualsBuilder().append(dType, owner.dType).append(username, owner.username).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(dType).append(username).toHashCode();
    }
}

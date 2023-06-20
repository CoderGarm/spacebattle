package de.yuga.spacebattle.backend.entities.account.forum;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.account.Alliance;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Check;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NamedQueries({
        @NamedQuery(name = "Forum.getAllianceForum", query = "SELECT f FROM Forum f WHERE f.alliance = :alliance"),
})
@Entity
@Table(name = "forum")
@AttributeOverride(name = "id", column = @Column(name = "idForum"))
@Check(constraints = "idAlliance IS NOT NULL OR role IS NOT NULL")
public class Forum extends AbstractEntityKey {

    /**
     * If it has an alliance set, only members can visit it.
     */
    @Nullable
    @OneToOne
    @JoinColumn(name = "idAlliance")
    private Alliance alliance;

    /**
     * If it has a role set, only the bearer can visit it.
     */
    @Nullable
    @Enumerated(EnumType.STRING)
    private EWebUserRole role;

    @Nonnull
    @NotNull
    @SuppressWarnings("NotNullFieldNotInitialized")
    private String title;

    @Nonnull
    @NotNull
    @SuppressWarnings("NotNullFieldNotInitialized")
    private String description;

    @Nonnull
    @NotNull
    @SuppressWarnings("NotNullFieldNotInitialized")
    private LocalDateTime createdAt;

    @Nonnull
    @OneToMany
    @JoinColumn(name = "idForum")
    private final Set<ForumThread> threads = new HashSet<>();

    public Forum() {
    }

    public Forum(@Nullable final Alliance alliance,
                 @Nonnull final String title,
                 @Nonnull final String description) {
        this(title, description);
        Preconditions.checkNotNull(alliance, "alliance shouldn't be null!");

        this.alliance = alliance;
    }

    public Forum(@Nullable final EWebUserRole role,
                 @Nonnull final String title,
                 @Nonnull final String description) {
        this(title, description);
        Preconditions.checkNotNull(role, "role shouldn't be null!");

        this.role = role;
    }

    public Forum(@Nonnull final String title, @Nonnull final String description) {
        Preconditions.checkNotNull(title, "title shouldn't be null!");
        Preconditions.checkNotNull(description, "description shouldn't be null!");

        this.title = title;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    @Nullable
    public Alliance getAlliance() {
        return alliance;
    }

    @Nullable
    public EWebUserRole getRole() {
        return role;
    }

    @Nonnull
    public String getTitle() {
        return title;
    }

    @Nonnull
    public String getDescription() {
        return description;
    }

    @Nonnull
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Checks if a user has access to this forum.
     *
     * @param user the user
     * @return <code>true</code> if the user may enter the forum, <code>false</code> otherwise
     */
    public boolean isUserAllowed(final User user) {
        boolean isAllianceOk = getAlliance() == null || getAlliance() != null && getAlliance().equals(user.getAlliance());
        boolean isRoleOk = getRole() == null || getRole() != null && user.getUserRole().getAllowedRoles().contains(getRole());
        return isAllianceOk && isRoleOk;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof Forum)) return false;

        final Forum forum = (Forum) o;

        return new EqualsBuilder().append(id, forum.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}

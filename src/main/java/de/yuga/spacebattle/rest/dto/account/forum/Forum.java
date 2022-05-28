package de.yuga.spacebattle.rest.dto.account.forum;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EWebUserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Schema(description = "Represents a single forum with it's access rules and threads.")
public class Forum {

    @JsonProperty
    @Schema(required = true, description = "The id of the forum.")
    private final int idForum;

    @Nullable
    @JsonProperty
    @Schema(description = "If set, the forum is for the defined alliance only.")
    private final Integer idAlliance;

    @Nullable
    @JsonProperty
    @Schema(description = "If it has a role set, only the bearer can visit it.")
    private final EWebUserRole role;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The forums title.")
    private final String title;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The forums description.")
    private final String description;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The forums creation timestamp.")
    private final LocalDateTime createdAt;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The id's of the forum's threads.")
    private final Set<Integer> idForumThreads = new HashSet<>();

    public Forum(@Nonnull final de.yuga.spacebattle.backend.entities.account.forum.Forum forum) {
        Preconditions.checkNotNull(forum, "forum shouldn't be null!");

        this.idForum = forum.getId();
        this.idAlliance = forum.getAlliance() != null ? forum.getAlliance().getId() : null;
        this.role = forum.getRole() != null ? forum.getRole() : null;
        this.title = forum.getTitle();
        this.description = forum.getDescription();
        this.createdAt = forum.getCreatedAt();
    }

    public int getIdForum() {
        return idForum;
    }

    @Nullable
    public Integer getIdAlliance() {
        return idAlliance;
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

    @Nonnull
    public Set<Integer> getIdForumThreads() {
        return idForumThreads;
    }


    public void enrichForumThreads(@Nonnull final List<Integer> idForumThreads) {
        Preconditions.checkNotNull(idForumThreads, "idForumThreads shouldn't be null!");

        this.idForumThreads.addAll(idForumThreads);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (!(o instanceof Forum)) return false;

        final Forum forum = (Forum) o;

        return new EqualsBuilder().append(idForum, forum.idForum).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(idForum).toHashCode();
    }
}

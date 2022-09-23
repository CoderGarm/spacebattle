package de.yuga.spacebattle.rest.dto.account.forum;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Schema(description = "A forum thread.")
public class ForumThread {

    @JsonProperty
    @Schema(required = true, description = "The id of the thread.")
    private final int idForumThread;

    @JsonProperty
    @Schema(required = true, description = "The id of the parent's forum.")
    private final int idForum;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The title of the thread.")
    private String title;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The description of the thread.")
    private String description;

    @Nonnull
    @JsonProperty
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(required = true, description = "The creation timestamp.")
    private LocalDateTime createdAt;

    @Nonnull
    @JsonProperty
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Schema(required = true, description = "The timestamp of the last added message.")
    private LocalDateTime lastChanged;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The id of the thread.")
    private final Set<Integer> idForumMessages = new HashSet<>();

    public ForumThread(@Nonnull final de.yuga.spacebattle.backend.entities.account.forum.ForumThread thread) {
        Preconditions.checkNotNull(thread, "thread shouldn't be null!");

        this.idForumThread = thread.getId();
        this.idForum = thread.getForum().getId();
        this.title = thread.getTitle();
        this.description = thread.getDescription();
        this.createdAt = thread.getCreatedAt();
        this.lastChanged = thread.getLastChanged();
    }

    public void enrichMessageIds(@Nonnull final List<Integer> messageIds) {
        Preconditions.checkNotNull(messageIds, "messageIds shouldn't be null!");

        this.idForumMessages.addAll(messageIds);
    }
}

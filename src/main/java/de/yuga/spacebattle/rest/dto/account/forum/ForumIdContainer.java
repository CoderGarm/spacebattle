package de.yuga.spacebattle.rest.dto.account.forum;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nullable;

@Schema()
public class ForumIdContainer {

    @Nullable
    @JsonProperty
    @Schema(description = "The forum id")
    private Integer idForum;

    @Nullable
    @JsonProperty
    @Schema(description = "The thread id")
    private Integer idThread;

    @Nullable
    @JsonProperty
    @Schema(description = "The message id")
    private Integer idMessage;

    public ForumIdContainer() {

    }

    @Nullable
    public Integer getIdForum() {
        return idForum;
    }

    @Nullable
    public Integer getIdThread() {
        return idThread;
    }

    @Nullable
    public Integer getIdMessage() {
        return idMessage;
    }
}

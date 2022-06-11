package de.yuga.spacebattle.rest.dto.combined.account;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.AbstractEntityKey;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.stream.Collectors;

@Schema(description = ".")
public class Alliance {

    @JsonProperty
    @Schema(required = true, description = "The id of the alliance.")
    private int idAlliance;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The name of the alliance.")
    private String name;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The code of the alliance.")
    private String code;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The founder of the alliance.")
    private UserJson founder;

    @JsonProperty
    @Schema(required = true, description = "The amount of members")
    private int membersAmount;

    public Alliance() {
    }

    public Alliance(@Nonnull final de.yuga.spacebattle.backend.entities.combined.account.Alliance alliance) {
        Preconditions.checkNotNull(alliance, "alliance shouldn't be null!");

        this.idAlliance = alliance.getId();
        this.name = alliance.getName();
        this.code = alliance.getCode();
        this.founder = new UserJson(alliance.getMembers().stream().sorted(Comparator.comparingInt(AbstractEntityKey::getId)).collect(Collectors.toList()).get(0));
        this.membersAmount = alliance.getMembers().size();
    }
}

package de.yuga.spacebattle.rest.dto.spacecrafts.modules.basics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

public class HasIdModule {

    @JsonProperty
    @Schema(required = true, description = "The id of this module.")
    private int idModule;

    public HasIdModule() {
        this.idModule = -1;
    }

    public HasIdModule(@Nonnull final AbstractEntityKey abstractEntityKey) {
        Preconditions.checkNotNull(abstractEntityKey, "abstractEntityKey must not be empty");

        this.idModule = abstractEntityKey.getId();
    }

    @JsonIgnore
    public int getIdModule() {
        return idModule;
    }
}

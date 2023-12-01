package de.yuga.spacebattle.rest.dto.misc.descriptors;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.Armor;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;

@Schema(description = ".")
public class ArmorDescriptor {

    public ArmorDescriptor() {
    }

    public ArmorDescriptor(@Nonnull final Armor content) {
        Preconditions.checkNotNull(content, "content must not be empty");

        // I am empty for the sake of attendance
    }
}

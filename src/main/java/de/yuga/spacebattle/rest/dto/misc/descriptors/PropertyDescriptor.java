package de.yuga.spacebattle.rest.dto.misc.descriptors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.OrbitalModule;
import de.yuga.spacebattle.backend.entities.misc.HasName;
import de.yuga.spacebattle.backend.entities.misc.HasNamedTechLevel;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.Missile;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.*;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class PropertyDescriptor {

    @Nullable
    @JsonProperty
    @Schema(description = ".")
    private OrbitalModuleDescriptor orbitalModuleDescriptor;

    @Nullable
    @JsonProperty
    @Schema(description = ".")
    private PropulsionDescriptor propulsionDescriptor;

    @Nullable
    @JsonProperty
    @Schema(description = ".")
    private PassiveModuleDescriptor passiveModuleDescriptor;

    @Nullable
    @JsonProperty
    @Schema(description = ".")
    private MissileDescriptor missileDescriptor;

    @Nullable
    @JsonProperty
    @Schema(description = ".")
    private LauncherDescriptor launcherDescriptor;

    @Nullable
    @JsonProperty
    @Schema(description = ".")
    private ArmorDescriptor armorDescriptor;

    @Nullable
    @JsonProperty
    @Schema(description = ".")
    private ElectronicWarfareDescriptor electronicWarfareDescriptor;

    @Nullable
    @JsonProperty
    @Schema(description = ".")
    private WeaponDescriptor weaponDescriptor;

    @Nullable
    @JsonProperty
    @Schema(description = ".")
    private SidewallDescriptor sidewallDescriptor;

    public PropertyDescriptor() {
    }

    public PropertyDescriptor(@Nonnull final HasName content) {
        Preconditions.checkNotNull(content, "content must not be empty");

        if (content instanceof PassiveModule) {
            this.passiveModuleDescriptor = new PassiveModuleDescriptor((PassiveModule) content);
        } else if (content instanceof OrbitalModule) {
            this.orbitalModuleDescriptor = new OrbitalModuleDescriptor((OrbitalModule) content);
        }
    }

    public PropertyDescriptor(@Nonnull final HasNamedTechLevel content) {
        Preconditions.checkNotNull(content, "content must not be empty");

        if (content instanceof Armor) {
            this.armorDescriptor = new ArmorDescriptor((Armor) content);
        } else if (content instanceof ElectronicWarfare) {
            this.electronicWarfareDescriptor = new ElectronicWarfareDescriptor((ElectronicWarfare) content);
        } else if (content instanceof Missile) {
            this.missileDescriptor = new MissileDescriptor((Missile) content);
        } else if (content instanceof Weapon) {
            this.weaponDescriptor = new WeaponDescriptor((Weapon) content);
        } else if (content instanceof Sidewall) {
            this.sidewallDescriptor = new SidewallDescriptor((Sidewall) content);
        } else if (content instanceof Launcher) {
            this.launcherDescriptor = new LauncherDescriptor((Launcher) content);
        }
    }
}

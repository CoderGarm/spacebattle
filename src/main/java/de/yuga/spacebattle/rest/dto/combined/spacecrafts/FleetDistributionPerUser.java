package de.yuga.spacebattle.rest.dto.combined.spacecrafts;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import de.yuga.spacebattle.rest.dto.orbitals.StarSystem;
import io.swagger.annotations.ApiModelProperty;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FleetDistributionPerUser {

    @Nonnull
    @ApiModelProperty(required = true, value = "The system which includes the fleets.")
    private final StarSystem starSystem;

    @Nonnull
    @ApiModelProperty(required = true, value = "The owner of the fleets in the system.")
    private final List<UserJson> users = new ArrayList<>();

    public FleetDistributionPerUser(@Nonnull final Map.Entry<de.yuga.spacebattle.backend.entities.orbitals.StarSystem, Set<User>> entry) {
        Preconditions.checkNotNull(entry, "entry shouldn't be null!");

        starSystem = new StarSystem(entry.getKey());
        users.addAll(entry.getValue().stream().map(UserJson::new).collect(Collectors.toList()));
    }

    @Nonnull
    public StarSystem getStarSystem() {
        return starSystem;
    }

    @Nonnull
    public List<UserJson> getUsers() {
        return users;
    }
}

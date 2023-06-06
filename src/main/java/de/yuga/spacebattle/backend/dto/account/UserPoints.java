package de.yuga.spacebattle.backend.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.ResearchLevel;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = ".")
public class UserPoints {

    @Nonnull
    @JsonIgnore
    private final User transientUser;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "the user.")
    private final UserJson user;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The users creation timestamp.")
    private LocalDateTime createdAt;

    @JsonProperty
    @Schema(required = true, description = "All points summed.")
    private int overallPoints = 0;

    @JsonProperty
    @Schema(required = true, description = "The colonization related points.")
    private int planetaryPoints = 0;

    @JsonProperty
    @Schema(required = true, description = "The fleet related points.")
    private int fleetPoints = 0;

    @JsonProperty
    @Schema(required = true, description = "The research related points.")
    private int researchPoints = 0;

    public UserPoints(@Nonnull final User user) {
        this.transientUser = Preconditions.checkNotNull(user, "user must not be empty");
        this.user = new UserJson(user);
        this.createdAt = user.getUserSetting().getCreatedAt();
    }

    private void sumUpPoints() {
        this.overallPoints = this.planetaryPoints + this.fleetPoints + this.researchPoints;
    }

    /**
     * - Adds 100 points per colonized planet.<br>
     * - Adds 5 point per construction level.
     */
    @Nonnull
    public UserPoints withPlanets(@Nonnull final List<Planet> planets) {
        Preconditions.checkNotNull(planets, "planets must not be empty");

        this.planetaryPoints += planets.size() * 100;
        this.planetaryPoints += planets.stream()
                .map(Planet::getConstructions)
                .flatMap(Collection::stream)
                .map(Construction::getLevel)
                .reduce(0, Integer::sum) * 5;

        sumUpPoints();
        return this;
    }

    /**
     * Adds 45 points per running colonization.
     */
    @Nonnull
    public UserPoints withColonizations(@Nonnull final List<Colonization> runningColonizations) {
        Preconditions.checkNotNull(runningColonizations, "runningColonizations must not be empty");

        this.planetaryPoints += runningColonizations.size() * 45;

        sumUpPoints();
        return this;
    }

    /**
     * Adds 1 point per running job.
     */
    @Nonnull
    public UserPoints withJobs(@Nonnull final List<Job> jobs) {
        Preconditions.checkNotNull(jobs, "jobs must not be empty");

        this.planetaryPoints += (int) jobs.stream().filter(j -> j.getConstructable().getResourceType() == EResourceType.CONSTRUCTION).count();
        this.fleetPoints += (int) jobs.stream().filter(j -> j.getConstructable().getResourceType() == EResourceType.ORBITAL_CONSTRUCTION).count();
        this.researchPoints += (int) jobs.stream().filter(j -> j.getConstructable().getResourceType() == EResourceType.RESEARCH).count();

        sumUpPoints();
        return this;
    }

    /**
     * Adds 10 points per research level.
     */
    @Nonnull
    public UserPoints withResearches(@Nonnull final Set<ResearchLevel> researches) {
        Preconditions.checkNotNull(researches, "researches must not be empty");

        this.researchPoints += researches.stream().map(ResearchLevel::getLevel).reduce(0, Integer::sum) * 10;

        sumUpPoints();
        return this;
    }

    /**
     * Adds 25 points per active warship.<br>
     * Adds 10 points per alive, inactive warship.
     */
    @Nonnull
    public UserPoints withFleets(@Nonnull final List<Fleet> fleets) {
        Preconditions.checkNotNull(fleets, "fleets must not be empty");

        final Set<WarShip> aliveShips = fleets.stream().map(Fleet::getAliveShips).flatMap(Collection::stream).filter(WarShip::isAlive).collect(Collectors.toSet());
        this.fleetPoints += aliveShips.stream().filter(WarShip::isInactive).count() * 10;
        this.fleetPoints += aliveShips.stream().filter(WarShip::isActive).count() * 25;

        sumUpPoints();
        return this;
    }
}

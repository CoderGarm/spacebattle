package de.yuga.spacebattle.backend.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.Owner;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.backend.entities.constructables.buildings.Construction;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.orbitals.Planet;
import de.yuga.spacebattle.backend.entities.researches.ResearchLevel;
import de.yuga.spacebattle.backend.entities.turn.Colonization;
import de.yuga.spacebattle.backend.entities.turn.Job;
import de.yuga.spacebattle.backend.entities.turn.mission.Mission;
import de.yuga.spacebattle.backend.enums.EResourceType;
import de.yuga.spacebattle.rest.dto.account.Player;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = ".")
public class UserPoints {

    @Nonnull
    @JsonIgnore
    private Owner transientUser;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "the user.")
    private Player user;

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

    public UserPoints() {
        // lazy idea to use only points calculation without real usage of the class
    }

    public UserPoints(@Nonnull final Owner owner) {
        this.transientUser = Preconditions.checkNotNull(owner, "owner must not be empty");
        this.user = new Player(owner);
        if (owner.getHumanOwner() != null) {
            this.createdAt = ((User) owner).getUserSetting().getCreatedAt();
        } else {
            this.createdAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.MIN);
        }
    }

    public int getPlanetaryPoints() {
        return planetaryPoints;
    }

    public int getFleetPoints() {
        return fleetPoints;
    }

    public int getResearchPoints() {
        return researchPoints;
    }

    private void sumUpPoints() {
        this.overallPoints = this.planetaryPoints + this.fleetPoints + this.researchPoints;
    }

    /**
     * - Adds 100 points per colonized planet.<br>
     * - Adds 5 point per construction level.
     */
    @Nonnull
    public UserPoints withPlanets(@Nonnull final Collection<Planet> planets) {
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
    public UserPoints withColonizations(@Nonnull final Collection<Colonization> runningColonizations) {
        Preconditions.checkNotNull(runningColonizations, "runningColonizations must not be empty");

        this.planetaryPoints += runningColonizations.size() * 45;

        sumUpPoints();
        return this;
    }

    /**
     * Adds 1 point per running job.
     */
    @Nonnull
    public UserPoints withJobs(@Nonnull final Collection<Job> jobs) {
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
    public UserPoints withResearches(@Nonnull final Collection<ResearchLevel> researches) {
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
    public UserPoints withFleets(@Nonnull final Collection<Fleet> fleets) {
        Preconditions.checkNotNull(fleets, "fleets must not be empty");

        final Set<WarShip> warShips = fleets.stream().map(Fleet::getAliveShips).flatMap(Collection::stream).filter(WarShip::isAlive).collect(Collectors.toSet());
        this.fleetPoints += (int) (warShips.stream().filter(WarShip::isInactive).count() * 10);
        this.fleetPoints += (int) (warShips.stream().filter(WarShip::isActive).count() * 25);

        sumUpPoints();
        return this;
    }

    @Nonnull
    public UserPoints withMissions(@Nonnull final Collection<Mission> fleets) {
        Preconditions.checkNotNull(fleets, "fleets must not be empty");

        final Set<WarShip> warShips = fleets.stream().map(Mission::getShips).flatMap(Collection::stream).collect(Collectors.toSet());
        this.fleetPoints += (int) (warShips.stream().filter(WarShip::isActive).count() * 9);

        sumUpPoints();
        return this;
    }

    @Nonnull
    public UserPoints withMothball(@Nonnull final Set<WarShip> warShips) {
        Preconditions.checkNotNull(warShips, "warShips must not be empty");

        this.fleetPoints += (int) (warShips.stream().filter(WarShip::isInactive).count() * 8);
        this.fleetPoints += (int) (warShips.stream().filter(WarShip::isActive).count() * 20);

        sumUpPoints();
        return this;
    }
}

package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.turn.Constructable;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import de.yuga.spacebattle.rest.dto.buildings.Building;
import de.yuga.spacebattle.rest.dto.constructables.buildings.Construction;
import de.yuga.spacebattle.rest.dto.enums.EResourceType;
import de.yuga.spacebattle.rest.dto.orbitals.Planet;
import de.yuga.spacebattle.rest.dto.researches.Research;
import de.yuga.spacebattle.rest.dto.spacecrafts.ShipClass;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class Job {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The owner.")
    private UserJson user;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The facility.")
    private Construction facility;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The planet where the facility is at.")
    private Planet facilityPlanet;

    @JsonProperty
    @Schema(required = true, description = "The left duration of this job.")
    private long ticksLeft;

    /**
     * The resource type which must be invested to run the job.
     */
    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The type of costs.")
    private EResourceType resourceType;

    @JsonProperty
    @Schema(required = true, description = "Is this a building job build.")
    private boolean isBuildingJob;

    @JsonProperty
    @Schema(required = true, description = "Is this a shipyard job.")
    private boolean isShipyardJob;

    @JsonProperty
    @Schema(required = true, description = "Is this a research job.")
    private boolean isResearchJob;

    @Nullable
    @JsonProperty
    @Schema(description = "If this is a research job.")
    private Research researchTarget;

    @Nullable
    @JsonProperty
    @Schema(description = "If this is a building job.")
    private Building buildingTarget;

    @Nullable
    @JsonProperty
    @Schema(description = "The targeted level if it is a research or building.")
    private Integer targetLevel;

    @Nullable
    @JsonProperty
    @Schema(description = "If this is a shipyard job.")
    private ShipClass shipYardTarget;

    @Nullable
    @JsonProperty
    @Schema(description = "The targeted amount of ships in case of an shipyard job..")
    private Integer amountShips;

    public Job() {
    }

    public Job(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Job job,
               @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(job, "job shouldn't be null!");

        this.user = new UserJson(job.getOwner());
        this.facility = new Construction(job.getFacility(), languageCode);
        this.facilityPlanet = new Planet(job.getFacility().getPlanet());
        this.ticksLeft = job.getJobDoneAtZero();
        final Constructable constructable = job.getConstructable();
        this.resourceType = new EResourceType(constructable.getResourceType());
        this.isBuildingJob = constructable.getBuilding() != null;
        this.isShipyardJob = constructable.getShipClass() != null;
        this.isResearchJob = constructable.getResearch() != null;
        if (isBuildingJob) {
            this.buildingTarget = new Building(constructable.getBuilding(), languageCode);
        }
        if (isResearchJob) {
            this.researchTarget = new Research(constructable.getResearch(), languageCode);
        }
        if (isShipyardJob) {
            this.shipYardTarget = new ShipClass(constructable.getShipClass(), languageCode);

        }
        this.targetLevel = constructable.getTargetLevel();
        this.amountShips = constructable.getAmountShips();
    }
}

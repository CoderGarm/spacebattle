package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.Constructable;
import de.yuga.spacebattle.rest.dto.account.UserJson;
import de.yuga.spacebattle.rest.dto.buildings.Building;
import de.yuga.spacebattle.rest.dto.constructables.buildings.Construction;
import de.yuga.spacebattle.rest.dto.enums.EResourceType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Schema(description = ".")
public class Job {

    @Nonnull
    @Schema(required = true, description = "The owner.")
    private UserJson user;

    @Nonnull
    @Schema(required = true, description = "The facility.")
    private Construction facility;

    @Schema(required = true, description = "The left duration of this job.")
    private long ticksLeft;

    /**
     * The resource type which must be invested to run the job.
     */
    @Nonnull
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
    @Schema(description = "If this if a research job.")
    private String researchTarget;

    @Nullable
    @JsonProperty
    @Schema(description = "If this if a building job.")
    private Building buildingTarget;

    @Nullable
    @JsonProperty
    @Schema(description = "The targeted level if it is a research or building.")
    private Integer targetLevel;

    @Nullable
    @JsonProperty
    @Schema(description = "If this if a shipyard job.")
    private String shipYardTarget;

    @Nullable
    @JsonProperty
    @Schema(description = "The targeted amount of ships in case of an shipyard job..")
    private Integer amountShips;

    public Job() {
    }

    public Job(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Job job) {
        Preconditions.checkNotNull(job, "job shouldn't be null!");

        this.user = new UserJson(job.getOwner());
        this.facility = new Construction(job.getFacility());
        this.ticksLeft = job.getJobDoneAtZero();
        final Constructable constructable = job.getConstructable();
        this.resourceType = new EResourceType(constructable.getResourceType());
        this.isBuildingJob = constructable.getBuilding() != null;
        this.isShipyardJob = constructable.getShipClass() != null;
        this.isResearchJob = constructable.getResearch() != null;
        if (isBuildingJob) {
            this.buildingTarget = new Building(constructable.getBuilding());
        }
        if (isResearchJob) {
            this.researchTarget = constructable.getResearch().getName();
        }
        if (isShipyardJob) {
            this.shipYardTarget = constructable.getShipClass().getName();

        }
        this.targetLevel = constructable.getTargetLevel();
        this.amountShips = constructable.getAmountShips();
    }

    @Nonnull
    public UserJson getUser() {
        return user;
    }

    @Nonnull
    public Construction getFacility() {
        return facility;
    }

    public long getTicksLeft() {
        return ticksLeft;
    }

    @Nonnull
    public EResourceType getResourceType() {
        return resourceType;
    }

    @JsonIgnore
    public boolean isBuildingTarget() {
        return isBuildingJob;
    }

    @JsonIgnore
    public boolean isShipyardJob() {
        return isShipyardJob;
    }

    @JsonIgnore
    public boolean isResearchTarget() {
        return isResearchJob;
    }

    @Nullable
    public String getResearchTarget() {
        return researchTarget;
    }

    @Nullable
    public Building getBuildingTarget() {
        return buildingTarget;
    }

    @Nullable
    public Integer getTargetLevel() {
        return targetLevel;
    }

    @Nullable
    public String getShipYardTarget() {
        return shipYardTarget;
    }

    @Nullable
    public Integer getAmountShips() {
        return amountShips;
    }
}

package de.yuga.spacebattle.rest.dto.turn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.calculator.resource.JobCostsCalculator;
import de.yuga.spacebattle.backend.dto.research.EmpireResearchCapability;
import de.yuga.spacebattle.backend.entities.combined.spacecrafts.FleetSnapshot;
import de.yuga.spacebattle.backend.entities.constructables.spacecrafts.WarShip;
import de.yuga.spacebattle.backend.entities.turn.Constructable;
import de.yuga.spacebattle.backend.entities.turn.OrbitalModuleJobElement;
import de.yuga.spacebattle.backend.entities.turn.battle.combat.WarshipHealthStateSnapshot;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EJobPriority;
import de.yuga.spacebattle.rest.dto.account.Player;
import de.yuga.spacebattle.rest.dto.buildings.Building;
import de.yuga.spacebattle.rest.dto.combined.spacecrafts.Fleet;
import de.yuga.spacebattle.rest.dto.constructables.buildings.Construction;
import de.yuga.spacebattle.rest.dto.constructables.spacecrafts.OrbitalStructures;
import de.yuga.spacebattle.rest.dto.enums.EResourceType;
import de.yuga.spacebattle.rest.dto.orbitals.Planet;
import de.yuga.spacebattle.rest.dto.researches.Research;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Schema(description = ".")
public class Job {

    @JsonProperty
    @Schema(required = true, description = "The id")
    private int idJob;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The owner.")
    private Player user;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The facility.")
    private Construction facility;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The planet where the facility is at.")
    private Planet facilityPlanet;

    @JsonProperty
    @Schema(description = "The left duration of this job.")
    private Integer ticksLeft;

    @JsonProperty
    @Schema(required = true, description = "The left construction points of this job.")
    private long pointsLeft;

    /**
     * The resource type which must be invested to run the job.
     */
    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The type of costs.")
    private EResourceType resourceType;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The priority of the job.")
    private EJobPriority priority;

    @JsonProperty
    @Schema(required = true, description = "Is this a building job build.")
    private boolean isBuildingJob;

    @JsonProperty
    @Schema(required = true, description = "Is this a shipyard job.")
    private boolean isShipyardJob;

    @JsonProperty
    @Schema(required = true, description = "Is this a repair job.")
    private boolean isRepairJob;

    @JsonProperty
    @Schema(required = true, description = "Is this a upgrade job.")
    private boolean isUpgradeJob;

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
    @Schema(description = "The fleet which will be repaired in case of an shipyard job.")
    private Fleet fleet;

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The orbital structures, when build.")
    private List<OrbitalStructures> orbitalStructures = new ArrayList<>();

    public Job() {
    }

    public Job(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Job job,
               @Nonnull final ResourceDeposit ticklyIncome,
               @Nonnull final ResourceDeposit resourceDeposit,
               @Nonnull final String languageCode) {
        this(job, languageCode);
        Preconditions.checkNotNull(ticklyIncome, "ticklyIncome must not be empty");
        Preconditions.checkNotNull(resourceDeposit, "resourceDeposit must not be empty");

        this.ticksLeft = JobCostsCalculator.calculateRemainingTicks(job, ticklyIncome, resourceDeposit);
    }

    /**
     * Creates a job with no ticks left!
     */
    public Job(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Job job,
               @Nonnull final String languageCode) {
        Preconditions.checkNotNull(languageCode, "languageCode must not be empty");
        Preconditions.checkNotNull(job, "job shouldn't be null!");

        this.idJob = job.getId();
        this.user = new Player(job.getOwner());
        this.facility = new Construction(job.getFacility(), languageCode);
        this.facilityPlanet = new Planet(job.getFacility().getPlanet());
        this.pointsLeft = job.getPointsLeft();
        this.priority = job.getPriority();
        final Constructable constructable = job.getConstructable();
        this.resourceType = new EResourceType(constructable.getResourceType());
        this.isBuildingJob = constructable.getBuilding() != null;
        if (isBuildingJob) {
            this.buildingTarget = new Building(constructable.getBuilding(), languageCode);
        }
        this.isResearchJob = constructable.getResearch() != null;
        if (isResearchJob) {
            this.researchTarget = new Research(constructable.getResearch(), languageCode);
        }
        this.isShipyardJob = constructable.isShipyardJob();
        if (isShipyardJob) {
            this.isRepairJob = job.getConstructable().isRepairJob();
            this.isUpgradeJob = job.getConstructable().isUpgradeJob();

            final de.yuga.spacebattle.backend.entities.combined.spacecrafts.Fleet fleet = constructable.getFleet();
            final FleetSnapshot fleetSnapshot = constructable.getFleetSnapshot();
            if (fleet != null | fleetSnapshot != null) {
                final Set<WarShip> ships = fleet != null ? (constructable.isUpgradeJob() ? fleet.getAliveShips() : fleet.getAllShips()) : Objects.requireNonNull(fleetSnapshot).getShips().stream().map(WarshipHealthStateSnapshot::getWarShip).collect(Collectors.toSet());
                this.fleet = new Fleet(fleet != null ? fleet : fleetSnapshot.getFleet(), ships, languageCode);
            }

            final Set<OrbitalModuleJobElement> orbitalModuleJobElements = job.getConstructable().getOrbitalModuleJobElements();
            orbitalStructures.addAll(orbitalModuleJobElements.stream().map(e -> new OrbitalStructures(e, job.getFacility().getPlanet(), languageCode)).collect(Collectors.toList()));
        }
        this.targetLevel = constructable.getTargetLevel();
    }

    public Job(@Nonnull final de.yuga.spacebattle.backend.entities.turn.Job researchJob,
               @Nonnull final EmpireResearchCapability capability,
               @Nonnull final String preferredLanguage) {
        this(researchJob, preferredLanguage);
        Preconditions.checkNotNull(capability, "capability must not be empty");

        final long empireWideResearchPoints = capability.getEmpireWideResearchPoints();
        final long empireWideResearchPointsLeftOver = capability.getEmpireWideResearchPointsLeftOver();
        this.ticksLeft = JobCostsCalculator.calculateRemainingTicks(empireWideResearchPoints, empireWideResearchPointsLeftOver, researchJob.getPointsLeft());
    }
}

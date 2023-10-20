package de.yuga.spacebattle.rest.dto.misc;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.yuga.spacebattle.backend.enums.EShipClassType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Only to transfer enum values to the frontend.")
public class EnumValueDto {


    @JsonProperty
    private de.yuga.spacebattle.backend.enums.EModuleType[] eModuleTypes;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.EDepositType[] eDepositType;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.EResourceType[] eResourceType;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.EEducationType[] eEducationType;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.ECollectableType[] eCollectableType;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.ECalculationType[] eCalculationType;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.EAlignmentType[] eAlignmentType;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.EHitArea[] eHitArea;
    @JsonProperty
    private EShipClassType[] eShipClassType;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.EProductionCategory[] eProductionCategory;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.ERefinementSequence[] eRefinementSequence;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.ESupportType[] eSupportType;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.EWarheadType[] eWarheadType;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.EWeaponAlignment[] eWeaponAlignment;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.EWeaponType[] eWeaponType;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.EWikiCategory[] eWikiCategories;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.EMissionType[] eMissionTypes;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.ECapacityAreaType[] eCapacityAreaTypes;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.ETechnologyType[] eTechnologyTypes;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.physics.EAccelerationMetric[] eAccelerationMetrics;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.physics.EDistanceMetric[] eDistanceMetrics;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.physics.EHyperBand[] eHyperBands;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.physics.EMassMetric[] eMassMetrics;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.physics.ETimeMetric[] eTimeMetrics;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.EWebUserRole[] eWebUserRoles;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.EJobType[] eJobTypes;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.EStarNation[] eStarNations;
    @JsonProperty
    private de.yuga.spacebattle.backend.enums.EJobPriority[] eJobPriorities;
}

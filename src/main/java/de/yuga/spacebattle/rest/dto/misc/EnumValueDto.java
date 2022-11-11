package de.yuga.spacebattle.rest.dto.misc;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Only to transfer enum values to the frontend.")
public class EnumValueDto {


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
    private de.yuga.spacebattle.backend.enums.EHullType[] eHullType;
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
}

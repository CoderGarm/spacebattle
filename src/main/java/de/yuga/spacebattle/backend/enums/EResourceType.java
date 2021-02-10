package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum EResourceType {

    CONSTRUCTION("Construction Point", "Construction Points", new ERaceType[]{}, EBuildingType.JOB),
    ORBITALCONSTRUCTION("Shipyard Construction Point", "Shipyard Construction Points", new ERaceType[]{}, EBuildingType.JOB),
    RESEARCH("Research Point", "Research Point", new ERaceType[]{}, EBuildingType.JOB),
    CREDITS("Credit", "Credits", new ERaceType[]{}, EBuildingType.PRODUCING),
    METALORE("Metalore", "Metalore", new ERaceType[]{}, EBuildingType.PRODUCING),
    MERCURIUM("Mercurium", "Mercurium", new ERaceType[]{ERaceType.KANDORIAN}, EBuildingType.PRODUCING),
    HYPERONIUM("Hyperonium", "Hyperonium", new ERaceType[]{ERaceType.HUMAN}, EBuildingType.PRODUCING);

    @Nonnull
    private final String singularName;

    @Nonnull
    private final String pluralName;

    @Nonnull
    private final ERaceType[] raceTypes;

    @Nonnull
    private final EBuildingType buildingType;

    EResourceType(@Nonnull final String singularName,
                  @Nonnull final String pluralName,
                  @Nonnull final ERaceType[] raceTypes,
                  @Nonnull final EBuildingType buildingType) {
        Preconditions.checkNotNull(singularName, "singularName shouldn't be null!");
        Preconditions.checkNotNull(pluralName, "pluralName shouldn't be null!");
        Preconditions.checkNotNull(raceTypes, "raceTypes shouldn't be null!");
        Preconditions.checkNotNull(buildingType, "buildingType shouldn't be null!");

        this.singularName = singularName;
        this.pluralName = pluralName;
        this.raceTypes = raceTypes;
        this.buildingType = buildingType;
    }

    @Nonnull
    public String getSingularName() {
        return singularName;
    }

    @Nonnull
    public String getPluralName() {
        return pluralName;
    }

    @Nonnull
    public ERaceType[] getRaceTypes() {
        return raceTypes;
    }

    @Nonnull
    public List<EResourceType> getResourceListFor(ERaceType raceType) {
        List<EResourceType> collect = Arrays.stream(EResourceType.values()).filter(rt -> Arrays.asList(rt.getRaceTypes()).contains(raceType)).collect(Collectors.toList());
        List<EResourceType> collect1 = Arrays.stream(EResourceType.values()).filter(rt -> false).collect(Collectors.toList());

        collect1.addAll(collect);
        return collect;
    }

    @Nonnull
    public EBuildingType getBuildingType() {
        return buildingType;
    }
}

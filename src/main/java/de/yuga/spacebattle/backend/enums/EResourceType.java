package de.yuga.spacebattle.backend.enums;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

public enum EResourceType {

    CONSTRUCTION("Construction Point", "Construction Points", EBuildingType.JOB, EIconPath.RESOURCES.getPath(), "construction"),
    ORBITALCONSTRUCTION("Shipyard Construction Point", "Shipyard Construction Points", EBuildingType.JOB, EIconPath.RESOURCES.getPath(), "orbitalconstruction"),
    RESEARCH("Research Point", "Research Point", EBuildingType.JOB, EIconPath.RESOURCES.getPath(), "research"),
    CREDITS("Credit", "Credits", EBuildingType.PRODUCING, EIconPath.RESOURCES.getPath(), "credit"),
    METALORE("Metalore", "Metalore", EBuildingType.PRODUCING, EIconPath.RESOURCES.getPath(), "metalore"),
    MERCURIUM("Mercurium", "Mercurium", EBuildingType.PRODUCING, EIconPath.RESOURCES.getPath(), "mercurium"),
    HYPERONIUM("Hyperonium", "Hyperonium", EBuildingType.PRODUCING, EIconPath.RESOURCES.getPath(), "hyperonium");

    @Nonnull
    private final String singularName;

    @Nonnull
    private final String pluralName;

    @Nonnull
    private final EBuildingType buildingType;

    @Nonnull
    final String directory;

    @Nonnull
    final String iconName;

    EResourceType(@Nonnull final String singularName,
                  @Nonnull final String pluralName,
                  @Nonnull final EBuildingType buildingType,
                  @Nonnull final String directory,
                  @Nonnull final String iconName) {
        Preconditions.checkNotNull(singularName, "singularName shouldn't be null!");
        Preconditions.checkNotNull(pluralName, "pluralName shouldn't be null!");
        Preconditions.checkNotNull(buildingType, "buildingType shouldn't be null!");
        Preconditions.checkNotNull(directory, "directory shouldn't be null!");
        Preconditions.checkNotNull(iconName, "iconName shouldn't be null!");

        this.singularName = singularName;
        this.pluralName = pluralName;
        this.buildingType = buildingType;
        this.directory = directory;
        this.iconName = iconName;
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
    public EBuildingType getBuildingType() {
        return buildingType;
    }

    @Nonnull
    public String getDirectory() {
        return directory;
    }

    @Nonnull
    public String getIconName() {
        return iconName;
    }
}

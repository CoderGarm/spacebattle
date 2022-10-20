package de.yuga.spacebattle.backend.transformer;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.Building;

import javax.annotation.Nonnull;

public class BuildingCsvTransformer extends CSVTransformer<Building> {

    public BuildingCsvTransformer(final boolean withoutHeader, @Nonnull final String preferredLanguage) {
        super(withoutHeader, preferredLanguage);
    }

    @Override
    protected void createHeader() {
        headers.add("idBuilding");
        headers.add("baseValue");
        headers.add("increasingFactorPerLevel");
        headers.add(dependencies.get(0).getHeadersString());
        headers.add(dependencies.get(1).getHeadersString());
    }

    @Override
    protected void getDependencies(@Nonnull final String preferredLanguage) {
        dependencies.add(new HasCostsCsvTransformer(true, preferredLanguage));
        dependencies.add(new ProductionTypeCsvTransformer(true, preferredLanguage));
    }

    public BuildingCsvTransformer(@Nonnull final String preferredLanguage) {
        super(preferredLanguage);
    }

    @Override
    protected void convertInternally(@Nonnull final Building toTransform) {
        Preconditions.checkNotNull(toTransform, "building must not be empty");

        a(toTransform.getId() + "");
        a(toTransform.getBaseValue() + "");
        a(toTransform.getIncreasingFactorPerLevel() + "");
        a(((HasCostsCsvTransformer) dependencies.get(0)).convert(toTransform));
        a(((ProductionTypeCsvTransformer) dependencies.get(1)).convert(toTransform.getProductionType()));

    }
}

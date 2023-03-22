package de.yuga.spacebattle.backend.transformer;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModule;
import de.yuga.spacebattle.backend.enums.physics.EMassMetric;

import javax.annotation.Nonnull;

public class BaseModuleCsvTransformer extends CSVTransformer<BaseModule> {


    public BaseModuleCsvTransformer(final boolean withoutHeader, @Nonnull final String preferredLanguage) {
        super(withoutHeader, preferredLanguage);
    }

    public BaseModuleCsvTransformer(@Nonnull final String preferredLanguage) {
        super(preferredLanguage);
    }

    @Override
    protected void createHeader() {
        headers.add(dependencies.get(0).getHeadersString());
        headers.add("tonnage");
    }

    @Override
    protected void getDependencies(@Nonnull final String preferredLanguage) {
        dependencies.add(new HasCostsCsvTransformer(true, preferredLanguage));
    }

    @Override
    protected void convertInternally(@Nonnull final BaseModule toTransform) {
        Preconditions.checkNotNull(toTransform, "toTransform must not be empty");

        a(((HasCostsCsvTransformer) dependencies.get(0)).convert(toTransform));
        a(toTransform.getTonnage().convertToMetric(EMassMetric.T).getCoordinate().intValue());
    }
}

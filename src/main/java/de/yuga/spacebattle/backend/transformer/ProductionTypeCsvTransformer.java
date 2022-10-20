package de.yuga.spacebattle.backend.transformer;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.buildings.ProductionType;

import javax.annotation.Nonnull;

public class ProductionTypeCsvTransformer extends CSVTransformer<ProductionType> {

    public ProductionTypeCsvTransformer(final boolean withoutHeader, @Nonnull final String preferredLanguage) {
        super(withoutHeader, preferredLanguage);
    }

    public ProductionTypeCsvTransformer(@Nonnull final String preferredLanguage) {
        super(preferredLanguage);
    }

    @Override
    protected void createHeader() {
        headers.add("EResourceType");
        headers.add("EProductionCategory");
        headers.add("ERefinementSequence");
    }

    @Override
    protected void getDependencies(@Nonnull final String preferredLanguage) {
    }


    @Override
    protected void convertInternally(@Nonnull final ProductionType toTransform) {
        Preconditions.checkNotNull(toTransform, "toTransform must not be empty");

        a(toTransform.getProductionTarget());
        a(toTransform.getProductionCategory());
        a(toTransform.getRefinementSequence());
    }
}

package de.yuga.spacebattle.backend.transformer;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.researches.Research;

import javax.annotation.Nonnull;

public class ResearchCsvTransformer extends CSVTransformer<Research> {

    public ResearchCsvTransformer(final boolean withoutHeader, @Nonnull final String preferredLanguage) {
        super(withoutHeader, preferredLanguage);
    }

    public ResearchCsvTransformer(@Nonnull final String preferredLanguage) {
        super(preferredLanguage);
    }

    @Override
    protected void createHeader() {
        headers.add("idResearch");
        headers.add(dependencies.get(0).getHeadersString());
    }

    @Override
    protected void getDependencies(@Nonnull final String preferredLanguage) {
        dependencies.add(new HasCostsCsvTransformer(true, preferredLanguage));
    }

    @Override
    protected void convertInternally(@Nonnull final Research toTransform) {
        Preconditions.checkNotNull(toTransform, "building must not be empty");

        a(toTransform.getId());
        a(((HasCostsCsvTransformer) dependencies.get(0)).convert(toTransform));
    }
}

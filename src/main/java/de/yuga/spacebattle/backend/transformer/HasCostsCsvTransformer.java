package de.yuga.spacebattle.backend.transformer;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.misc.HasCosts;
import de.yuga.spacebattle.backend.entities.turn.resources.ResourceDeposit;
import de.yuga.spacebattle.backend.enums.EEducationType;
import de.yuga.spacebattle.backend.enums.EResourceType;

import javax.annotation.Nonnull;

public class HasCostsCsvTransformer extends CSVTransformer<HasCosts> {

    public HasCostsCsvTransformer(final boolean withoutHeader, @Nonnull final String preferredLanguage) {
        super(withoutHeader, preferredLanguage);
    }

    public HasCostsCsvTransformer(@Nonnull final String preferredLanguage) {
        super(preferredLanguage);
    }

    @Override
    protected void createHeader() {
        headers.add("Name");
        headers.add("TechLevel");
        for (final EResourceType eResourceType : EResourceType.valuesWithoutPopulation()) {
            headers.add(eResourceType.name());
        }
        for (final EEducationType eEducationType : EEducationType.values()) {
            headers.add(eEducationType.name());
        }
    }

    @Override
    protected void getDependencies(@Nonnull final String preferredLanguage) {

    }


    @Override
    protected void convertInternally(@Nonnull final HasCosts toTransform) {
        Preconditions.checkNotNull(toTransform, "toTransform must not be empty");

        a(toTransform.getName(getPreferredLanguage()));
        a(toTransform.getTechLevel());
        final ResourceDeposit costs = toTransform.getCosts();
        for (final EResourceType eResourceType : EResourceType.valuesWithoutPopulation()) {
            final long amount = costs.getResourceAmountByType(eResourceType);
            a(amount);
        }
        for (final EEducationType eEducationType : EEducationType.values()) {
            final long amount = costs.getCrewAmountByType(eEducationType);
            a(amount);
        }
    }
}

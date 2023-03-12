package de.yuga.spacebattle.backend.transformer;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.ammunition.MissileMotor;

import javax.annotation.Nonnull;

public class MissileMotorCsvTransformer extends CSVTransformer<MissileMotor> {

    public MissileMotorCsvTransformer(final boolean withoutHeader, @Nonnull final String preferredLanguage) {
        super(withoutHeader, preferredLanguage);
    }

    public MissileMotorCsvTransformer(@Nonnull final String preferredLanguage) {
        super(preferredLanguage);
    }

    @Override
    protected void createHeader() {
        headers.add("idMissileMotor");
        headers.add(dependencies.get(0).getHeadersString());
        headers.add("endurance");
        headers.add("acceleration");
        headers.add("maneuverability");
        headers.add("useCapacity");
    }

    @Override
    protected void getDependencies(@Nonnull final String preferredLanguage) {
        dependencies.add(new HasCostsCsvTransformer(true, preferredLanguage));
    }

    @Override
    protected void convertInternally(@Nonnull final MissileMotor toTransform) {
        Preconditions.checkNotNull(toTransform, "building must not be empty");

        /*
        a(toTransform.getId());
        a(((HasCostsCsvTransformer) dependencies.get(0)).convert(toTransform));
        a(toTransform.getEndurance());
        a(toTransform.getAcceleration().asString());
        a(toTransform.getManeuverability());
        a(toTransform.getUseCapacity());
        */
    }
}

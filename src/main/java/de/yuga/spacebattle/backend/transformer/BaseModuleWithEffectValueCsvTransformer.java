package de.yuga.spacebattle.backend.transformer;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.spacecrafts.modules.basics.BaseModuleWithEffectValue;

import javax.annotation.Nonnull;

public class BaseModuleWithEffectValueCsvTransformer extends CSVTransformer<BaseModuleWithEffectValue> {


    public BaseModuleWithEffectValueCsvTransformer(final boolean withoutHeader, @Nonnull final String preferredLanguage) {
        super(withoutHeader, preferredLanguage);
    }

    public BaseModuleWithEffectValueCsvTransformer(@Nonnull final String preferredLanguage) {
        super(preferredLanguage);
    }

    @Override
    protected void createHeader() {
        headers.add(dependencies.get(0).getHeadersString());
        headers.add("effectValue");
    }

    @Override
    protected void getDependencies(@Nonnull final String preferredLanguage) {
        dependencies.add(new BaseModuleCsvTransformer(true, preferredLanguage));
    }

    @Override
    protected void convertInternally(@Nonnull final BaseModuleWithEffectValue toTransform) {
        Preconditions.checkNotNull(toTransform, "toTransform must not be empty");

        a(((BaseModuleCsvTransformer) dependencies.get(0)).convert(toTransform));
        a(toTransform.getEffectValue());
    }
}

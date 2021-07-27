package de.yuga.spacebattle.rest.dto.enums;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EProductionCategory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class EProductionCategoryList extends ArrayList<EProductionCategory> {

    public EProductionCategoryList(@Nonnull final List<EProductionCategory> eProductionCategories) {
        Preconditions.checkNotNull(eProductionCategories, "eProductionCategories shouldn't be null!");

        addAll(eProductionCategories);
    }
}

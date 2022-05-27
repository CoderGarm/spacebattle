package de.yuga.spacebattle.rest.dto.researches;

import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Schema(description = "The research tree contains basic information about all researches and kind of a linked list by the tech tree's single elements itself.")
public class ResearchTree {

    @Nonnull
    @Schema(required = true, description = "The elements of the tech tree which know their 'predecessor' and 'successor'.")
    private final List<ResearchTreeElement> treeElements;

    @Nonnull
    @Schema(required = true, description = "The researches which are part of the treeElements.")
    private final List<Research> researches;

    public ResearchTree(@Nonnull final Collection<ResearchTreeElement> treeElements, @Nonnull final List<Research> researches) {
        Preconditions.checkNotNull(treeElements, "treeElements shouldn't be null!");
        Preconditions.checkNotNull(researches, "researches shouldn't be null!");

        this.treeElements = new ArrayList<>(treeElements);
        this.researches = researches;
    }

    @Nonnull
    public List<ResearchTreeElement> getTreeElements() {
        return treeElements;
    }

    @Nonnull
    public List<Research> getResearches() {
        return researches;
    }
}

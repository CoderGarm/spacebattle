package de.yuga.spacebattle.rest.dto.researches;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

@Schema(description = "The research tree contains basic information about all researches and kind of a linked list by the tech tree's single elements itself.")
public class ResearchTree {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The elements of the tech tree which know their 'predecessor' and 'successor'.")
    private final Set<ResearchTreeChain> researchTreeChains = new HashSet<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The researches which are part of the treeElements.")
    private final List<Research> researches;

    public ResearchTree(@Nonnull final Collection<ResearchTreeElement> treeElements, @Nonnull final List<Research> researches) {
        Preconditions.checkNotNull(treeElements, "treeElements shouldn't be null!");
        Preconditions.checkNotNull(researches, "researches shouldn't be null!");

        final Map<Integer, List<ResearchTreeElement>> map = new HashMap<>();
        for (final ResearchTreeElement treeElement : treeElements) {
            final int idResearch = treeElement.getIdResearch();
            final List<ResearchTreeElement> connectedElements = treeElements.stream().filter(t -> t.isPartOfChain(idResearch)).collect(Collectors.toList());
            map.put(idResearch, connectedElements);
        }

        map.forEach((initialIdResearch, researchTreeElements) -> {
            final Set<Integer> connectedIdResearches = researchTreeElements.stream().map(ResearchTreeElement::getIdResearch).collect(Collectors.toSet());
            final Set<ResearchTreeElement> singleChain = connectedIdResearches.stream()
                    .map(idResearch -> treeElements.stream().filter(t -> t.isPartOfChain(idResearch)).collect(Collectors.toList()))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            researchTreeChains.add(new ResearchTreeChain(singleChain));
        });
        this.researches = researches;
    }
}

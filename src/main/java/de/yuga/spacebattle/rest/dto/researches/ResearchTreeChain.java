package de.yuga.spacebattle.rest.dto.researches;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

@Schema(description = ".")
public class ResearchTreeChain {

    @Nonnull
    @JsonProperty
    @Schema(required = true, description = "The elements of this research chain.")
    private final Set<ResearchTreeElement> treeElements = new HashSet<>();

    public ResearchTreeChain(@Nonnull final Set<ResearchTreeElement> singleChain) {
        Preconditions.checkNotNull(singleChain, "singleChain must not be empty");

        treeElements.addAll(singleChain);
    }

    public boolean contains(final int idResearch) {
        return treeElements.stream().filter(t -> t.isPartOfChain(idResearch)).findFirst().orElse(null) != null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final ResearchTreeChain that = (ResearchTreeChain) o;

        return new EqualsBuilder().append(treeElements, that.treeElements).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(treeElements).toHashCode();
    }
}

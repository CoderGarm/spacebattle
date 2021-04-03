package de.yuga.spacebattle.gui.vaadin.research.details;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.researches.Research;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Wraps a {@link Research} and it's level.
 */
public class ResearchLevelDTO {

    @Nonnull
    private final Research research;

    @Nonnull
    private Integer level;

    public ResearchLevelDTO(@Nonnull final Research research, @Nonnull final Integer level) {
        Preconditions.checkNotNull(research, "research shouldn't be null!");
        Preconditions.checkNotNull(level, "amount shouldn't be null!");

        this.research = research;
        this.level = level;
    }

    @Nonnull
    public Research getResearch() {
        return research;
    }

    @Nonnull
    public Integer getLevel() {
        return level;
    }

    public void setLevel(@Nonnull final Integer level) {
        Preconditions.checkNotNull(level, "level shouldn't be null!");

        this.level = level;
    }

    public String getLevelString() {
        return "Level: " + level;
    }

    /**
     * Necessary while vaadin data binding uses this entry to compute further.
     *
     * @return the entry which represents this wrapper
     */
    public Map.Entry<Research, Integer> getAsEntry() {
        return new Map.Entry<Research, Integer>() {
            @Override
            public Research getKey() {
                return research;
            }

            @Override
            public Integer getValue() {
                return level;
            }

            @Override
            public Integer setValue(Integer value) {
                level = value;
                return level;
            }
        };
    }
}

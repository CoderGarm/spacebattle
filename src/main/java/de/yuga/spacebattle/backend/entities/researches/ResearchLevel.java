package de.yuga.spacebattle.backend.entities.researches;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.entities.misc.AbstractEntityKey;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * This represents a research and the level onto the research is processed.
 */
@Entity
@Table(name = "researchLevels")
@AttributeOverride(name = "id", column = @Column(name = "idResearchLevel"))
public class ResearchLevel extends AbstractEntityKey {

    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idUser")
    private User user;

    /**
     * The placement.
     */
    @Nonnull
    @NotNull
    @ManyToOne
    @JoinColumn(name = "idResearch")
    private Research research;

    /**
     * The currently reached level.
     */
    @Min(0)
    private int level;

    public ResearchLevel() {
    }

    public ResearchLevel(@Nonnull final User user, @Nonnull final Research research) {
        Preconditions.checkNotNull(user, "user shouldn't be null!");
        Preconditions.checkNotNull(research, "research shouldn't be null!");

        this.user = user;
        this.research = research;
        this.level = 1;
    }

    @Nonnull
    public User getUser() {
        return user;
    }

    @Nonnull
    public Research getResearch() {
        return research;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(final int level) {
        this.level = level;
    }
}

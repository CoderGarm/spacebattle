package de.yuga.spacebattle.misc.fandom.battle.dto;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IndividualBattle {

    @Nonnull
    private final String name;

    @Nonnull
    private final String content;

    @Nullable
    private final WikiBattleBlock battleBlock;

    public IndividualBattle(@Nonnull final String name, @Nonnull final String content) {
        this.name = Preconditions.checkNotNull(name, "name must not be empty");
        this.content = Preconditions.checkNotNull(content, "content must not be empty");

        battleBlock = parse();
    }

    @Nullable
    private WikiBattleBlock parse() {
        return new WikiBattleBlock(content).getValid();
    }
}

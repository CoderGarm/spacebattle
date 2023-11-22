package de.yuga.spacebattle.rest.dto.misc.wiki;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.ToStringBuilder;

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

    @Nonnull
    public String getName() {
        return name;
    }

    @Nullable
    public WikiBattleBlock getBattleBlock() {
        return battleBlock;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name: ", name)
                .toString();
    }

    @Nullable
    public IndividualBattle getValid() {
        return battleBlock != null && battleBlock.isValid() ? this : null;
    }

    @Nullable
    private WikiBattleBlock parse() {
        return new WikiBattleBlock(content).getValid();
    }
}

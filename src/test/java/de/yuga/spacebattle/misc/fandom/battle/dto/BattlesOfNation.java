package de.yuga.spacebattle.misc.fandom.battle.dto;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BattlesOfNation {

    @Nonnull
    private final String nation;

    @Nonnull
    private final Map<String, String> content;

    @Nonnull
    private final List<IndividualBattle> individualBattles;

    public BattlesOfNation(@Nonnull final String nation, @Nonnull final Map<String, String> content) {
        this.nation = Preconditions.checkNotNull(nation, "nation must not be empty");
        this.content = Preconditions.checkNotNull(content, "content must not be empty");

        this.individualBattles = parse();
    }

    private List<IndividualBattle> parse() {
        return content.entrySet().stream().map(e -> new IndividualBattle(e.getKey(), e.getValue())).collect(Collectors.toList());
    }
}

package de.yuga.spacebattle.misc.fandom.battle.dto;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BattlesOfNation {

    @Nonnull
    private final String nation;

    @Nonnull
    private final Map<String, String> content;

    @Nonnull
    private final List<IndividualBattle> individualBattles = new ArrayList<>();

    public BattlesOfNation(@Nonnull final String nation, @Nonnull final Map<String, String> content) {
        this.nation = Preconditions.checkNotNull(nation, "nation must not be empty");
        this.content = Preconditions.checkNotNull(content, "content must not be empty");

        this.individualBattles.addAll(parse());
    }

    @Nonnull
    public String getNation() {
        return nation;
    }

    @Nonnull
    public List<IndividualBattle> getIndividualBattles() {
        return individualBattles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("nation: ", nation)
                .toString();
    }

    @Nonnull
    private List<IndividualBattle> parse() {
        return content.entrySet().stream()
                .map(e -> new IndividualBattle(e.getKey(), e.getValue()).getValid())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}

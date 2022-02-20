package de.yuga.spacebattle.rest.dto.turn.battle.collections;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.turn.battle.BattleReport;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class BattleReportList extends ArrayList<BattleReport> {

    public BattleReportList(@Nonnull final Collection<de.yuga.spacebattle.backend.entities.turn.battle.BattleReport> reports) {
        Preconditions.checkNotNull(reports, "reports shouldn't be null!");

        addAll(reports.stream().map(BattleReport::new).collect(Collectors.toList()));
    }
}

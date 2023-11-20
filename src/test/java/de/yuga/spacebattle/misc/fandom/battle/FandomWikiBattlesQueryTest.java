package de.yuga.spacebattle.misc.fandom.battle;

import de.yuga.spacebattle.TestUtils;
import de.yuga.spacebattle.misc.fandom.EWikiConfig;
import de.yuga.spacebattle.misc.fandom.FandomWikiQueryTest;
import de.yuga.spacebattle.misc.fandom.battle.dto.BattlesOfNation;
import io.github.fastily.jwiki.core.MQuery;
import io.github.fastily.jwiki.core.Wiki;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * <b>Attention:</b> Please be kind, do not create more than necessary traffic.
 */
@Disabled("no test")
public class FandomWikiBattlesQueryTest {

    private final String FOLDER = "battles";
    private final Set<String> NATIONAL_BATTLES = Set.of(
            "Andermani Battles",
            "Grayson Battles",
            "Havenite Battles",
            "Manticoran Battles",
            "Masadan Battles",
            "Mesan Battles",
            "Solarian Battles"
    );

    @Test
    void fetchAllBattlesFromWiki() {

        final Wiki wiki = EWikiConfig.EN.getWiki();
        for (final String nationBattleCategory : NATIONAL_BATTLES) {
            final List<String> categoryMembers = wiki.getCategoryMembers(nationBattleCategory);
            final Map<String, String> texts = MQuery.getPageText(wiki, categoryMembers);
            texts.forEach((key, value) -> {
                final String fileName = key.replaceAll(" ", "-");
                final String dir = FandomWikiQueryTest.DIR + FOLDER + FandomWikiQueryTest.FS
                        + nationBattleCategory.replaceAll(" ", "-") + FandomWikiQueryTest.FS;
                TestUtils.writeString(dir, fileName, value);
            });
        }
    }

    @Test
    void readBattles() {
        final Map<String, Map<String, String>> result = new HashMap<>();
        for (final String nationBattleCategory : NATIONAL_BATTLES) {
            final String dir = FandomWikiQueryTest.DIR + FOLDER + FandomWikiQueryTest.FS;
            final String subDir = nationBattleCategory.replaceAll(" ", "-") + FandomWikiQueryTest.FS;
            final Map<String, String> content = getContent(dir, subDir);
            result.put(nationBattleCategory, content);
        }
        final List<BattlesOfNation> battles = result.entrySet().stream()
                .map(e -> new BattlesOfNation(e.getKey(), e.getValue())).collect(Collectors.toList());
        assertNotNull(battles);

        battles.forEach(battlesOfNation -> {
            final String nation = battlesOfNation.getNation();
            battlesOfNation.getIndividualBattles().forEach(battle -> {
                final String value = battle.getBattleBlock().printDe();
                final String dir = FandomWikiQueryTest.DIR + FOLDER + "-output" + FandomWikiQueryTest.FS + nation + FandomWikiQueryTest.FS;
                TestUtils.writeString(dir, battle.getName(), value);
            });
        });
    }

    @Nonnull
    @SuppressWarnings("SameParameterValue")
    private Map<String, String> getContent(@Nonnull final String dir, @Nonnull final String filename) {
        final File[] files = new File(dir + filename).listFiles();
        assertNotNull(files);
        return Arrays.stream(files).collect(Collectors.toMap(File::getName, TestUtils::readFile));
    }
}

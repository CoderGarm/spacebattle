package de.yuga.spacebattle.misc.fandom.battle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.yuga.spacebattle.TestUtils;
import de.yuga.spacebattle.misc.fandom.EWikiConfig;
import de.yuga.spacebattle.misc.fandom.FandomWikiQueryTest;
import de.yuga.spacebattle.rest.dto.misc.wiki.BattlesOfNation;
import de.yuga.spacebattle.rest.dto.misc.wiki.IndividualBattle;
import de.yuga.spacebattle.rest.dto.misc.wiki.WikiBattleBlock;
import io.github.fastily.jwiki.core.MQuery;
import io.github.fastily.jwiki.core.Wiki;
import org.apache.commons.lang3.StringUtils;
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

    private static final Map<String, Set<String>> CONFLICT_NAMES = new HashMap<>();

    static {
        FandomWikiBattlesQueryTest.CONFLICT_NAMES.put("Axelrod Conspiracy", Set.of("Axelrod Conspiracy"));
        FandomWikiBattlesQueryTest.CONFLICT_NAMES.put("Silesian Confederacy", Set.of("Silesian Confederacy|Silesian piracy"));
        FandomWikiBattlesQueryTest.CONFLICT_NAMES.put("RMN anti-slavery operations", Set.of("RMN anti-slavery operations"));
        FandomWikiBattlesQueryTest.CONFLICT_NAMES.put("RMN anti-piracy campaign", Set.of("RMN anti-piracy campaign", "Manticoran anti-piracy action"));
        FandomWikiBattlesQueryTest.CONFLICT_NAMES.put("Anti-piracy action_Mesan Alignment operation", Set.of("Anti-piracy action_Mesan Alignment operation"));
        FandomWikiBattlesQueryTest.CONFLICT_NAMES.put("Talbott Cluster conflict", Set.of("Talbott Cluster conflict"));
        FandomWikiBattlesQueryTest.CONFLICT_NAMES.put("Solarian-Manticoran War", Set.of("Solarian-Manticoran War"));
        FandomWikiBattlesQueryTest.CONFLICT_NAMES.put("Operation Ferret", Set.of("Operation Ferret"));
        FandomWikiBattlesQueryTest.CONFLICT_NAMES.put("Maccabeus Campaign", Set.of("Maccabeus Campaign"));
        FandomWikiBattlesQueryTest.CONFLICT_NAMES.put("First Havenite-Manticoran War", Set.of(
                "Havenite Operation Perseus - First Havenite-Manticoran War",
                "First Havenite-Manticoran War",
                "First Manticoran-Havenite Waranti-piracy operations and commerce raiding in Silesian Confederacy|Silesia",
                "Havenite Operation Perseus prior to the First Havenite-Manticoran War",
                "People's Republic of Haven|Havenite pre-war attempt to seize the Basilisk Terminus|Basilisk terminus",
                "Havenite Operation Perseus before the the First Havenite-Manticoran War",
                "First Haven-Manticore WarOperation Perseus",
                "First Haven-Manticore War"
        ));
        FandomWikiBattlesQueryTest.CONFLICT_NAMES.put("Second Havenite-Manticoran War", Set.of(
                "Second Havenite-Manticoran War",
                "Second Haven-Manticore War",
                "Second Havenite-Manticoran WarOperation Cutworm II",
                "Second Havenite-Manticoran WarOperation Cutworm"
        ));
    }

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
    void readBattlesToJsonSortByDate() {
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

        final Map<String, List<WikiBattleBlock>> battlesByDate = new HashMap<>();
        battles.forEach(battlesOfNation -> {
            final List<IndividualBattle> individualBattles = battlesOfNation.getIndividualBattles();
            individualBattles.forEach(battle -> {
                final WikiBattleBlock battleBlock = battle.getBattleBlock();
                final String yearPD = battleBlock.getYearPD();
                final List<WikiBattleBlock> orDefault = battlesByDate.getOrDefault(yearPD, new ArrayList<>());
                orDefault.add(battleBlock);
                battlesByDate.put(yearPD, orDefault);
            });
        });
        assertNotNull(battlesByDate);
    }

    @Test
    void readBattlesToJsonSortByConflict() {
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

        final Map<String, List<WikiBattleBlock>> battlesByConflict = new HashMap<>();
        battles.forEach(battlesOfNation -> {
            final List<IndividualBattle> individualBattles = battlesOfNation.getIndividualBattles();
            individualBattles.forEach(battle -> {
                final WikiBattleBlock battleBlock = battle.getBattleBlock();
                String conflict = WikiBattleBlock.sanitize(battleBlock.getConflict());
                if (StringUtils.isEmpty(conflict)) {
                    conflict = "UNKNOWN";
                }

                final List<WikiBattleBlock> orDefault = battlesByConflict.getOrDefault(conflict, new ArrayList<>());
                orDefault.add(battleBlock);
                battlesByConflict.put(conflict, orDefault);
            });
        });
        assertNotNull(battlesByConflict);

        final Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        battlesByConflict.forEach((conflict, battleBlocks) -> {
            final String unifiedConflictName = CONFLICT_NAMES.entrySet().stream().filter(e -> e.getValue().contains(conflict)).findFirst().map(Map.Entry::getKey).orElse("UNKNOWN");
            // hack to suppress printing the battle block content
            battleBlocks.forEach(WikiBattleBlock::tidyUp);
            final String value = gson.toJson(battleBlocks);
            final String dir = FandomWikiQueryTest.DIR + FOLDER + "-by-conflict" + FandomWikiQueryTest.FS;
            TestUtils.writeString(dir, unifiedConflictName + ".json", value);
        });
    }

    @Test
    void readBattlesAndWrite() {
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

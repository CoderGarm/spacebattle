package de.yuga.spacebattle.misc.fandom;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import de.yuga.spacebattle.TestUtils;
import io.github.fastily.jwiki.core.MQuery;
import io.github.fastily.jwiki.core.Wiki;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * <b>Attention:</b> Please be kind, do not create more than necessary traffic.
 */
@Disabled("no test")
public class BattleTechWikiQueryTest {

    public static final String USR_HOME = System.getProperty("user.home");
    public static final String FS = File.separator;
    public static final String DIR = USR_HOME + FS + "Desktop" + FS + "battletech" + FS;

    @Test
    void fetchBattleTechSystems() {

        final Wiki wiki = new Wiki.Builder().withApiEndpoint(HttpUrl.get("https://www.sarna.net/wiki/api.php")).build();

        final List<String> categoryMembers = wiki.getCategoryMembers("Category:Systems");
        final Map<String, String> texts = MQuery.getPageText(wiki, categoryMembers);
        texts.forEach((key, value) -> TestUtils.writeString(DIR + "systems" + FS, key, value));
    }

    @Test
    void evaluateSystems() {
        final Map<String, String> contentByName = getContent(DIR + "systems" + FS);
        final Map<String, Map<Integer, String>> systemsWithAffiliationByYears = new HashMap<>();
        contentByName.forEach((name, content) -> systemsWithAffiliationByYears.put(name, readAffiliations(content)));

        final List<Integer> years = systemsWithAffiliationByYears.values().stream()
                .map(Map::keySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()).stream()
                .sorted()
                .collect(Collectors.toList());

        final Map<Integer, Map<String, List<String>>> yearsWithSystemsByAffiliation = new HashMap<>();
        systemsWithAffiliationByYears.forEach((system, affiliationByYears) -> {
            affiliationByYears.forEach((year, affiliation) -> {
                final Map<String, List<String>> systemsByAffiliation = yearsWithSystemsByAffiliation.getOrDefault(year, new HashMap<>());

                final List<String> systems = systemsByAffiliation.getOrDefault(affiliation, new ArrayList<>());
                systems.add(system);
                systemsByAffiliation.put(affiliation, systems);
                yearsWithSystemsByAffiliation.put(year, systemsByAffiliation);
            });
        });

        final Map<Integer, Set<SystemWikiEntry>> printOut = new HashMap<>();
        final Map<Integer, Map<String, String>> yearsWithAffiliationBySystem = new HashMap<>();
        yearsWithSystemsByAffiliation.forEach((year, systemsByAffiliation) -> {
            systemsByAffiliation.forEach((affiliation, systems) -> {
                final Map<String, String> affiliationBySystem = yearsWithAffiliationBySystem.getOrDefault(year, new HashMap<>());
                systems.forEach(system -> {
                    affiliationBySystem.put(system, affiliation);
                    final Set<SystemWikiEntry> wikiEntries = printOut.getOrDefault(year, new HashSet<>());
                    wikiEntries.add(new SystemWikiEntry(system, affiliation));
                    printOut.put(year, wikiEntries);
                });
                yearsWithAffiliationBySystem.put(year, affiliationBySystem);
            });
        });

        final Gson gson = new Gson();
        printOut.forEach((year, systemWikiEntries) ->
                TestUtils.writeString(DIR + "systemsByAffiliation" + FS, year + ".json", gson.toJson(systemWikiEntries))
        );
    }


    private static class SystemWikiEntry {
        @JsonProperty
        String name;

        @JsonProperty
        String affiliation;

        public SystemWikiEntry(final String name, final String affiliation) {
            this.name = name;
            this.affiliation = affiliation;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            final SystemWikiEntry that = (SystemWikiEntry) o;

            return new EqualsBuilder().append(name, that.name).append(affiliation, that.affiliation).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(name).append(affiliation).toHashCode();
        }
    }

    @Nonnull
    private Map<Integer, String> readAffiliations(@Nonnull final String content) {
        Preconditions.checkNotNull(content, "content must not be empty");

        boolean readActive = false;

        final Map<Integer, String> result = new HashMap<>();
        final List<String> list = Arrays.asList(content.split("\n"));
        for (int i = 0; i < list.size(); i++) {
            final String line = list.get(i);
            if (line.contains("Political Affiliation")) {
                // start reading
                readActive = true;
                continue;
            }

            if (readActive && line.indexOf("\n") == 0) {
                // stop reading
                break;
            }

            if (readActive) {
                Pattern pattern = Pattern.compile("\\*\\s*\\[\\[(\\d{4})\\]\\]");
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    final int year = Integer.parseInt(matcher.group(1));

                    pattern = Pattern.compile("- (.*?)<ref");
                    matcher = pattern.matcher(line);

                    if (matcher.find()) {
                        final String affiliation = matcher.group(1)
                                .replace(" - ", "")
                                .replace("<ref>", "")
                                .replaceAll("\\[", "")
                                .replaceAll("]", "")
                                .replace("100%", "")
                                .trim();
                        result.put(year, affiliation);
                    }
                }
            }
        }
        return result;
    }

    @Nonnull
    @SuppressWarnings("SameParameterValue")
    private Map<String, String> getContent(@Nonnull final String dir) {
        Preconditions.checkNotNull(dir, "dir must not be empty");

        final File[] files = new File(dir).listFiles();
        assertNotNull(files);
        return Arrays.stream(files).collect(Collectors.toMap(File::getName, TestUtils::readFile));
    }

}

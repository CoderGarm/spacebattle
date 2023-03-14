package de.yuga.spacebattle.misc.fandom;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.TestUtils;
import de.yuga.spacebattle.misc.CoordinateElement;
import de.yuga.spacebattle.misc.fandom.spacecraft.dto.WikiShipClass;
import io.github.fastily.jwiki.core.MQuery;
import io.github.fastily.jwiki.core.Wiki;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * <b>Attention:</b> Please be kind, do not create more than necessary traffic.
 */
@Disabled("no test")
public class FandomWikiQueryTest {

    private final static String URL_EN = "https://honorverse.fandom.com/api.php";
    private final static String URL_DE = "https://honor-harrington.fandom.com/de/api.php"; // no api available?

    private static final Wiki WIKI = new Wiki.Builder()
            .withApiEndpoint(HttpUrl.get(URL_DE))
            .build();

    private static final Map<String, String> CATEGORIES = Map.of(
            "Planets", "/planets/",
            "Systems", "/systems/",
            "Asteroid_Belts", "/belts/"
    );

    private static final Map<String, String> CATEGORIES_FOR_TITLES_ONLY = Map.of(
            "Naval_Ships_of_Manticore", "/ships/names/",
            "Naval_Ships_of_Haven", "/ships/names/",
            "Naval_Ships_of_the_Anderman_Empire", "/ships/names/",
            "Naval_Ships_of_Silesia", "/ships/names/",
            "Naval_Ships_of_the_Solarian_League", "/ships/names/"
    );

    public static final String WIKI_SHIPS_DIR = "wiki-ships/";
    public static final String WIKI_SHIPS_EVAL_DIR = "wiki-ships/";

    public static final String DIR = "/home/karsten/Desktop/map/fandom/";

    @Test
    void fetchCategoriesAndTextsFromWiki() {

        CATEGORIES.forEach((category, folder) -> {
            System.out.println("Writing '" + category + "'");
            final List<String> categoryMembers = WIKI.getCategoryMembers(category);
            final Map<String, String> texts = MQuery.getPageText(WIKI, categoryMembers);
            texts.forEach((key, value) -> TestUtils.writeString(DIR + folder, key, value));
        });
    }

    @Test
    void fetchShipClasses() {
        final String category = "Raumschiffsklassen";
        System.out.println("Writing '" + category + "'");
        final List<String> categoryMembers = WIKI.getCategoryMembers(category);
        categoryMembers.forEach(categoryMember -> {
            try {
                final String wikiPageText = WIKI.getPageText(categoryMember);
                if (wikiPageText.contains("{{")) {
                    final WikiShipClass wikiShipClass = new WikiShipClass(wikiPageText);
                    if (wikiShipClass.isValid()) {
                        TestUtils.writeShipClass(DIR + WIKI_SHIPS_DIR, categoryMember + ".csv", wikiShipClass);
                    }
                }
            } catch (final Exception e) {
                fail(e);
            }
        });
    }

    @Test
    void fetchShipNames() {
        CATEGORIES_FOR_TITLES_ONLY.forEach((category, folder) -> {
            System.out.println("Writing '" + category + "'");
            final List<String> categoryMembers = WIKI.getCategoryMembers(category);
            final String join = String.join("\n", categoryMembers);
            TestUtils.writeString(DIR + folder, category, join);
        });
    }

    @Test
    void fetchCategoryNames() {
        final String category = "Planets";
        final String folder = "";

        System.out.println("Writing '" + category + "'");
        final List<String> categoryMembers = WIKI.getCategoryMembers(category);
        final String join = String.join("\n", categoryMembers);
        TestUtils.writeString(DIR + folder, category, join);
    }

    @Test
    void readFiles() {

        final List<CoordinateElement> coordinateElements = readStarSystems();
        final Set<String> knownSystemNames = coordinateElements.stream().map(CoordinateElement::getName).collect(Collectors.toSet());

        CATEGORIES.forEach((category, folder) -> {
            System.out.println("Reading '" + category + "'");
            final Map<String, String> texts = getContent(DIR, CATEGORIES.get("Planets"));
            texts.forEach((name, text) -> {
                for (final String knownSystemName : knownSystemNames) {
                    final String[] split = text.split(" ");
                    for (final String word : split) {
                        final String sanitized = word.replaceAll("[^A-Za-z]+", "");
                        if (sanitized.equals(knownSystemName)) {
                            System.out.println("Found system name: " + knownSystemName + "\n");
                            System.out.println("In " + category + " text:\n" + text);
                        }
                    }
                }
            });
        });
    }

    @SuppressWarnings("SameParameterValue")
    private Map<String, String> getContent(final String dir, final String filename) {
        final File[] files = new File(dir + filename).listFiles();
        assertNotNull(files);
        return Arrays.stream(files).collect(Collectors.toMap(File::getName, TestUtils::readFile));
    }

    private List<CoordinateElement> readStarSystems() {

        final List<CoordinateElement> coordinateWthNames = new ArrayList<>();

        InputStream mapDataStream = null;
        String line = null;
        try {
            mapDataStream = this.getClass().getResourceAsStream("/map-data.csv");
            Preconditions.checkNotNull(mapDataStream, "mapDataStream must not be empty");
            final BufferedReader br = new BufferedReader(new InputStreamReader(mapDataStream));
            while ((line = br.readLine()) != null) {
                final CoordinateElement coordinateWthName = new CoordinateElement(line.split(","));
                coordinateWthNames.add(coordinateWthName);
            }

        } catch (Exception e) {
            System.out.println(line);
            e.printStackTrace();
        } finally {
            if (mapDataStream != null) {
                try {
                    mapDataStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return coordinateWthNames;
    }
}

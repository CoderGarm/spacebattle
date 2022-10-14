package de.yuga.spacebattle.misc;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.TestUtils;
import io.github.fastily.jwiki.core.MQuery;
import io.github.fastily.jwiki.core.Wiki;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("no test")
public class FandomQueryTest {

    public static final String PLANETS = "/planets/";
    public static final String SYSTEMS = "/systems/";
    private final String DIR = "/home/karsten/Desktop/map/fandom/";

    @Test
    void fetchFromWiki() {

        final HttpUrl httpUrl = null;
        /* todo do only fetch when needed
         * final HttpUrl httpUrl = HttpUrl.get("https://honorverse.fandom.com/api.php");
         */
        final Wiki wiki = new Wiki.Builder()
                .withApiEndpoint(httpUrl)
                .build();

        final List<String> planets = wiki.getCategoryMembers("Planets");
        final List<String> systems = wiki.getCategoryMembers("Systems");


        final Map<String, String> planetsText = MQuery.getPageText(wiki, planets);
        final Map<String, String> systemsText = MQuery.getPageText(wiki, systems);

        planetsText.forEach((key, value) -> TestUtils.writeString(DIR + PLANETS, key, value));
        systemsText.forEach((key, value) -> TestUtils.writeString(DIR + SYSTEMS, key, value));

        assertNotNull(planets);
        assertNotNull(systems);
    }

    @Test
    void readFiles() {

        final Map<String, String> planets = getContent(DIR, PLANETS);
        final Map<String, String> systems = getContent(DIR, SYSTEMS);

        final List<CoordinateElement> coordinateElements = readStarSystems();
        final Set<String> knownSystemNames = coordinateElements.stream().map(CoordinateElement::getName).collect(Collectors.toSet());

        // todo match names to find knowledge

    }

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

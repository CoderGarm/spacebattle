package de.yuga.spacebattle.misc.fandom;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import de.yuga.spacebattle.TestUtils;
import de.yuga.spacebattle.misc.CoordinateElement;
import de.yuga.spacebattle.rest.dto.misc.DistanceElement;
import de.yuga.spacebattle.rest.dto.misc.Position;
import io.github.fastily.jwiki.core.MQuery;
import io.github.fastily.jwiki.core.Wiki;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * <b>Attention:</b> Please be kind, do not create more than necessary traffic.
 */
@Disabled("no test")
public class FandomWikiQueryTest {

    public static final String USR_HOME = System.getProperty("user.home");
    public static final String FS = File.separator;
    public static final String DIR = USR_HOME + FS + "Desktop" + FS + "fandom" + FS;

    @Test
    void fetchAllFromWiki() {

        EWikiConfig.get().forEach(config -> {
            final Wiki wiki = config.getWiki();
            final List<EWikiCategories> categories = EWikiCategories.get();
            for (final EWikiCategories eWikiCategory : categories) {
                final String category = eWikiCategory.getCategory(config);
                if (category == null) {
                    System.out.println("Category empty: " + eWikiCategory + " for language: " + config.getLanguage());
                    continue;
                }
                final String folder = eWikiCategory.getFolder(config);
                final List<String> categoryMembers = wiki.getCategoryMembers(category);
                final Map<String, String> texts = MQuery.getPageText(wiki, categoryMembers);
                texts.forEach((key, value) -> TestUtils.writeString(DIR + folder, key, value));
            }
        });
    }

    @Test
    void sortStarSystems() {
        final Gson gson = new Gson();
        Set.of(EWikiCategories.SYSTEMS_MESA, EWikiCategories.SYSTEMS_HAVEN, EWikiCategories.SYSTEMS_SOLARIAN, EWikiCategories.SYSTEMS_GRAYSON, EWikiCategories.SYSTEMS_MANTICORE, EWikiCategories.SYSTEMS_ANDERMAN)
                .forEach(wikiCategory -> {
                    EWikiConfig config = EWikiConfig.EN;
                    String category = wikiCategory.getCategory(config);
                    if (category == null) {
                        config = EWikiConfig.DE;
                        category = wikiCategory.getCategory(config);
                    }
                    final String folder = wikiCategory.getFolder(config);
                    final Set<String> fileNames = getContent(DIR + folder, "").keySet();
                    final Set<WikiEntry> collect = fileNames.stream().map(WikiEntry::new).collect(Collectors.toSet());
                    TestUtils.writeString(DIR + "systems/summary/", category + ".json ", gson.toJson(collect));
                });
    }

    private static class WikiEntry {
        @JsonProperty
        String title;

        public WikiEntry(final String title) {
            this.title = title;
        }
    }

    @Test
    void checkDistances() {
        final List<DistanceElement> distanceElements = readDistances();
        assertNotNull(distanceElements);

        final List<CoordinateElement> coordinateElements = readStarSystems();
        distanceElements.forEach(d -> {
            coordinateElements.stream().filter(c -> c.getName().equals(d.getName())).findFirst().ifPresent(c -> d.setPosition(c.getPosition()));
        });

        final List<DistanceElement> without = distanceElements.stream().filter(d -> d.getPosition() == null).sorted().collect(Collectors.toList());
        final List<DistanceElement> with = distanceElements.stream().filter(d -> d.getPosition() != null).sorted().collect(Collectors.toList());
        System.out.println("With: " + with.size() + ", without: " + without.size());

        final Set<String> known = new HashSet<>();
        with.forEach(distanceElement -> {
            final String distanceElementName = distanceElement.getName();
            final Position position = distanceElement.getPosition();
            final Map<DistanceElement, Integer> connectionsWithCoordinates = distanceElement.getConnectionsWithCoordinates();
            connectionsWithCoordinates.forEach((connectedElement, canonicalDistance) -> {
                final Position connectedPosition = connectedElement.getPosition();
                assert position != null;
                assert connectedPosition != null;
                final int distance = getDistance(position, connectedPosition);
                final String connectedElementName = connectedElement.getName();
                final String o1 = distanceElementName + connectedElementName;
                final String o2 = connectedElementName + distanceElementName;
                if (!known.contains(o1) && !known.contains(o2)) {
                    final double scale = ((double) distance) / ((double) canonicalDistance);
                    final double round = Math.round(scale * 100.0) / 100.0;
                    System.out.println(distanceElementName + " to " + connectedElementName + ", distance of " + distance + " by canonical distance of " + canonicalDistance + " with scale " + round);
                    known.add(o1);
                    known.add(o2);
                }
            });
        });
    }

    private int getDistance(@Nonnull final Position orbit1, @Nonnull final Position orbit2) {
        Preconditions.checkNotNull(orbit1, "orbit1 shouldn't be null!");
        Preconditions.checkNotNull(orbit2, "orbit2 shouldn't be null!");

        final int x1 = orbit1.getX();
        final int y1 = orbit1.getY();

        final int x2 = orbit2.getX();
        final int y2 = orbit2.getY();

        return getDistance(x2 - x1, y2 - y1);
    }

    public int getDistance(final int firstCoord, final int secondCoord) {
        final double x = Math.pow(firstCoord, 2);
        final double y = Math.pow(secondCoord, 2);
        return ((Double) (Math.sqrt(x + y))).intValue();
    }

    @Test
    void readCoordinates() {
        final List<CoordinateElement> coordinateElements = readStarSystems();
        final Set<String> knownSystemNames = coordinateElements.stream().map(CoordinateElement::getName).collect(Collectors.toSet());
    }

    @Nonnull
    @SuppressWarnings("SameParameterValue")
    private Map<String, String> getContent(@Nonnull final String dir, @Nonnull final String filename) {
        final File[] files = new File(dir + filename).listFiles();
        assertNotNull(files);
        return Arrays.stream(files).collect(Collectors.toMap(File::getName, TestUtils::readFile));
    }

    private List<CoordinateElement> readStarSystems() {
        final List<CoordinateElement> coordinateWthNames = new ArrayList<>();
        InputStream stream = null;
        String line = null;
        try {
            stream = this.getClass().getResourceAsStream("/systems.csv");
            Preconditions.checkNotNull(stream, "stream must not be empty");
            final BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            while ((line = br.readLine()) != null) {
                final CoordinateElement coordinateWthName = new CoordinateElement(line.split(","));
                coordinateWthNames.add(coordinateWthName);
            }

        } catch (Exception e) {
            System.out.println(line);
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return coordinateWthNames;
    }

    private List<DistanceElement> readDistances() {
        final Set<DistanceElement> distanceElements = new HashSet<>();
        InputStream stream = null;
        String line = null;
        try {
            stream = this.getClass().getResourceAsStream("/distance.txt");
            Preconditions.checkNotNull(stream, "stream must not be empty");
            final BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            while ((line = br.readLine()) != null) {
                final String[] split = line.split("\\,");
                final String name = split[0];
                final DistanceElement distanceElement = requireNonNullElse(distanceElements, name);
                for (int i = 1; i < split.length; i++) {
                    final String elem = split[i];
                    final String[] strings = elem.replaceAll("\\s", "").split("LY");
                    final int distance = Integer.parseInt(strings[0]);
                    final String connectedToName = strings[1];
                    final DistanceElement connectedElement = requireNonNullElse(distanceElements, connectedToName);
                    distanceElements.add(connectedElement);
                    distanceElement.add(connectedElement, distance);
                    connectedElement.add(distanceElement, distance);
                }
                distanceElements.add(distanceElement);
            }

        } catch (Exception e) {
            System.out.println(line);
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return distanceElements.stream().sorted().collect(Collectors.toList());
    }

    @Nonnull
    private static DistanceElement requireNonNullElse(final Collection<DistanceElement> distanceElements, final String name) {
        return Objects.requireNonNullElse(
                distanceElements.stream().filter(d -> d.getName().equals(name)).findFirst().orElse(null),
                new DistanceElement(name));
    }
}

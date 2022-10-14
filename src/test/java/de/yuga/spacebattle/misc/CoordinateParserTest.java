package de.yuga.spacebattle.misc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mifmif.common.regex.Generex;
import de.yuga.spacebattle.backend.calculator.distance.DistanceCalculator;
import de.yuga.spacebattle.backend.enums.EStarSystemCatalogue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import javax.annotation.Nonnull;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.yuga.spacebattle.TestUtils.readFromInputStream;
import static de.yuga.spacebattle.TestUtils.writeString;
import static org.junit.jupiter.api.Assertions.*;

@Disabled
public class CoordinateParserTest {

    private final String DIR = "/home/karsten/Desktop/map/coords/";

    private static final Set<String> REFERENCE_NAMES = Set.of("Sol",
            "Manticore",
            "Haven",
            "Gregor",
            "Basilisk",
            "Marsh",
            "Erewhon",
            "Spindle",
            "Asgerd",
            "Midgard",
            "Matapan");

    private static final Set<String> UNMATCHED_SYSTEMS = Set.of("Clearway", "Hancock", "Talisman", "Yeltsin", "Zanzibar");

    /**
     * These systems must be multiple times present - by lore.
     */
    private static final Set<String> DOUBLE_NAMED_SYSTEMS = Set.of("Casca", "Slocum");
    public static final String EMPTY_NAME = "";

    @Test
    void writeToFile() {
        final List<CoordinateElement> fromCsv = getCsvData();

        final List<CoordinateElement> fromJson = getJsonData();

        write("csv.data", fromCsv);
        write("json.data", fromJson);
    }

    @Test
    void fetchDoubleNamedSystems() {
        final List<CoordinateElement> fromCsv = readCsvBased();
        assertFalse(fromCsv.isEmpty());
        final Map<String, List<CoordinateElement>> doublesInCsv = fetchDoubles(fromCsv);
        DOUBLE_NAMED_SYSTEMS.forEach(doublesInCsv::remove);
        writeString(DIR, "doublesInCsv.data", String.join("\n", doublesInCsv.keySet()));

        final List<CoordinateElement> fromJson = readJsonBased();
        assertFalse(fromJson.isEmpty());
        final Map<String, List<CoordinateElement>> doublesInJson = fetchDoubles(fromJson);
        DOUBLE_NAMED_SYSTEMS.forEach(doublesInJson::remove);
        writeString(DIR, "doublesInJson.data", String.join("\n", doublesInJson.keySet()));
    }

    @Test
    void analyse() {
        final List<CoordinateElement> fromCsv = getCsvData();
        final List<CoordinateElement> fromJson = getJsonData();

        final Map<String, List<CoordinateElement>> csvMap = fromCsv.stream()
                .collect(Collectors.groupingBy(CoordinateElement::getName,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final Map<String, List<CoordinateElement>> jsonMap = fromJson.stream()
                .collect(Collectors.groupingBy(CoordinateElement::getName,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

        final Set<String> csvNames = new HashSet<>(csvMap.keySet());
        final Set<String> jsonNames = new HashSet<>(jsonMap.keySet());

        final Set<String> notInJson = csvNames.stream().filter(name -> !jsonNames.contains(name)).collect(Collectors.toSet());
        writeString(DIR, "notInJson.data", String.join("\n", notInJson));
        final Set<String> notInCsv = jsonNames.stream().filter(name -> !csvNames.contains(name)).collect(Collectors.toSet());
        writeString(DIR, "notInCsv.data", String.join("\n", notInCsv));

        final Set<String> inJson = csvNames.stream().filter(jsonNames::contains).collect(Collectors.toSet());
        final Set<String> inCsv = jsonNames.stream().filter(csvNames::contains).collect(Collectors.toSet());

        final Set<String> inBoth = new HashSet<>(inJson);
        inBoth.addAll(inCsv);
        // remove all systems which can be multiple times present and unnamed systems
        inBoth.removeAll(DOUBLE_NAMED_SYSTEMS);
        inBoth.remove("");

        writeString(DIR, "inBoth.data", String.join("\n", inBoth));

        final Set<Integer> xDiff = new HashSet<>();
        final Set<Integer> yDiff = new HashSet<>();

        final List<CoordinateDifference> diffs = new ArrayList<>();
        inBoth.forEach(name -> {

            final List<CoordinateElement> csvElements = csvMap.get(name);
            final List<CoordinateElement> jsonElements = jsonMap.get(name);

            final List<CoordinateElement> csvAveraged = middleDoubles(csvElements);
            assertEquals(1, csvAveraged.size());
            final List<CoordinateElement> jsonAveraged = middleDoubles(jsonElements);
            assertEquals(1, jsonAveraged.size());

            final CoordinateElement csv = csvAveraged.get(0);
            final CoordinateElement json = jsonAveraged.get(0);
            final CoordinateDifference diff = new CoordinateDifference(csv, json);
            diffs.add(diff);

            final Integer xCsv = csv.getXCoord();
            final Integer xJson = json.getXCoord();
            xDiff.add(xJson - xCsv);

            final Integer yCsv = csv.getYCoord();
            final Integer yJson = json.getYCoord();
            yDiff.add(yJson - yCsv);
        });
        write("same-name-different-positions.data", diffs.stream()
                .sorted(Comparator.comparing(CoordinateDifference::getDistance))
                .collect(Collectors.toList()));

        // systems with not matching names
        final List<CoordinateElement> csvData = new ArrayList<>(fromCsv).stream()
                .sorted(Comparator.comparing(CoordinateElement::getName))
                .collect(Collectors.toList());
        csvData.removeIf(c -> inBoth.contains(c.getName()));

        final Map<String, List<CoordinateElement>> csvMultiples = csvMap.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        csvMultiples.forEach((name, cs) -> assertTrue(DOUBLE_NAMED_SYSTEMS.contains(name)));
        DOUBLE_NAMED_SYSTEMS.forEach(name -> assertEquals(2, (int) csvData.stream().filter(c -> c.getName().equals(name)).count()));

        final List<CoordinateElement> jsonData = new ArrayList<>(fromJson).stream()
                .filter(c -> StringUtils.isNotBlank(c.getName()))
                .sorted(Comparator.comparing(CoordinateElement::getName))
                .collect(Collectors.toList());
        jsonData.removeIf(c -> inBoth.contains(c.getName()));

        final Map<String, List<CoordinateElement>> jsonMultiples = jsonMap.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        assertEquals(1, jsonMultiples.size());

        // systems with averaged coordinates from both sources
        final List<CoordinateElement> middlesFromBoth = middleDoublesFromDiffs(diffs).stream()
                .sorted(Comparator.comparing(CoordinateElement::getName))
                .collect(Collectors.toList());

        List<CoordinateElement> withoutNamesJson = jsonMultiples.get(EMPTY_NAME);

        final List<CoordinateElement> result = new ArrayList<>();
        result.addAll(csvData);
        result.addAll(jsonData);
        result.addAll(middlesFromBoth);
        for (final CoordinateElement c : withoutNamesJson) {
            setCatalogueName(withoutNamesJson, c);
        }
        withoutNamesJson = withoutNamesJson.stream()
                .sorted(Comparator.comparing(CoordinateElement::getName))
                .collect(Collectors.toList());
        result.addAll(withoutNamesJson);

        // detect systems which could be the same or are to close to each other
        final int xAverage = getAverage(xDiff);
        final int yAverage = getAverage(yDiff);
        final int averageDeviance = getDistance(xAverage, yAverage) / 10;
        final List<CoordinateOverlapping> closeTogether = new ArrayList<>();
        for (final CoordinateElement outer : result) {
            for (final CoordinateElement inner : result) {
                final int distance = outer.getPosition().getDistance(inner.getPosition());
                if (distance <= averageDeviance && !outer.getName().equals(inner.getName())) {
                    final boolean present = isPresent(closeTogether, outer, inner);
                    if (!present) {
                        final CoordinateOverlapping coordinateDifference = new CoordinateOverlapping(outer, inner);
                        closeTogether.add(coordinateDifference);
                    }
                }
            }
        }

        // fill up missing Ids
        final List<CoordinateElement> referenceSystems = result.stream()
                .filter(c -> REFERENCE_NAMES.contains(c.getName()))
                .collect(Collectors.toList());

        if (referenceSystems.stream().anyMatch(CoordinateElement::isNope)) {
            fail("Reference systems must have a valid id");
        }

        final List<CoordinateElement> withoutIds = result.stream()
                .filter(CoordinateElement::isNope)
                .collect(Collectors.toList());
        assertFalse(withoutIds.isEmpty());

        for (final CoordinateElement c : withoutIds) {
            assertTrue(c.isNope());
            final CoordinateElement reference = getClosestReferenceSystem(referenceSystems, c);
            final Position ref = reference.getPosition();
            final Position pos = c.getPosition();

            final int dist = ref.getDistance(pos);
            final double bearingTo = ref.bearingTo(pos);

            CoordinateElement candidate = null;
            for (double i = 0.1; i < 1; i += 0.1) {

                final double deviance = i;
                final List<CoordinateElement> candidates = withoutNamesJson.stream()
                        .filter(json -> json.isCandidate(reference, dist, bearingTo, deviance))
                        .sorted(Comparator.comparingInt(o -> o.getDistance(reference)))
                        .collect(Collectors.toList());

                if (!candidates.isEmpty()) {
                    candidate = candidates.get(0);
                    break;
                }
            }

            if (candidate != null) {
                c.setId(candidate.getId());
                c.setOwner(candidate.getOwner());
            }
        }

        final List<Integer> allIDs = result.stream()
                .filter(c -> !c.isNope()).map(CoordinateElement::getId).map(Integer::parseInt)
                .sorted(Integer::compareTo)
                .collect(Collectors.toList());
        result.stream()
                .filter(CoordinateElement::isNope)
                .forEach(c -> {
                    if (UNMATCHED_SYSTEMS.contains(c.getName())) {
                        final Integer lastId = allIDs.get(allIDs.size() - 1);
                        c.setId("" + lastId + 1);
                    } else {
                        fail("Why is '" + c.getName() + "' in this loop?");
                    }
                });

        write("result.data", result);

        writeAsJson("mapdata-modified.js", result);

        assertTrue(true);
    }

    private CoordinateElement getClosestReferenceSystem(final List<CoordinateElement> referenceSystems, final CoordinateElement c) {
        int checker = Integer.MAX_VALUE;
        CoordinateElement referenceSystem = null;
        for (final CoordinateElement ref : referenceSystems) {
            final int refDistance = ref.getDistance(c);
            if (refDistance < checker) {
                referenceSystem = ref;
                checker = refDistance;
            }
        }
        return referenceSystem;
    }

    private static boolean isPresent(final List<CoordinateOverlapping> closeTogether, final CoordinateElement first, final CoordinateElement second) {
        return closeTogether.stream().anyMatch(o -> (o.getFirst().equals(first) || o.getSecond().equals(first)) && (o.getFirst().equals(second) || o.getSecond().equals(second)));
    }

    private static void setCatalogueName(final List<CoordinateElement> withoutNamesJson, final CoordinateElement c) {
        final Set<String> names = withoutNamesJson.stream()
                .map(CoordinateElement::getName)
                .collect(Collectors.toSet());
        String randomCatalogueName = EStarSystemCatalogue.getRandomCatalogueName();
        while (names.contains(randomCatalogueName)) {
            randomCatalogueName = EStarSystemCatalogue.getRandomCatalogueName();
        }
        c.setName(randomCatalogueName);
    }

    @Test
    void testGeneration() {
        for (int i = 0; i < 100; i++) {
            final EStarSystemCatalogue random = EStarSystemCatalogue.getRandom();
            final String result = generateCatalogueName(random.getPseudoRegex());
            System.out.println(result);
            assertNotNull(result);
        }
    }

    private String generateCatalogueName(final String regex) {
        final Generex generex = new Generex(regex);

        // Generate random String
        return generex.random();
    }

    public int getDistance(final int x, final int y) {
        final BigDecimal xThat = new BigDecimal(x);
        final BigDecimal yThat = new BigDecimal(y);
        return DistanceCalculator.getDistance(xThat, yThat).intValue();
    }

    private List<CoordinateElement> middleDoublesFromDiffs(final List<CoordinateDifference> coordinateDifferences) {

        final List<CoordinateElement> result = new ArrayList<>();
        coordinateDifferences.forEach(diff -> {
            final List<Position> positions = new ArrayList<>();
            final CoordinateElement csv = diff.getFromCsv();
            positions.add(csv.getPosition());
            final CoordinateElement json = diff.getFromJson();
            positions.add(json.getPosition());

            final Set<Integer> xCoords = positions.stream().map(Position::getX).map(Integer::valueOf).collect(Collectors.toSet());
            final int xAverage = getAverage(xCoords);

            final Set<Integer> yCoords = positions.stream().map(Position::getY).map(Integer::valueOf).collect(Collectors.toSet());
            final int yAverage = getAverage(yCoords);

            final Position position = new Position(xAverage, yAverage);
            result.add(new CoordinateElement(json, position));
        });

        return result;
    }

    private void write(final String fileName, final List<CoordinateDifference> coordinateDifferences) {

        final StringBuilder sb = new StringBuilder();
        for (final CoordinateDifference e : coordinateDifferences) {
            sb.append(e.toString());
            sb.append("\n");
        }
        writeString(DIR, fileName, sb.toString());
    }

    private void write(final String fileName, final Collection<CoordinateElement> coordinateElements) {
        final StringBuilder sb = new StringBuilder();
        for (final CoordinateElement e : coordinateElements) {
            sb.append(e.toString());
            sb.append("\n");
        }
        writeString(DIR, fileName, sb.toString());
    }

    @Nonnull
    private List<CoordinateElement> getJsonData() {
        final List<CoordinateElement> fromJson = readJsonBased();
        assertFalse(fromJson.isEmpty());
        return fromJson;
    }

    @Nonnull
    private List<CoordinateElement> getCsvData() {
        final List<CoordinateElement> fromCsv = readCsvBased();
        assertFalse(fromCsv.isEmpty());

        fromCsv.forEach(CoordinateElement::invertYAxis);
        return middleDoubles(fromCsv);
    }


    private void writeAsJson(final String fileName, final List<CoordinateElement> coordinateElements) {
        final StringBuilder sb = new StringBuilder();
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();

        final String result = gson.toJson(coordinateElements.toArray(), CoordinateElement[].class);
        sb.append("var mapData =\n");
        sb.append(result);
        sb.append("\n");
        writeString(DIR, fileName, sb.toString());
    }

    private Map<String, List<CoordinateElement>> fetchDoubles(final List<CoordinateElement> coordinateElements) {

        final Map<String, List<CoordinateElement>> result = new HashMap<>();
        final Set<String> names = coordinateElements.stream()
                .map(CoordinateElement::getName)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());

        for (final String name : names) {
            final List<CoordinateElement> doubles = coordinateElements.stream()
                    .filter(c -> c.getName().equals(name))
                    .collect(Collectors.toList());
            if (doubles.size() > 1) {
                result.put(name, doubles);
            }
        }

        return result;
    }

    private List<CoordinateElement> middleDoubles(final List<CoordinateElement> coordinateElements) {
        final List<CoordinateElement> result = new ArrayList<>(coordinateElements);
        final Map<String, List<CoordinateElement>> doublesByName = fetchDoubles(coordinateElements);
        DOUBLE_NAMED_SYSTEMS.forEach(doublesByName::remove);

        final List<CoordinateElement> toRemove = new ArrayList<>();
        final List<CoordinateElement> toAdd = new ArrayList<>();
        doublesByName.forEach((name, doubles) -> {
            final List<Position> positions = doubles.stream()
                    .map(CoordinateElement::getPosition)
                    .collect(Collectors.toList());

            final Set<Integer> xCoords = positions.stream().map(Position::getX).map(Integer::valueOf).collect(Collectors.toSet());
            final int xAverage = getAverage(xCoords);

            final Set<Integer> yCoords = positions.stream().map(Position::getY).map(Integer::valueOf).collect(Collectors.toSet());
            final int yAverage = getAverage(yCoords);

            final Position position = new Position(xAverage, yAverage);
            toAdd.add(new CoordinateElement(doubles.get(0), position));
            toRemove.addAll(doubles);
        });

        result.removeAll(toRemove);
        result.addAll(toAdd);
        return result;
    }

    private int getAverage(final Set<Integer> coords) {
        int coord = 0;
        for (final Integer yCoord : coords) {
            coord += yCoord;
        }
        return coord / coords.size();
    }


    private List<CoordinateElement> readCsvBased() {

        final List<CoordinateElement> coordinateWthNames = new ArrayList<>();

        InputStream inputStream = null;
        try {
            final File file = new File("/home/karsten/Desktop/map/coords/base-data/coords.csv");
            inputStream = new FileInputStream(file);
            final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                final CoordinateElement coordinateWthName = new CoordinateElement(line.split(","));
                coordinateWthNames.add(coordinateWthName);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return coordinateWthNames;
    }

    private List<CoordinateElement> readJsonBased() {
        final File file = new File("/home/karsten/Desktop/map/coords/base-data/star-coords.json");
        try (final InputStream inputStream = Files.newInputStream(file.toPath())) {
            final String result = readFromInputStream(inputStream);

            final Gson gson = new GsonBuilder().create();
            final CoordinateElement[] userArray = gson.fromJson(result, CoordinateElement[].class);

            return Arrays.stream(userArray).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}

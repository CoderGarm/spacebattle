package de.yuga.spacebattle.backend.services;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.account.RolePlaySetting;
import de.yuga.spacebattle.backend.entities.account.User;
import de.yuga.spacebattle.backend.enums.EStarNation;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import de.yuga.spacebattle.rest.dto.misc.Coords;
import de.yuga.spacebattle.rest.dto.misc.DistanceElement;
import de.yuga.spacebattle.rest.dto.misc.Position;
import de.yuga.spacebattle.rest.dto.misc.wormhole.Junction;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class ResourceService {


    @Nonnull
    public Set<String> getRandomShipNamesForOwner(@Nonnull final User owner, @Nonnull final Integer amount) {
        final Set<String> randomNames;
        final RolePlaySetting rolePlaySetting = owner.getRolePlaySetting();
        final Set<String> shipNames = rolePlaySetting.getShipNames();
        if (shipNames.isEmpty()) {
            final Set<EStarNation> shipNameTemplates = rolePlaySetting.getShipNameTemplates();
            if (shipNameTemplates.isEmpty()) {
                randomNames = getRandomWarshipName(amount);
            } else {
                randomNames = getRandomWarshipNames(shipNameTemplates, amount);
            }
        } else {
            randomNames = shipNames;
        }
        return randomNames;
    }

    @Nonnull
    public List<Coords> readStarSystems() {
        final List<String> lst = new ArrayList<>();
        getFileLineByLine("/", "systems.csv", lst);
        return lst.stream().map(line -> new Coords(line.split(","))).collect(Collectors.toList());
    }

    @Nonnull
    public List<DistanceElement> getAllDistances() {
        return collectDistanceElements();
    }

    private List<DistanceElement> collectDistanceElements() {
        final List<DistanceElement> distanceElements = readDistances();

        final List<Coords> coordinateElements = readStarSystems();
        distanceElements.forEach(d -> {
            coordinateElements.stream().filter(c -> c.getName().equals(d.getName())).findFirst().ifPresent(c -> d.setPosition(c.getPosition()));
        });

        final List<DistanceElement> without = distanceElements.stream().filter(d -> d.getPosition() == null).sorted().collect(Collectors.toList());
        final List<DistanceElement> with = distanceElements.stream().filter(d -> d.getPosition() != null).sorted().collect(Collectors.toList());

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
                    known.add(o1);
                    known.add(o2);
                }
            });
        });
        return distanceElements;
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

    private List<DistanceElement> readDistances() {
        final Set<DistanceElement> distanceElements = new HashSet<>();
        String line = null;
        try (InputStream stream = this.getClass().getResourceAsStream("/distance.txt")) {
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

        } catch (Exception ignore) {
        }
        return distanceElements.stream().sorted().collect(Collectors.toList());
    }


    @Nonnull
    private static DistanceElement requireNonNullElse(final Collection<DistanceElement> distanceElements, final String name) {
        return Objects.requireNonNullElse(
                distanceElements.stream().filter(d -> d.getName().equals(name)).findFirst().orElse(null),
                new DistanceElement(name));
    }

    @Nonnull
    public List<String> readAllShipNames() {

        final String dir = "ship-names";
        final Set<String> shipNameFiles = Set.of("Naval_Ships_of_Manticore",
                "Naval_Ships_of_Haven",
                "Naval_Ships_of_the_Anderman_Empire",
                "Naval_Ships_of_Silesia",
                "Naval_Ships_of_the_Solarian_League");

        final List<String> shipNames = new ArrayList<>();

        for (final String shipNameFile : shipNameFiles) {
            getFileLineByLine(dir, shipNameFile, shipNames);
        }
        return shipNames;
    }


    @Nonnull
    public List<String> readShipNamesFromList(@Nonnull final EStarNation starNation) {
        Preconditions.checkNotNull(starNation, "starNation must not be empty");

        final String dir = "ship-names";
        final String shipNameFile;

        switch (starNation) {
            case MANTICORE:
                shipNameFile = "Naval_Ships_of_Manticore";
                break;
            case HAVEN:
                shipNameFile = "Naval_Ships_of_Haven";
                break;
            case ANDERMAN:
                shipNameFile = "Naval_Ships_of_the_Anderman_Empire";
                break;
            case SILESIA:
                shipNameFile = "Naval_Ships_of_Silesia";
                break;
            case SOLARIAN_LEAGUE:
                shipNameFile = "Naval_Ships_of_the_Solarian_League";
                break;
            default:
                throw new NotifyWebUserException("Nope.");
        }

        final List<String> shipNames = new ArrayList<>();


        getFileLineByLine(dir, shipNameFile, shipNames);

        return shipNames;
    }

    private void getFileLineByLine(@Nonnull String dir, @Nonnull final String fileName, final Collection<String> content) {
        Preconditions.checkNotNull(dir, "dir must not be empty");
        Preconditions.checkNotNull(fileName, "fileName must not be empty");

        InputStream inputStream = null;
        String line = null;
        try {
            if (!dir.startsWith("/")) {
                dir = "/" + dir;
            }
            if (!dir.endsWith("/")) {
                dir = dir + "/";
            }
            inputStream = this.getClass().getResourceAsStream(dir + fileName);
            Preconditions.checkNotNull(inputStream, "inputStream must not be empty");
            final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = br.readLine()) != null) {
                content.add(line);
            }

        } catch (Exception e) {
            System.out.println(line);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    @Nonnull
    public String getRandomWarshipName() {
        final List<String> strings = readAllShipNames();

        final int i = ThreadLocalRandom.current().nextInt(0, strings.size() - 1);
        return strings.get(i);
    }

    @Nonnull
    public Set<String> getRandomWarshipNames(@Nonnull final Set<EStarNation> starNations, final int amount) {
        Preconditions.checkNotNull(starNations, "starNations must not be empty");

        final List<String> strings = new ArrayList<>();
        starNations.forEach(nation -> strings.addAll(readShipNamesFromList(nation)));

        final Set<String> names = new HashSet<>();
        for (int i = 0; i < amount; i++) {
            names.add(strings.get(ThreadLocalRandom.current().nextInt(0, strings.size() - 1)));
        }
        return names;
    }

    @Nonnull
    public Set<String> getRandomWarshipName(final int amount) {
        final List<String> strings = readAllShipNames();
        final Set<String> names = new HashSet<>();
        for (int i = 0; i < amount; i++) {
            names.add(strings.get(ThreadLocalRandom.current().nextInt(0, strings.size() - 1)));
        }
        return names;
    }

    public List<String> readPlanetNames() {
        final String dir = "orbitals";
        final List<String> shipNames = new ArrayList<>();
        getFileLineByLine(dir, "Planets", shipNames);
        return shipNames;
    }

    @Nonnull
    public String getRandomPlanetName() {
        final List<String> strings = readPlanetNames();

        final int i = ThreadLocalRandom.current().nextInt(0, strings.size() - 1);
        return strings.get(i);
    }

    @Nonnull
    public List<String> getRandomPlanetName(final int amount) {
        final List<String> strings = readPlanetNames();
        final List<String> names = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            names.add(strings.get(ThreadLocalRandom.current().nextInt(0, strings.size() - 1)));
        }
        return names;
    }

    @Nonnull
    public Set<Junction> readWormholes() {
        final List<String> lst = new ArrayList<>();
        getFileLineByLine("/", "junctions.csv", lst);

        final Map<Coords, Set<Coords>> junctions = new HashMap<>();
        lst.forEach(line -> {
            final String[] split = line.split("\\|");
            final Coords junction = new Coords(split[0]);
            final Set<Coords> termini = junctions.getOrDefault(junction, new HashSet<>());
            for (int i = 1; i < split.length; i++) {
                termini.add(new Coords(split[i]));
            }
            junctions.put(junction, termini);
        });

        return junctions.entrySet().stream().map(e -> new Junction(e.getKey(), e.getValue())).collect(Collectors.toSet());
    }
}

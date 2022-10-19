package de.yuga.spacebattle.backend.services;

import com.google.common.base.Preconditions;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    public List<MasterOfTheUniverseService.Coords> readStarSystems() {
        final List<String> lst = new ArrayList<>();
        getFileLineByLine("/", "map-data.csv", lst);
        return lst.stream().map(line -> new MasterOfTheUniverseService.Coords(line.split(","))).collect(Collectors.toList());
    }

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

    private void getFileLineByLine(String dir, final String fileName, final Collection<String> content) {
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
    }

    @Nonnull
    public String getRandomWarshipName() {
        final List<String> strings = readAllShipNames();

        final int i = ThreadLocalRandom.current().nextInt(0, strings.size() - 1);
        return strings.get(i);
    }

    @Nonnull
    public List<String> getRandomWarshipName(final int amount) {
        final List<String> strings = readAllShipNames();
        final List<String> names = new ArrayList<>();
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
}

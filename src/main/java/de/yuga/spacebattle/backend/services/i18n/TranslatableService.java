package de.yuga.spacebattle.backend.services.i18n;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.i18n.Translatable;
import de.yuga.spacebattle.backend.repositories.i18n.TranslatableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class TranslatableService {

    @Nonnull
    private final TranslatableRepository translatableRepository;

    @Autowired
    public TranslatableService(@Nonnull final TranslatableRepository translatableRepository) {
        Preconditions.checkNotNull(translatableRepository, "translatableRepository must not be empty");

        this.translatableRepository = translatableRepository;
    }

    @Nonnull
    public List<Translatable> saveAll(@Nonnull final Collection<Translatable> toStore) {
        Preconditions.checkNotNull(toStore, "toStore shouldn't be null!");

        final Iterable<Translatable> translatables = translatableRepository.saveAll(toStore);
        return StreamSupport.stream(translatables.spliterator(), false).collect(Collectors.toList());
    }

    @Nonnull
    public List<Translatable> findAll() {
        return Objects.requireNonNullElse(translatableRepository.findAll(), new ArrayList<>());
    }

    public Translatable save(@Nonnull final Translatable translatable) {
        Preconditions.checkNotNull(translatable, "translatable must not be empty");

        return translatableRepository.save(translatable);
    }

    @Nullable
    public Translatable findById(final int idTranslatable) {
        return translatableRepository.findById(idTranslatable).orElse(null);
    }

    @Nullable
    @Deprecated(since = "Just a creation helper")
    public String provideTranslation(final String en) {
        if (en.equals("Rocket Ammunition")) {
            return "Raketenmunition";
        }
        if (en.equals("'Counter Rocket Ammunition")) {
            return "Gegenraketenmunition";
        }
        if (en.equals("A bunch of rockets.")) {
            return "Ein Haufen Raketen.";
        }
        if (en.equals("Another bunch of rockets.")) {
            return "Ein weiterer Haufen Raketen.";
        }
        if (en.equals("Armor Mk I")) {
            return "Rüstung Mk I";
        }
        if (en.equals("An armor")) {
            return "Eine Rüstung";
        }
        if (en.equals("Construction Yard")) {
            return "Bauhof";
        }
        if (en.equals("Orbitals Construction Yard")) {
            return "Orbital Bauhof";
        }
        if (en.equals("Research Laboratories")) {
            return "Forschungslabore";
        }
        if (en.equals("Market place")) {
            return "Marktplatz";
        }
        if (en.equals("Metal works")) {
            return "Metallbearbeitung";
        }
        if (en.equals("Special orbital ores")) {
            return "Spezielle orbitale Erze";
        }
        if (en.equals("Asynchronous Investigations")) {
            return "Asynchrone Ermittlungen";
        }
        if (en.equals("Living room")) {
            return "Wohnzimmer";
        }
        if (en.equals("Hospital")) {
            return "Krankenhaus";
        }
        if (en.equals("Elementary schools")) {
            return "Grundschulen";
        }
        if (en.equals("Secondary schools")) {
            return "Weiterführende Schulen";
        }
        if (en.equals("University")) {
            return "Universität";
        }
        if (en.equals("Teams Rank School")) {
            return "Mannschaftsschule";
        }
        if (en.equals("Military Academy")) {
            return "Militärakademie";
        }
        if (en.equals("The construction yard construct constructions.")) {
            return "Der Bauhof baut Bauwerke.";
        }
        if (en.equals("The construction yard construct orbital constructions.")) {
            return "Der Bauhof baut orbitale Konstruktionen.";
        }
        if (en.equals("The lab investigates researches.")) {
            return "Das Labor untersucht Forschungen.";
        }
        if (en.equals("The market makes money.")) {
            return "Der Markt verdient Geld.";
        }
        if (en.equals("Metals for progress.")) {
            return "Metalle für den Fortschritt.";
        }
        if (en.equals("Heavier metals for more progress.")) {
            return "Schwerere Metalle für mehr Fortschritt.";
        }
        if (en.equals("Rare elements for the future.")) {
            return "Seltene Elemente für die Zukunft.";
        }
        if (en.equals("Everyone needs a home")) {
            return "Jeder braucht ein Zuhause";
        }
        if (en.equals("Everyone needs a doctor")) {
            return "Jeder braucht einen Arzt";
        }
        if (en.equals("a school")) {
            return "eine Schule";
        }
        if (en.equals("another school")) {
            return "eine andere Schule";
        }
        if (en.equals("a university")) {
            return "eine Universität";
        }
        if (en.equals("for the guys which are loud")) {
            return "für die Jungs, die laut sind";
        }
        if (en.equals("for the guys which are silent")) {
            return "für die Jungs, die schweigen";
        }
        if (en.equals("Scanner Mk I")) {
            return "Scanner Mk I";
        }
        if (en.equals("A scanner")) {
            return "Ein Scanner";
        }
        if (en.equals("Corvette vessel")) {
            return "Korvettenschiff";
        }
        if (en.equals("Frigate vessel")) {
            return "Fregattenschiff";
        }
        if (en.equals("Cruiser vessel")) {
            return "Kreuzerschiff";
        }
        if (en.equals("The corvette hull")) {
            return "Der Korvettenrumpf";
        }
        if (en.equals("The frigate hull")) {
            return "Der Fregattenrumpf";
        }
        if (en.equals("The cruiser hull")) {
            return "Der Rumpf des Kreuzers";
        }
        if (en.equals("Ship killer launcher Mk I")) {
            return "Schiffskillerwerfer Mk I";
        }
        if (en.equals("Counter missile launcher Mk I")) {
            return "Abwehrraketenwerfer Mk I";
        }
        if (en.equals("The launcher for ship killers")) {
            return "Der Werfer für Schiffskiller";
        }
        if (en.equals("The launcher for counter missiles")) {
            return "Der Werfer für Gegenraketen";
        }
        if (en.equals("Improves armor")) {
            return "Verbessert die Rüstung";
        }
        if (en.equals("Increases the amount of armor")) {
            return "Erhöht die Menge an Rüstung";
        }
        if (en.equals("Speed Mk I")) {
            return "Geschwindigkeit Mk I";
        }
        if (en.equals("FTL Speed Mk I")) {
            return "Überlicht Geschwindigkeit Mk I";
        }
        if (en.equals("A drive")) {
            return "Ein Antrieb";
        }
        if (en.equals("A FTL drive")) {
            return "Ein Überlicht Antrieb";
        }
        if (en.equals("Eternal live")) {
            return "Ewiges Leben";
        }
        if (en.equals("Laboratories")) {
            return "Labore";
        }
        if (en.equals("Laser")) {
            return "Laser";
        }
        if (en.equals("Missile")) {
            return "Rakete";
        }
        if (en.equals("Counter Missile")) {
            return "Gegenrakete";
        }
        if (en.equals("Point Defense")) {
            return "Punktverteidigung";
        }
        if (en.equals("Armor")) {
            return "Rüstung";
        }
        if (en.equals("Shield")) {
            return "Schild";
        }
        if (en.equals("Speed")) {
            return "Geschwindigkeit";
        }
        if (en.equals("FTL Speed")) {
            return "Überlicht Geschwindigkeit";
        }
        if (en.equals("Electronic Warfare")) {
            return "Elektronische Kriegsführung";
        }
        if (en.equals("Point Defense Ammunition")) {
            return "Punktverteidigungsmunition";
        }
        if (en.equals("Counter Rocket Ammunition")) {
            return "Gegenraketenmunition";
        }
        if (en.equals("Armor improvement I")) {
            return "Rüstungsverbesserung I";
        }
        if (en.equals("Corvette")) {
            return "Korvette";
        }
        if (en.equals("Frigate")) {
            return "Fregatte";
        }
        if (en.equals("Cruiser")) {
            return "Kreuzer";
        }
        if (en.equals("How to buy wine.")) {
            return "Wie man Wein kauft.";
        }
        if (en.equals("The construction yard research researches the construction yard.")) {
            return "Die Bauhofforschung erforscht den Bauhof.";
        }
        if (en.equals("The orbitals Construction Yard research researches the orbitals construction yard.")) {
            return "Die Orbital-Bauhofforschung erforscht den Orbital-Bauhof.";
        }
        if (en.equals("The laboratories research researches laboratories.")) {
            return "Die Labore forschen forschen Labore.";
        }
        if (en.equals("The Market place research researches Market places.")) {
            return "Die Marktplatzforschung erforscht Marktplätze.";
        }
        if (en.equals("The Metal works research researches Metal works.")) {
            return "Die Metallwerksforschung erforscht Metallwerke.";
        }
        if (en.equals("The Special orbital ores research researches Special orbital ores.")) {
            return "Die Spezialorbital-Erze-Forschung erforscht Spezialorbital-Erze.";
        }
        if (en.equals("The Asynchronous Investigations research researches Asynchronous Investigations.")) {
            return "Die Forschung zu asynchronen Ermittlungen untersucht asynchrone Ermittlungen.";
        }
        if (en.equals("The Laser research researches ...")) {
            return "Die Laserforschung erforscht ...";
        }
        if (en.equals("The Missile research researches ...")) {
            return "Die Raketenforschung erforscht ...";
        }
        if (en.equals("The Counter Missile research researches ...")) {
            return "Die Counter-Missile-Forschung erforscht ...";
        }
        if (en.equals("The point defense research researches ...")) {
            return "Die Punktverteidigungsforschung forscht ...";
        }
        if (en.equals("The Armor research researches ...")) {
            return "Die Rüstungsforschung erforscht ...";
        }
        if (en.equals("The Shield research researches ...")) {
            return "Die Shield-Forschung erforscht ...";
        }
        if (en.equals("The Speed research researches sub light ...")) {
            return "Die Speed-Forschung erforscht Unterlicht ...";
        }
        if (en.equals("The FTL Speed research researches FTL ...")) {
            return "Die FTL-Speed-Forschung erforscht FTL ...";
        }
        if (en.equals("The EW research researches electronic warfare.")) {
            return "Die EW-Forschung erforscht die elektronische Kriegsführung.";
        }
        if (en.equals("a bunch of rockets.")) {
            return "ein Haufen Raketen.";
        }
        if (en.equals("a bunch of bullets.")) {
            return "ein Haufen Kugeln.";
        }
        if (en.equals("another bunch of rockets.")) {
            return "ein weiterer Haufen Raketen.";
        }
        if (en.equals("Improves the armor improvement module")) {
            return "Verbessert das Rüstungsverbesserungsmodul";
        }
        if (en.equals("The Corvette research researches Corvettes.")) {
            return "Die Korvettenforschung erforscht Korvetten.";
        }
        if (en.equals("The Frigate research researches Frigates.")) {
            return "Die Fregattenforschung erforscht Fregatten.";
        }
        if (en.equals("The Cruiser research researches Cruisers.")) {
            return "Die Kreuzerforschung erforscht Kreuzer.";
        }
        if (en.equals("Shield Mk I")) {
            return "Schild Mk I";
        }
        if (en.equals("A shield")) {
            return "Ein Schild";
        }
        if (en.equals("Laser Mk I")) {
            return "Laser Mk I";
        }
        if (en.equals("Point Defense Mk I")) {
            return "Punktverteidigung Mk I";
        }
        if (en.equals("A laser")) {
            return "Ein Laser";
        }
        if (en.equals("A point defense")) {
            return "Eine Punktverteidigung";
        }
        if (en.equals("Nuclear ship killer war head")) {
            return "Atomschiff-Killer-Kriegskopf";
        }
        if (en.equals("Counter war head")) {
            return "Gegenkriegskopf";
        }
        if (en.equals("Ship Killer Motor Mk I")) {
            return "Schiff Killer Motor Mk I";
        }
        if (en.equals("Counter Motor Mk I")) {
            return "Gegenmotor Mk I";
        }
        if (en.equals("Nuclear ship killer missile Mk I")) {
            return "Nukleare Schiffskillerrakete Mk I";
        }
        if (en.equals("Counter missile Mk I")) {
            return "Gegenrakete Mk I";
        }
        return "Stub text";
    }
}

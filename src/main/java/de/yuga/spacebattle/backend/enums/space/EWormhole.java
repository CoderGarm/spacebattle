package de.yuga.spacebattle.backend.enums.space;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.entities.orbitals.StarSystem;
import de.yuga.spacebattle.rest.dto.misc.wormhole.WormholeNexus;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum EWormhole {

    MANTICORE("Manticore", "Basilisk", "Trevor's Star", "Hennesy", "Sigma Draconis", "Lynx B (Terminus)", "Matapan", "Gregor"),
    EREWHON("Erewhon", "Terra Haute", "Joshua", "Sasebo"),
    ASGARD("Asgard", "Durandel", "Midgard"),
    PRIME("Prime", "Ajay"),
    AGUEDA("Agueda", "Stine"),
    CLARENCE("Clarence", "Artesia"),
    DIONIGI("Dionigi", "Katharina"),
    FRANZEKI("Franzeki", "Bessie"),
    IDAHO("Idaho", "Zunker"),
    CALVIN("Calvin", "J-156-18(L)"),
    MANNERHEIM("Mannerheim", "Warner"),
    NOLAN("Nolan", "Katharina"),
    SYOU_TANG("Syou-tang", "Olivia"),
    WLOCLAWEK("Włocławek", "Sarduchi"),
    VISIGOTH("Visigoth", "Mesa", "Epsilon Virgo"),
    TITANIA("Titania", "Mullins"),
    YILDUN("Yildun", "Templar", "Dickerson", "Mascot"),
    CONGO("Congo", "SGC-902-36-G"),
    FELIX("Felix", "SGC-902-36-G", "Darius"),
    ROULETTE("Roulette", "Limbo");

    @Nonnull
    private final WormholeNexus wormhole;

    EWormhole(@Nonnull final String nexusName, @Nonnull final String... terminiNames) {
        Preconditions.checkNotNull(nexusName, "nexusName must not be empty");
        Preconditions.checkNotNull(terminiNames, "terminiNames must not be empty");

        this.wormhole = new WormholeNexus(nexusName, terminiNames);
    }

    public static boolean areSystemsConnected(@Nonnull final StarSystem o1, @Nonnull final StarSystem o2) {
        Preconditions.checkNotNull(o1, "o1 must not be empty");
        Preconditions.checkNotNull(o2, "o2 must not be empty");

        return Arrays.stream(EWormhole.values()).anyMatch(wormhole -> wormhole.getWormhole().areSystemsConnected(o1, o2));
    }

    @Nonnull
    public WormholeNexus getWormhole() {
        return wormhole;
    }

    @Nonnull
    public static Set<String> getWormholeNames() {
        return Arrays.stream(EWormhole.values()).map(e -> {
                    final Set<String> names = new HashSet<>(e.getWormhole().getTerminiNames());
                    names.add(e.getWormhole().getNexusName());
                    return names;
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}



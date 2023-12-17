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

    public static int getConnectionGrade(@Nonnull final StarSystem o1, @Nonnull final StarSystem o2) {
        Preconditions.checkNotNull(o1, "o1 must not be empty");
        Preconditions.checkNotNull(o2, "o2 must not be empty");

        if (!EWormhole.areSystemsConnected(o1, o2)) {
            return Integer.MAX_VALUE;
        }

        final String o1Name = o1.getName();
        final String o2Name = o2.getName();

        final Set<EWormhole> wormholes = Arrays.stream(EWormhole.values())
                .filter(wormhole -> wormhole.getWormhole().getTerminiNames().contains(o1Name) || wormhole.getWormhole().getTerminiNames().contains(o2Name))
                .collect(Collectors.toSet());

        if (wormholes.contains(EWormhole.CONGO) || wormholes.contains(EWormhole.FELIX)) {
            // SGC-902-36-G Wormhole Anomaly
            final boolean onlyCongo = EWormhole.CONGO.contains(o1Name) && EWormhole.CONGO.contains(o2Name);
            final boolean onlyFelix = EWormhole.FELIX.contains(o1Name) && EWormhole.FELIX.contains(o2Name);
            if (!onlyCongo && !onlyFelix) {
                final boolean noNexusInvolved = "SGC-902-36-G".equals(o1Name) || "SGC-902-36-G".equals(o2Name);
                return noNexusInvolved ? 1 : 0;
            }
        }

        final boolean noNexusInvolved = wormholes.stream().noneMatch(w -> w.getWormhole().getNexusName().equals(o1Name) || w.getWormhole().getNexusName().equals(o2Name));
        return noNexusInvolved ? 1 : 0;
    }

    @Nonnull
    public WormholeNexus getWormhole() {
        return wormhole;
    }

    public boolean contains(@Nonnull final String name) {
        Preconditions.checkNotNull(name, "name must not be empty");

        return wormhole.getTerminiNames().contains(name);
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



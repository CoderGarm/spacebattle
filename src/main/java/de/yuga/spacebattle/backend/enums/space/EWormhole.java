package de.yuga.spacebattle.backend.enums.space;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.dto.misc.wormhole.WormholeNexus;

import javax.annotation.Nonnull;

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

    @Nonnull
    public WormholeNexus getWormhole() {
        return wormhole;
    }
}



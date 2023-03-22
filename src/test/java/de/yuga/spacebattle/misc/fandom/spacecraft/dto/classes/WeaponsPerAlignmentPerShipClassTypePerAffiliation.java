package de.yuga.spacebattle.misc.fandom.spacecraft.dto.classes;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EShipClassType;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import de.yuga.spacebattle.misc.fandom.spacecraft.dto.Weaponry;
import de.yuga.spacebattle.misc.fandom.spacecraft.dto.WikiShipClass;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WeaponsPerAlignmentPerShipClassTypePerAffiliation {

    @Nonnull
    private final String affiliation;

    @Nonnull
    private final EShipClassType shipClassType;

    private final Map<EWeaponAlignment, Integer> map = new HashMap<>();

    int counterShipClasses = 0;

    public WeaponsPerAlignmentPerShipClassTypePerAffiliation(@Nonnull final String affiliation, @Nonnull final EShipClassType shipClassType) {
        this.affiliation = Preconditions.checkNotNull(affiliation, "affiliation must not be empty");
        this.shipClassType = Preconditions.checkNotNull(shipClassType, "shipClassType must not be empty");
    }


    public void add(@Nonnull final WikiShipClass wikiShipClass) {
        Preconditions.checkNotNull(wikiShipClass, "wikiShipClass must not be empty");

        final Weaponry weaponry = wikiShipClass.getWeaponry();
        final Map<EWeaponAlignment, Map<String, Integer>> alignmentSet = weaponry.getAlignmentSet();
        alignmentSet.forEach((eWeaponAlignment, weaponTypeToAmount) -> {
            final Integer amount = weaponTypeToAmount.values().stream().reduce(0, Integer::sum);
            map.put(eWeaponAlignment, amount);
        });
        counterShipClasses++;
    }

    @Nonnull
    public String getAffiliation() {
        return affiliation;
    }

    @Nonnull
    public EShipClassType getShipClassType() {
        return shipClassType;
    }

    public Map<EWeaponAlignment, Integer> getMap() {
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() / counterShipClasses));
    }
}

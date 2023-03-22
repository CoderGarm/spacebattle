package de.yuga.spacebattle.misc.fandom.spacecraft.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EWeaponAlignment;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Weaponry {

    @Nonnull
    private final Map<String, Integer> weaponSet = new HashMap<>();

    /**
     * Type of weapon to amount by alignment
     */
    @Nonnull
    private final Map<EWeaponAlignment, Map<String, Integer>> alignmentSet = new HashMap<>();

    public Weaponry(@Nonnull final List<String> rawValues) {
        Preconditions.checkNotNull(rawValues, "rawValues must not be empty");

        detectArmament("Raketenwerfer", rawValues);
        detectArmament("Laser", rawValues);
        detectArmament("Graser", rawValues);
        detectArmament("Antiraketenwerfer", rawValues);
        detectArmament("Lasercluster", rawValues);
    }

    private void detectArmament(@Nonnull final String keyword, @Nonnull final List<String> rawValues) {
        Preconditions.checkNotNull(keyword, "keyword must not be empty");
        Preconditions.checkNotNull(rawValues, "rawValues must not be empty");

        Pattern p;
        Matcher m;
        for (int i = 0; i < rawValues.size(); i++) {
            final String line = rawValues.get(i);

            boolean goOn = false;
            p = Pattern.compile("^(?<amount>\\d+)\\s.*\\b(?<type>\\w+)\\b");
            m = p.matcher(line);
            if (m.find()) {
                final int amount = Integer.parseInt(m.group("amount"));
                final String type = m.group("type");
                if (type.equalsIgnoreCase(keyword)) {
                    goOn = true;
                    weaponSet.put(keyword, amount);
                }
            }

            if (goOn) {
                for (int j = i + 1; j < rawValues.size(); j++) {
                    final String next = rawValues.get(j);

                    p = Pattern.compile("(?<counter>1x|2x)(?<amount>[0-9]+).*(?<place>breit|jagd)(.*(?<special>bug|heck))*");
                    m = p.matcher(next.toLowerCase());
                    if (m.find()) {
                        final Integer multiplier = Integer.parseInt(m.group("counter").trim().split("x")[0]);
                        final Integer amount = Integer.parseInt(m.group("amount"));
                        final String alignment = m.group("place");
                        final String special = m.group("special");
                        final int weaponsCount = multiplier * amount;
                        if (StringUtils.isNotBlank(special)) {
                            Map<String, Integer> orDefault;
                            if (alignment.toLowerCase().contains("jagd")) {
                                if (special.toLowerCase().contains("bug")) {
                                    orDefault = alignmentSet.getOrDefault(EWeaponAlignment.BOW, new HashMap<>());
                                    orDefault.put(keyword, weaponsCount);
                                    alignmentSet.put(EWeaponAlignment.BOW, orDefault);
                                } else if (special.toLowerCase().contains("heck")) {
                                    orDefault = alignmentSet.getOrDefault(EWeaponAlignment.STERN, new HashMap<>());
                                    orDefault.put(keyword, weaponsCount);
                                    alignmentSet.put(EWeaponAlignment.STERN, orDefault);
                                }
                            }
                        } else {
                            Map<String, Integer> orDefault;
                            if (alignment.toLowerCase().contains("jagd")) {
                                orDefault = alignmentSet.getOrDefault(EWeaponAlignment.BOW, new HashMap<>());
                                orDefault.put(keyword, amount);
                                alignmentSet.put(EWeaponAlignment.BOW, orDefault);
                                orDefault = alignmentSet.getOrDefault(EWeaponAlignment.STERN, new HashMap<>());
                                orDefault.put(keyword, amount);
                                alignmentSet.put(EWeaponAlignment.STERN, orDefault);
                            } else if (alignment.toLowerCase().contains("breit")) {
                                orDefault = alignmentSet.getOrDefault(EWeaponAlignment.BROADSIDE, new HashMap<>());
                                orDefault.put(keyword, weaponsCount);
                                alignmentSet.put(EWeaponAlignment.BROADSIDE, orDefault);
                            }
                        }
                    } else {
                        i = j;
                        break;
                    }
                }
            }
        }
    }

    @Nonnull
    public Map<String, Integer> getWeaponSet() {
        return weaponSet;
    }

    @Nonnull
    public Map<EWeaponAlignment, Map<String, Integer>> getAlignmentSet() {
        return alignmentSet;
    }
}

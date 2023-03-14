package de.yuga.spacebattle.misc.fandom.spacecraft.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.ECrewType;
import de.yuga.spacebattle.backend.enums.EEducationType;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crew {

    private static final String[] TO_REMOVE = {"<", ">", "\\."};

    private final static String[] ENLISTED = {"mannschaften", "crew", "unteroffiziere"};
    private final static String OFFICER = "offizier";

    private final static String NAVY = "navy";
    private final static String[] MARINES = {"marines", "truppen"};
    private final static String STAB = "stab";
    private final static String LAC = "lac";

    private int overallAmount = 0;

    @Nonnull
    private final Map<ECrewType, List<CrewAmount>> crewTypeMapMap = new HashMap<>();

    public Crew(@Nonnull final WikiShipClass shipClass) {
        Preconditions.checkNotNull(shipClass, "shipClass must not be empty");

        detectCrew(shipClass.getRawValues(FieldName.Crew));
    }

    private void detectCrew(@Nonnull final List<String> rawValues) {
        Preconditions.checkNotNull(rawValues, "rawValues must not be empty");

        for (int i = 0; i < rawValues.size(); i++) {
            final String line = sanitize(rawValues, i);
            if (!getGroupedDetails(line)) {
                if (!getFullAmount(line)) {
                    if (!getTypeAmount(line)) {
                        if (!getGradeAmount(line)) {
                            if (!getPureSums(line)) {
                                System.out.println("Not parsable: '" + line + "'");
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean getPureSums(@Nonnull final String line) {
        Preconditions.checkNotNull(line, "line must not be empty");

        final Pattern p = Pattern.compile("^(?<first>[0-9]+)\\s*\\+{1}\\s*(?<second>[0-9]+)$");
        final Matcher m = p.matcher(line);
        if (m.find()) {
            final int first = Integer.parseInt(m.group("first"));
            final int second = Integer.parseInt(m.group("second"));
            final int smaller = Integer.min(first, second);
            final int bigger = Integer.max(first, second);
            final List<CrewAmount> orDefault = this.crewTypeMapMap.getOrDefault(ECrewType.NAVY, new ArrayList<>());
            orDefault.add(new CrewAmount(EEducationType.ENLISTED, bigger));
            orDefault.add(new CrewAmount(EEducationType.OFFICER, smaller));
            this.crewTypeMapMap.put(ECrewType.NAVY, orDefault);
            return true;
        }
        return false;
    }

    private boolean getGradeAmount(@Nonnull final String line) {
        Preconditions.checkNotNull(line, "line must not be empty");

        final Pattern p = Pattern.compile("(?<sum>[0-9]+)\\s*(?<type>[a-zA-Z]+)");
        final Matcher m = p.matcher(line);
        if (m.find()) {
            final int sum = Integer.parseInt(m.group("sum"));
            final String type = m.group("type");
            final EEducationType educationType = getEducationType(type);
            if (educationType != null) {
                final List<CrewAmount> orDefault = this.crewTypeMapMap.getOrDefault(ECrewType.NAVY, new ArrayList<>());
                orDefault.add(new CrewAmount(educationType, sum));
                this.crewTypeMapMap.put(ECrewType.NAVY, orDefault);
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static EEducationType getEducationType(@Nullable final String type) {
        if (StringUtils.isNotEmpty(type) && type.toLowerCase().contains(OFFICER.toLowerCase())) {
            return EEducationType.OFFICER;
        }

        for (final String marker : ENLISTED) {
            if (StringUtils.isNotEmpty(type) && type.toLowerCase().contains(marker.toLowerCase())) {
                return EEducationType.ENLISTED;
            }
        }
        return null;
    }

    private boolean getTypeAmount(@Nonnull final String line) {
        Preconditions.checkNotNull(line, "line must not be empty");

        final Pattern p = Pattern.compile("(?<sum>[0-9]+)\\s*(?<type>[a-zA-Z]+)");
        final Matcher m = p.matcher(line);
        if (m.find()) {
            final int sum = Integer.parseInt(m.group("sum"));
            final String type = m.group("type");
            final ECrewType crewType = getMandatoryCrewType(type);
            if (crewType != null) {
                final List<CrewAmount> orDefault = this.crewTypeMapMap.getOrDefault(crewType, new ArrayList<>());
                orDefault.add(new CrewAmount(sum));
                this.crewTypeMapMap.put(crewType, orDefault);
                return true;
            }
        }
        return false;
    }

    private boolean getFullAmount(@Nonnull final String line) {
        Preconditions.checkNotNull(line, "line must not be empty");

        final Pattern p = Pattern.compile("^\\s*(?<sum>[0-9]+)\\s*$");
        final Matcher m = p.matcher(line);
        if (m.find()) {
            this.overallAmount = Integer.parseInt(m.group("sum"));
            return true;
        }
        return false;
    }

    private boolean getGroupedDetails(@Nonnull final String line) {
        Preconditions.checkNotNull(line, "line must not be empty");

        final Pattern p = Pattern.compile("(?<sum>[0-9]+)\\s*(?<type>[a-zA-Z]*)\\s*\\((?<first>[0-9]*)\\s*\\+{1}\\s*(?<second>[0-9]+)\\)");
        final Matcher m = p.matcher(line);
        if (m.find()) {
            final int sum = Integer.parseInt(m.group("sum"));
            final String type = m.group("type");
            final int first = Integer.parseInt(m.group("first"));
            final int second = Integer.parseInt(m.group("second"));
            final int smaller = Integer.min(first, second);
            final int bigger = Integer.max(first, second);
            final ECrewType crewType = getCrewType(type);

            this.overallAmount += sum;

            final List<CrewAmount> orDefault = this.crewTypeMapMap.getOrDefault(crewType, new ArrayList<>());
            orDefault.add(new CrewAmount(EEducationType.ENLISTED, bigger));
            orDefault.add(new CrewAmount(EEducationType.OFFICER, smaller));
            this.crewTypeMapMap.put(crewType, orDefault);
            return true;
        }
        return false;
    }

    @Nonnull
    private static ECrewType getCrewType(@Nullable final String type) {
        ECrewType crewType = ECrewType.NAVY;
        for (final String marker : MARINES) {
            if (StringUtils.isNotEmpty(type) && type.toLowerCase().contains(marker.toLowerCase())) {
                crewType = ECrewType.MARINES;
                break;
            }
        }
        return crewType;
    }

    @Nullable
    private static ECrewType getMandatoryCrewType(@Nullable final String type) {
        if (StringUtils.isNotEmpty(type) && type.toLowerCase().contains(NAVY.toLowerCase())) {
            return ECrewType.NAVY;
        }

        for (final String marker : MARINES) {
            if (StringUtils.isNotEmpty(type) && type.toLowerCase().contains(marker.toLowerCase())) {
                return ECrewType.MARINES;
            }
        }
        return null;
    }

    private static String sanitize(@Nonnull final List<String> rawValues, final int i) {
        Preconditions.checkNotNull(rawValues, "rawValues must not be empty");

        String line = rawValues.get(i);
        for (final String toRem : TO_REMOVE) {
            line = line.replaceAll(toRem, "");
        }
        return line;
    }

    public int getOverallAmount() {
        return overallAmount;
    }

    @Nonnull
    public Map<ECrewType, List<CrewAmount>> getCrewTypeMapMap() {
        return crewTypeMapMap;
    }

    public static class CrewAmount {

        @Nullable
        private EEducationType educationType;
        private final int amount;

        public CrewAmount(@Nullable final EEducationType educationType, final int amount) {
            this.educationType = educationType;
            this.amount = amount;
        }

        public CrewAmount(final int amount) {
            this.amount = amount;
        }

        @Nullable
        public EEducationType getEducationType() {
            return educationType;
        }

        public int getAmount() {
            return amount;
        }
    }
}

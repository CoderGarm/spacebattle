package de.yuga.spacebattle.misc.fandom.spacecraft.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.backend.enums.EHullType;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class WikiShipClass {

    @Nonnull
    private final Map<FieldName, List<String>> keyValues = new HashMap<>();

    public WikiShipClass(@Nonnull final String wikiText) {
        Preconditions.checkNotNull(wikiText, "wikiText must not be empty");

        final int start = wikiText.indexOf("{{");
        final int end = wikiText.indexOf("}}");
        final String classText = wikiText.substring(start, end);
        final String[] split = classText.split("\\|");

        for (final String keyValue : split) {
            final String[] keyVal = keyValue.split("=");
            final FieldName fieldName = FieldName.getBy(keyVal[0]);
            if (fieldName != null) {
                String text = keyVal[1];
                text = text.replaceAll("\\[\\[", "");
                text = text.replaceAll("]]", "");
                text = text.replaceAll("<br>", "\n");
                text = text.replaceAll("</br>", "\n");
                text = text.replaceAll("</ br>", "\n");
                text = text.replaceAll("<br/>", "\n");
                text = text.replaceAll("<br />", "\n");
                keyValues.put(fieldName, List.of(text.split("\n")));
            }
        }
    }

    public WikiShipClass(@Nonnull final Map<FieldName, List<String>> keyValues) {
        Preconditions.checkNotNull(keyValues, "keyValues must not be empty");

        this.keyValues.putAll(keyValues);
    }

    @Nonnull
    public List<String> getRawValues(@Nonnull final FieldName fieldName) {
        Preconditions.checkNotNull(fieldName, "fieldName must not be empty");

        return keyValues.getOrDefault(fieldName, new ArrayList<>());
    }

    @Nullable
    public String getRawValue(@Nonnull final FieldName fieldName) {
        Preconditions.checkNotNull(fieldName, "fieldName must not be empty");

        final List<String> strings = keyValues.get(fieldName);
        if (strings.isEmpty()) {
            return null;
        }
        return strings.get(0);
    }

    public boolean isValid() {
        return !getRawValues(FieldName.Name).isEmpty()
                && !getRawValues(FieldName.Zugehoerigkeit).isEmpty()
                && !getRawValues(FieldName.Masse).isEmpty()
                && !getRawValues(FieldName.Crew).isEmpty()
                && !getRawValues(FieldName.Beschleunigung).isEmpty()
                && !getRawValues(FieldName.Bewaffnung).isEmpty()
                ;
    }

    private int getNumberOfCsvRows() {
        return keyValues.values().stream().sorted(Comparator.comparingInt(List::size)).map(List::size).reduce((o1, o2) -> o2).orElse(0);
    }

    @Nonnull
    public List<List<String>> getCsvRows() {
        final List<List<String>> result = new ArrayList<>();
        final int numberOfCsvRows = getNumberOfCsvRows();
        for (int i = 0; i < numberOfCsvRows; i++) {
            final List<String> row = new ArrayList<>();
            for (final FieldName fieldName : FieldName.values()) {
                final List<String> values = getRawValues(fieldName);
                if (values.size() > i) {
                    final String value = values.get(i);
                    row.add(value.replaceAll("'", ""));
                } else {
                    row.add("");
                }
            }
            result.add(row);
        }
        return result;
    }

    @Nonnull
    public EHullType getHullType() {
        final String rawValue = getRawValue(FieldName.Typ);

        switch (rawValue) {
            case "Schlachtschiff":
                return EHullType.BB;
            case "Superdreadnought":
                return EHullType.SD;
            case "Dreadnought":
                return EHullType.DN;
            case "Schwerer Kreuzer":
            case "Schwerer Kreuzer - Marinessupport":
                return EHullType.CA;
            case "Zerstörer":
                return EHullType.DD;
            case "Schlachtkreuzer":
                return EHullType.BC;
            case "Leichtes Angriffsboot":
                return EHullType.LAC;
            case "Gondelleger":
            case "Podnought":
                return EHullType.SDP;
            case "Fregatte":
                return EHullType.FG;
            case "LAC-Träger":
                return EHullType.CLAC;
            case "Leichter Kreuzer":
                return EHullType.CL;
            default:
                return EHullType.AR;
        }
    }

    @Nonnull
    public String getName() {
        return getRawValue(FieldName.Name).replaceAll("'", "").replaceAll("\"", "");
    }

    @Nonnull
    public String getAffiliation() {
        return getRawValue(FieldName.Zugehoerigkeit);
    }

    @Nonnull
    public Weaponry getWeaponry() {
        return new Weaponry(getRawValues(FieldName.Bewaffnung));
    }

    @Nonnull
    public ClassPhysics getClassPhysics() {
        return new ClassPhysics(this);
    }

    @Nonnull
    public Crew getCrew() {
        return new Crew(this);
    }

    public String getIntroductionDate() {
        final String rawValue = getRawValue(FieldName.Einfuehrung);
        return StringUtils.isNotBlank(rawValue) ? rawValue : "-";
    }
}

package de.yuga.spacebattle.rest.dto.misc.wiki;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WikiBattleBlock {

    @Nonnull
    private static final Set<Pattern> BATTLE_PATTERN = Set.of(
            Pattern.compile("(\\{\\{battle)(.*?)(\\}\\})", Pattern.DOTALL),
            Pattern.compile("(\\{\\{Infobox\\sMilitärischer\\sKonflikt)(.*?)(\\}\\})", Pattern.DOTALL)
    );

    private static final Set<String> nameField = Set.of("name", "EREIGNIS");
    private static final Set<String> imageField = Set.of("image", "BILD");
    private static final Set<String> conflictField = Set.of("conflict", "TEILVON");
    private static final Set<String> dateField = Set.of("date", "DATUM");
    private static final Set<String> placeField = Set.of("place", "ORT");
    private static final Set<String> resultField = Set.of("result", "AUSGANG");
    private static final Set<String> sideField = Set.of("side", "KONTRAHENT");
    private static final Set<String> commanderField = Set.of("commander", "BEFEHLSHABER");
    private static final Set<String> forceField = Set.of("force", "TRUPPENSTÄRKE");
    private static final Set<String> casualField = Set.of("casual", "VERLUSTE");

    @Nullable
    @JsonIgnore
    private String battleBlockContent;

    @Nonnull
    @JsonProperty
    @Schema(required = true)
    @SuppressWarnings("NotNullFieldNotInitialized")
    private String name;

    @Nullable
    @JsonProperty
    @Schema
    private String image;

    @Nullable
    @JsonProperty
    @Schema
    private String conflict;

    @Nonnull
    @JsonProperty
    @Schema(required = true)
    @SuppressWarnings("NotNullFieldNotInitialized")
    private String date;

    @Nullable
    @JsonProperty
    @Schema(required = true)
    private DateBlock dateBlock;

    @Nonnull
    @JsonProperty
    @Schema(required = true)
    @SuppressWarnings("NotNullFieldNotInitialized")
    private String place;

    @Nonnull
    @JsonProperty
    @Schema(required = true)
    @SuppressWarnings("NotNullFieldNotInitialized")
    private String result;

    @Nonnull
    @JsonProperty
    @Schema(required = true)
    private final List<String> side = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true)
    private final List<String> commander = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true)
    private final List<String> force = new ArrayList<>();

    @Nonnull
    @JsonProperty
    @Schema(required = true)
    private final List<String> casual = new ArrayList<>();

    public WikiBattleBlock(@Nonnull final String text) {
        Preconditions.checkNotNull(text, "text must not be empty");

        final List<String> list = BATTLE_PATTERN.stream().map(pattern -> {
                    final Matcher matcher = pattern.matcher(text);
                    return matcher.find() ? matcher.group(2).trim() : null;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(String::length)).collect(Collectors.toList());
        if (!list.isEmpty()) {
            this.battleBlockContent = list.get(0);
        }

        parse();
    }

    private void parse() {
        if (battleBlockContent == null) {
            return;
        }

        final String[] keyValuePair = battleBlockContent
                .replaceAll("\\|([a-zA-Z0-9]+)\\s*=\\s*", "\n\n|$1 = ")
                .split("\n\n");

        for (final String pair : keyValuePair) {

            final String[] split = pair.split("=");
            if (split.length < 2) {
                continue;
            }

            final String fieldName = split[0].replaceAll("\\|", "")
                    .replaceAll(" ", "")
                    .replaceAll("\n", "")
                    .trim();
            final String fieldValue = split[1].trim();
            setData(fieldName, fieldValue);
        }
    }

    private void setData(@Nonnull final String fieldName, @Nonnull final String value) {
        Preconditions.checkNotNull(fieldName, "fieldName must not be empty");
        Preconditions.checkNotNull(value, "value must not be empty");

        if (nameField.contains(fieldName)) {
            this.name = value;
        }
        if (imageField.contains(fieldName)) {
            this.image = value;
        }
        if (conflictField.contains(fieldName)) {
            this.conflict = value;
        }
        if (dateField.contains(fieldName)) {
            this.date = value;
        }
        if (placeField.contains(fieldName)) {
            this.place = value;
        }
        if (resultField.contains(fieldName)) {
            this.result = value;
        }
        if (sideField.stream().anyMatch(fieldName::startsWith)) {
            this.side.add(value);
        }
        if (commanderField.stream().anyMatch(fieldName::startsWith)) {
            this.commander.add(value);
        }
        if (forceField.stream().anyMatch(fieldName::startsWith)) {
            this.force.add(value);
        }
        if (casualField.stream().anyMatch(fieldName::startsWith)) {
            this.casual.add(value);
        }
    }

    @Nonnull
    @JsonIgnore
    public String getName() {
        return name;
    }

    @Nullable
    @JsonIgnore
    public String getImage() {
        return image;
    }

    @Nullable
    @JsonIgnore
    public String getConflict() {
        return conflict;
    }

    @Nonnull
    @JsonIgnore
    public String getDate() {
        return date;
    }

    @Nonnull
    @JsonIgnore
    public String getPlace() {
        return place;
    }

    @Nonnull
    @JsonIgnore
    public String getResult() {
        return result;
    }

    @Nonnull
    @JsonIgnore
    public List<String> getSide() {
        return side;
    }

    @Nonnull
    @JsonIgnore
    public List<String> getCommander() {
        return commander;
    }

    @Nonnull
    @JsonIgnore
    public List<String> getForce() {
        return force;
    }

    @Nonnull
    @JsonIgnore
    public List<String> getCasual() {
        return casual;
    }

    @Nullable
    @JsonIgnore
    public WikiBattleBlock getValid() {
        return battleBlockContent != null ? this : null;
    }

    @JsonIgnore
    public boolean isValid() {
        return StringUtils.isNotEmpty(name)
                && StringUtils.isNotEmpty(date)
                && StringUtils.isNotEmpty(place)
                && StringUtils.isNotEmpty(result)
                ;
    }

    @Override
    @JsonIgnore
    public String toString() {
        return toJson();
    }

    @Nonnull
    @JsonIgnore
    public String toJson() {
        if (!isValid()) {
            throw new NotifyWebUserException("Nope.");
        }
        final Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        tidyUp();
        return Objects.requireNonNull(sanitize(gson.toJson(this)));
    }

    @Nullable
    public static String sanitize(@Nullable final String string) {

        if (StringUtils.isBlank(string)) {
            return null;
        }

        return string
                .replaceAll("\\[", "")
                .replaceAll("\\]", "")
                .replaceAll("\\/", "_")
                .replaceAll("\\/", "_")
                .replaceAll("<br>", "\n")
                .replaceAll("<br/>", "\n")
                .replaceAll("<br-/>", "\n")
                .replaceAll("<[^>]*>", "")
                .replaceAll("[0-9]{2}th\\sCentury\\sPD\\|", "")
                ;
    }

    @Nonnull
    @JsonIgnore
    public String printEn() {
        if (!isValid()) {
            throw new NotifyWebUserException("Nope.");
        }

        String result = "";
        result += ln("{{battle");

        result += ln("name = " + name);
        result += ln("image = " + image);
        result += ln("conflict = " + conflict);
        result += ln("date = " + date);
        result += ln("place = " + place);
        result += ln("result = " + result);
        for (int i = 0; i < side.size(); i++) {
            //noinspection StringConcatenationInLoop
            result += ln("side" + (i + 1) + " = " + side.get(i));
        }
        for (int i = 0; i < commander.size(); i++) {
            //noinspection StringConcatenationInLoop
            result += ln("commander" + (i + 1) + " = " + commander.get(i));
        }
        for (int i = 0; i < force.size(); i++) {
            //noinspection StringConcatenationInLoop
            result += ln("force" + (i + 1) + " = " + force.get(i));
        }
        for (int i = 0; i < casual.size(); i++) {
            //noinspection StringConcatenationInLoop
            result += ln("casual" + (i + 1) + " = " + casual.get(i));
        }
        result += ln("}}");
        return result;
    }

    @Nonnull
    @JsonIgnore
    public String printDe() {
        if (!isValid()) {
            throw new NotifyWebUserException("Nope.");
        }

        String result = "";
        result += ln("{{Infobox Militärischer Konflikt");

        result += ln("EREIGNIS = " + name);
        result += ln("BILD = " + image);
        result += ln("TEILVON = " + conflict);
        result += ln("DATUM = " + date);
        result += ln("ORT = " + place);
        result += ln("AUSGANG = " + this.result);
        for (int i = 0; i < side.size(); i++) {
            //noinspection StringConcatenationInLoop
            result += ln("KONTRAHENT" + (i + 1) + " = " + side.get(i));
        }
        for (int i = 0; i < commander.size(); i++) {
            //noinspection StringConcatenationInLoop
            result += ln("BEFEHLSHABER" + (i + 1) + " = " + commander.get(i));
        }
        for (int i = 0; i < force.size(); i++) {
            //noinspection StringConcatenationInLoop
            result += ln("TRUPPENSTÄRKE" + (i + 1) + " = " + force.get(i));
        }
        for (int i = 0; i < casual.size(); i++) {
            //noinspection StringConcatenationInLoop
            result += ln("VERLUSTE" + (i + 1) + " = " + casual.get(i));
        }
        result += ln("}}");
        return result;
    }

    private String ln(@Nonnull final String string) {
        Preconditions.checkNotNull(string, "string must not be empty");

        if (string.endsWith("\n")) {
            return string;
        }
        return string + "\n";
    }

    public void tidyUp() {
        this.battleBlockContent = null;

        name = Objects.requireNonNull(sanitize(name));
        image = sanitize(image);
        conflict = sanitize(conflict);
        date = Objects.requireNonNull(sanitize(date));
        dateBlock = new DateBlock(getDay(), getMonth(), getYearPD());
        place = Objects.requireNonNull(sanitize(place));
        result = Objects.requireNonNull(sanitize(result));

        List<String> sanitizedContentLines = side.stream().map(WikiBattleBlock::sanitize).collect(Collectors.toList());
        side.clear();
        side.addAll(sanitizedContentLines);

        sanitizedContentLines = commander.stream().map(WikiBattleBlock::sanitize).collect(Collectors.toList());
        commander.clear();
        commander.addAll(sanitizedContentLines);

        sanitizedContentLines = force.stream().map(WikiBattleBlock::sanitize).collect(Collectors.toList());
        force.clear();
        force.addAll(sanitizedContentLines);

        sanitizedContentLines = casual.stream().map(WikiBattleBlock::sanitize).collect(Collectors.toList());
        casual.clear();
        casual.addAll(sanitizedContentLines);
    }

    @Nonnull
    @JsonIgnore
    public String getYearPD() {
        String date = WikiBattleBlock.sanitize(getDate());

        final String regex = "([a-zA-Z]*[0-9]{4}\\sPD)";
        final Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(date);
        while (matcher.find()) {
            date = matcher.group(1).trim();
        }
        return date;
    }

    @Nullable
    @JsonIgnore
    public String getMonth() {
        String date = WikiBattleBlock.sanitize(getDate());

        final String regex = "(\\b\\d{1,2}\\D{0,3})?\\b(Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|(Nov|Dec)(?:ember)?)\\D?(\\d{1,2}\\D?)?\\D?((19[7-9]\\d|20\\d{2})|\\d{2})";
        final Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(date);
        while (matcher.find()) {
            return matcher.group(2).trim();
        }
        return null;
    }

    @Nullable
    @JsonIgnore
    public String getDay() {
        String date = WikiBattleBlock.sanitize(getDate());

        final String regex = "(\\b\\d{1,2}\\D{0,3})?\\b(Jan(?:uary)?|Feb(?:ruary)?|Mar(?:ch)?|Apr(?:il)?|May|Jun(?:e)?|Jul(?:y)?|Aug(?:ust)?|Sep(?:tember)?|Oct(?:ober)?|(Nov|Dec)(?:ember)?)\\D?(\\d{1,2}\\D?)?\\D?((19[7-9]\\d|20\\d{2})|\\d{2})";
        final Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(date);
        while (matcher.find()) {
            final String first = matcher.group(1);
            final String second = matcher.group(4);
            final String result = first != null ? first : second;
            return result != null ? result.trim().replaceAll(",", "") : null;
        }
        return null;
    }
}

package de.yuga.spacebattle.misc.fandom.battle.dto;

import com.google.common.base.Preconditions;
import de.yuga.spacebattle.rest.api.error.NotifyWebUserException;
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
    private String battleBlockContent;

    @Nonnull
    @SuppressWarnings("NotNullFieldNotInitialized")
    private String name;

    @Nullable
    private String image;

    @Nullable
    private String conflict;

    @Nonnull
    @SuppressWarnings("NotNullFieldNotInitialized")
    private String date;

    @Nonnull
    @SuppressWarnings("NotNullFieldNotInitialized")
    private String place;

    @Nonnull
    @SuppressWarnings("NotNullFieldNotInitialized")
    private String result;

    @Nonnull
    private final List<String> side = new ArrayList<>();

    @Nonnull
    private final List<String> commander = new ArrayList<>();

    @Nonnull
    private final List<String> force = new ArrayList<>();

    @Nonnull
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
                .replaceAll("\\|([a-zA-Z0-9]+)\\s*=\\s*", "\n|$1 = ")
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
            setDate(fieldName, fieldValue);
        }
    }

    private void setDate(@Nonnull final String fieldName, @Nonnull final String value) {
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
    public String getName() {
        return name;
    }

    @Nullable
    public String getImage() {
        return image;
    }

    @Nullable
    public String getConflict() {
        return conflict;
    }

    @Nonnull
    public String getDate() {
        return date;
    }

    @Nonnull
    public String getPlace() {
        return place;
    }

    @Nonnull
    public String getResult() {
        return result;
    }

    @Nonnull
    public List<String> getSide() {
        return side;
    }

    @Nonnull
    public List<String> getCommander() {
        return commander;
    }

    @Nonnull
    public List<String> getForce() {
        return force;
    }

    @Nonnull
    public List<String> getCasual() {
        return casual;
    }

    @Nullable
    public WikiBattleBlock getValid() {
        return battleBlockContent != null ? this : null;
    }

    public boolean isValid() {
        return StringUtils.isNotEmpty(name)
                && StringUtils.isNotEmpty(date)
                && StringUtils.isNotEmpty(place)
                && StringUtils.isNotEmpty(result)
                ;
    }

    @Nonnull
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
        result += ln("AUSGANG = " + result);
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
}

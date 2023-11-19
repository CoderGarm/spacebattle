package de.yuga.spacebattle.misc.fandom.battle.dto;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WikiBattleBlock {

    @Nonnull
    private static final Set<Pattern> BATTLE_PATTERN = Set.of(
            Pattern.compile("(\\{\\{battle)(.*?)(\\}\\})", Pattern.DOTALL)
    );

    /*
    {{battle
        |name = Battle of Talbot
        |image=[[File:Battlesign RMN vs RHN PN.png]]
        |conflict = [[First Haven-Manticore War]]<br/>[[Operation Perseus]]
        |date = [[20th Century PD|1904 PD]]
        |place = [[Talbot System]]
        |result = Manticoran victory
        |side1 = [[Royal Manticoran Navy]]<br/>- [[HMS Bellerophon (Bellerophon class)|HMS ''Bellerophon'']]
        |side2 = [[Republic of Haven Navy|People's Navy]]<br/>- [[Battlecruiser Squadron 14 (PN)|Battlecruiser Squadron 14]]
        |commanders1 = [[Avshari|Lt. Cdm. Avshari]] OOD
        |commanders2 = [[Edward Pierre|Rear Adm. Edward Pierre]] (†)
        |forces1 = 1 DN
        |forces2 = 4 BC
        |casual1 = none
        |casual2 = 4 BC
    }}
     */

    @Nullable
    private String battleBlockContent;

    @Nonnull
    private String name;

    @Nonnull
    private String image;

    @Nonnull
    private String conflict;

    @Nonnull
    private String date;

    @Nonnull
    private String place;

    @Nonnull
    private String result;

    @Nonnull
    private final Set<String> sides = new HashSet<>();

    @Nonnull
    private final Set<String> commander = new HashSet<>();

    @Nonnull
    private final Set<String> forces = new HashSet<>();

    @Nonnull
    private final Set<String> casuals = new HashSet<>();

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

        // fixme hier weiter
    }

    @Nullable
    public WikiBattleBlock getValid() {
        return battleBlockContent != null ? this : null;
    }
}

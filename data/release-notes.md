### Features

#### Gameplay

1. Piraten raiden nur noch Planeten von NPCs und Spielern.  
   Sie sollen zwar die Umgebung gefährlich machen, aber Spielern nicht ihre anfängliche Möglichkeit an Reichtum zu kommen nehmen.
2. Neue Spieler starten mit einem für Piraten sehr uninteressanten Hauptplaneten.
3. Der Einfluss von Kriegsschiffen auf das nachbarschaftliche Verhältnis zu Piraten ist schwächer geworden.  
   Das wirkt im ersten Moment widersinnig, allerdings haben Piraten verschiedene Aspekte im Spielgeschehen und ich möchte die vorsichtig ausbalancieren.

todo. gebäudelevel deaktivieren (bis auf level 1 für das saubermachen)

#### Diverses

1. Die Auftragsübersicht zeigt nun auch Schiffswerften ohne aktiven Job an.
2. Im Forum werden nun bisher ungelesene Nachrichten markiert, nicht nur Foren und Fäden.
3. Man kann nun ganze Foren oder ganze Fäden auf einmal als gelesen markieren.
4. Das Löschen des Browsercaches sollte(!) jetzt nicht mehr notwendig sein.  
   Ich kann dem Browser allerdings nur sagen "Bitte keinen Cache benutzen" - was daraus gemacht wird, liegt außerhalb meiner Kontrolle.
5. Die Seite ist nun etwas besser mit einem Mobilgerät nutzbar.
6. Das Journal hat Markierungen für neue, ungelesene und relevante Informationen bekommen.
7. Der Beirat (Advisory Board) leitet nun detaillierter durch die Schritte zur Schiffswerft.

todo. quoting in chat und forum
todo. light theme
todo. introduce fleet edit tab: rename, retire -> no edit when in move
todo. history for dashboard
todo. cache cleanup or only load last 3 ticks

### Bugs fixed

1. Der Beirat deutet den Forschungszustand nicht mehr falsch an.
2. Das Dashboard zeigt bei auf dem Weg befindlichen Flotten wieder die erwartete Ankunftszeit an.
3. Auf dem Weg befindliche Flotten können nun nicht neu formiert werden.
4. In manchen Fällen wurden Operationals nicht aktiv, obwohl genug Personal vorhanden war. Das betrifft Gebäude genauso wie Schiffe.
5. Schiffe in Missionen und in der Reserve werden nun gepunktet.

todo. zoom issue at map
todo. issue in data consistence n frontend
todo. fleet detachment without orbit
todo. detatching a inoperable ship from a fleet doesn't update the fleet as operable
todo. add heat map on colonization
todo. heat must be used system wide not by planet -> user harassing by pirates at hyper limit
todo. chat checken nach falschem empfänger

#### next steps

todo. transportmenge zusammenfassen und bilanz bilden
todo. lock forum threads for admin permission
todo. forum admin area to remove forum_write
todo. login log with timestamp (or better regex?)
todo. exception via mail
todo. research tree issue
todo. forum und chat per handy
todo. async account creation
todo. angular google SEO
todo. fetching mining factors prüfen - erzeugt last
todo. housing left for x ticks - anzeige nach lastring ticks besser aufstellen
todo. songbird is out of scope bei researches, aber baubar -> muss nachvollziehbar sein

heatmap feature:
todo. Die Karte für strategische Operationen ("Mission Map") hat nun eine helle Farbgebung.  
Und das dahinter liegende Feature ist die Heat Map, die ... nun ja, die Hitze anzeigt, die verschiedene
Operationsgebiete akkumulieren.  
Anfangs bleibt sie beschränkt auf Piratenaktivität oder besser, die Angriffslust von Piraten.
todo. heat indicator on map -> -30 to 30 heat scale -> d3 statt svgjs?

todo. take-a-tour updaten

- combat sim section: desgin enemy ship classes, create fleets of them and let them fight in combat theatre

todo. Planeten nach Systemnamen durchnummerieren

- ressourcen müssen besser gebalanced werden
- punkte für missionen
- aktive Spielermissionen, e.g. provoke pirate raid at user or planet (man kann heat für andere kaufen)
- allianz-feature: missionen auf allianzterritorium, z.B. pirate hunt für nen kumpel oder convoyschutz
- unendliche forschungen kennzeichnen
- cache in datenbank verschieben
- ress verschieben per zahleneingabe
- article edit stuff
- menü als vertikaler slider
- minimal intelligence on foreign fleet if visible
- sanitize markdown messages without unsafe message
- bootstrap css nur bei markdown-editor möglich
- bug: ftl möglich bei reserveflotte
- write cache to file on shutdown and read post construct
- eingemottete Schiffe tatsächlich außer dienst nehmen
- normale schlachten auch im mission report anzeigen
- fleet management - flotten aufteilen neben flotten mergen

missionen

- nur reserveschiffe können missionen zugewiesen werden
- jeder plani bekommt heat pro runde
- counter mission reduziert um counter impact, flotten im orbit verbessern die heat nur zur hälfte, frachter im orbit
  sprechen sich rum

- müssen mit schiffen ausgestattet werden
- haben einen task
    - counter mission
        - anti-piraten-missionen (schutz vor passiven piraten im nahfeld)
        - konvoischutz (schutz vor passiven piraten)
    - active mission
        - to be discussed
- haben einen aktionszeitraum
- ist die mission abgeschlossen, kommen die schiffe zurück

aktive piraten

- heatmap pro user, umso höher die punkte im verhältnis zu anderen, umso wahrscheinlicher ein angriff
- ein piratenangriff reduziert heat
- ein anti-piraten-mission reduziert heat
- eine stationierte flotte kann piraten zur schnellen Flucht bewegen
- angegriffen und geraidet wird der am schwächsten verteidigte planet

passive piraten

- jeder trade kann piraten triggern
- erfolgsrate je nach anti-piraten-missionen/konvoi beider teilnehmer und größe des trades pro tick
- trade wird um erfolg des piraten reduziert

Thema: Handel
done. Bestenliste mit Punkten pro Leistungsfeld  
done. Marktplatz

Thema: NPC und Missionen
done. NPC-Konzept für Systeme und systemfreie NPC (Piraten, fliegende Händler ...)  
done. NPC-Handelssystem
done. Konzept für Missionen und NPC-Missionen
todo. interne Sektoraufteilung der Map nach Entfernung um wirtschaftliche Leistungszentren  
todo. aktiver NPC-Handel

Thema: Spionage
todo. Spionagegebäude, pro Level gibt es einen Spion
todo. Spion im Einsatz bringt 50% Genauigkeit bei den Punkten (pro Einsatzfeld?)
todo. Erfahrungslevel trainieren -> verbessert Genauigkeit
todo. Counter-Spionage, Spione umdrehen (unbenutze Spionage-Slots)

----------------- bugs -----------------

----------------- plannings ------------

1. Die Darstellung des Kampfes aufhübschen.
2. Aktuell kann man nur 1on1 kämpfen, das muss aufgebohrt werden
3. Population balancing
4. Rework der Flottenbewegungen (nice to have)
5. mich endlich mal um die "corporate identity" kümmern, also eigene Icons, Symbole, Benennungen usw erzeugen

todo. Forum: Thema sperren
todo. gebäudeoutput beobachten, population balancen
todo. Gebäudegrundkosten zu hoch, Kostensteigerung pro Level zu gering
todo. Usernamen und Namen des Imperiums trennen, Herrschertitel, Navy-Prefix
todo. wurmlochbrücken
todo. population raiden
todo. spot offer hat nur so viel volumen wie die letzten 10 Tage insgesamt gehandelt wurde
todo. warship health states not persisted
todo. reload trade offers and stuff after take one
todo. operationsgebiet auf karte einzeichen -> systeme/planeten erkennen und für mission setzen

todo. kein alter Menüpunkt mehr ausgewählt wenn man sich wieder einloggt
todo. Exception per mail an webmaster (warum funzt das nicht auf prod?)

- look & feel wie in büchern
- emission spectra als gimmick

todo. better distinguish between tick-worker-service and tick-time-service
todo. combat aufhübschen and more infos
todo. fleet movement report im dashboard
todo. Wartung von Schiffen und Flotten -> Abnutzung der Impeller
todo. warship icon instead of fleet shark
todo. library for modules and hulls
todo. provide alliance at account creation to place main planet by friends?
todo. außerdem auf dem dashboard wäre neben dem population development auch das ressourcen development interessant

todo. use context menu for star map?
todo. fleets in motion needs nav point icon
todo. diagramme mit apache echart einbauen, z.B. in Flotten Anzahl nach Klasse o.ä.
todo. ground constructions show non-collectables correctly
todo. switch to "calculate orbit in backend" while travelling

todo. combat with more then 2 opponents
todo. inoperational flotten zerstören bei sieg oder beute?
todo. im bau befindliche flotten während des Baus zerstören
todo. Schiffe erbeuten?

todo. simulierter ziviler Handel - Kosten und Steuereinnahmen

todo. wiki: list of contents (dynamically from markdown headlines)
todo. wiki: images and position

- file size?
- stored in?
  todo. wiki: links between articles?
- every article know their links and pass them by a keyword?

todo. support module zum reduzieren der benötigten crew
todo. researches: display what is the result
todo. sorting für resourcen, gebäudetype
todo. Wurmlöcher
todo. Forumsbeiträge müssen automatisch runter scrollen
todo. leerzeichen im usernamen verbieten
todo. Anzeigenamen für User plus Dienstgrad je nach Nation
todo. google fonts selber hosten / per apache forwarden?

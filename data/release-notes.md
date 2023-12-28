# Event War Harvest Release

todo. fleets not agress each other
todo. create foritied bases with rewards
todo. create player-to-delete bases with rewards
todo. punkte sammeln
todo. imperiumsnamen wählen (-> externe profilseite, link zum chat?), namen in spielerliste zur wiedererkennung, topic: "Char address"
todo. ladderboard

article priorisierung bei pop demand und migration
article orbital modules

- combat theatre
    - battles by conflict and date
    - all battles by date

### Features

#### Gameplay

todo. more speed!
todo. new trade outpost near haven, another near

#### Diverses


#### next steps

---
todo. distance based duration

- all manual transfer stuff takes time
- If pops and resource transfers between your own planets are difficult, you need another option to do it.
  This option is the market place. Then this element become more important.
  And what we need then is a way to let people migrate between the empires. This offers in the end a lot of diplomatic opportunities (migration treaty like in Stellaris e.g.)...

---

---
um bei den Planis besser zu sehen, welche Marktplatz lieferungen demnächst ankommen, schlage ich folgendes vor:  
Beim Marktplatz-tab unten drunter, eine kauf / verkauf historie als tabelle mit Rohstoff, tick gekauft/verkauft, tick geliefert und convoy beschützt oder nicht.  
Ich sehe nämlich manchmal, dass ein plani zu wenig geld hat und will was verkaufen, muss dann aber erstmal ins journal um mich zu erinnern, dass ich das im tick zuvor auch schon
gemacht habe und dann wieder zum plani zurück. das nervt. :D
---

todo. calculate pop deposit in demand calculation for displaying?

todo. manual transfer to fleet overrides capacity
todo. education building shows income at level 0
todo. negative pop development in UI
todo. battlereport new advisory but no new present

todo. chats von anderen werden automatisch gelesen
todo. unload fleet on ship transfer or something

- Raumforts an variablen Positionen?

todo. ally interner marktplatz
todo. mission items more role play "unprotected convoy changes signature of ship to warship and pirate fled ...""
todo. songbird is out of scope bei researches, aber baubar -> muss nachvollziehbar sein

todo. a overview dashboard for fleet. where are they all and which are they designed for

todo. forum management is more important, e.g. sticky posts

todo. schadensprofil von schiffsklassen und flotten darstellen, Bedienmannschaften von Modulen darstellen
todo. look & feel wie in büchern
todo. emission spectra als gimmick
todo. missionen sollten Rumpfmasse inkl Klasse beachten "großer Rumpf und Frachtertriebwerke sind kein Schlachtschiff"
todo. player comment about the game in profile to display at take-a-tour
todo. chat search: empire name, ingame name, username

### important issues

todo. quoting in chat und forum
todo. preview in chat und forum
todo. fleet detachment submit button disable when no change
todo. unlimied reasearch at the tick of new colos
todo. fleet calc hyper limit position in backend - georges error -> numeric overflow?

#### for later

todo. schiffe am markt verkaufen
todo. schiffsbeute machen -> schiffsklasse muss erforscht werden
todo. journal report via mail

todo. durchsuchbare release notes?
todo. battle reports mit anderen teilen

todo. placing new players must be improved -> select cluster, by neighbour usw usw

todo: JobRunr for running async jobs?

todo. expansion list - travel time is always based on home planet -> takes to long at all, fetch them one by one for the displayed items

todo. multiple browser tabs
todo. transportmenge zusammenfassen und bilanz bilden
todo. lock forum threads for admin permission
todo. forum admin area to remove forum_write
todo. exception via mail
todo. angular google SEO
todo. fetching mining factors prüfen - erzeugt last
todo. housing left for x ticks - anzeige nach lasting ticks besser aufstellen
todo. wrong password no second login attempt

?. gebäudelevel deaktivieren (bis auf level 1 für das saubermachen)
?. history for dashboard

- cache cleanup or only load last 3 ticks
- cache in datenbank verschieben - mariadb json pointer useful?

heatmap feature:
todo. Die Karte für strategische Operationen ("Mission Map") hat nun eine helle Farbgebung.  
Und das dahinter liegende Feature ist die Heat Map, die ... nun ja, die Hitze anzeigt, die verschiedene
Operationsgebiete akkumulieren.  
Anfangs bleibt sie beschränkt auf Piratenaktivität oder besser, die Angriffslust von Piraten.
todo. heat indicator on map -> -30 to 30 heat scale -> d3 statt svgjs?

todo. take-a-tour updaten

- combat sim section: desgin enemy ship classes, create fleets of them and let them fight in combat theatre

todo. Planeten nach Systemnamen durchnummerieren

- aktive Spielermissionen, e.g. provoke pirate raid at user or planet (man kann heat für andere kaufen)
- allianz-feature: missionen auf allianzterritorium, z.B. pirate hunt für nen kumpel oder convoyschutz
- ress verschieben per zahleneingabe
- menü als vertikaler slider
- minimal intelligence on foreign fleet if visible
  ?. bootstrap css nur bei markdown-editor möglich
- exploit: ftl möglich bei reserveflotte
- eingemottete Schiffe tatsächlich außer dienst nehmen
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

todo. Forum: Thema sperren
todo. gebäudeoutput beobachten, population balancen
todo. Gebäudegrundkosten zu hoch, Kostensteigerung pro Level zu gering
todo. Usernamen und Namen des Imperiums trennen, Herrschertitel, Navy-Prefix
todo. population raiden
todo. spot offer hat nur so viel volumen wie die letzten 10 Tage insgesamt gehandelt wurde
todo. operationsgebiet auf karte einzeichen -> systeme/planeten erkennen und für mission setzen

todo. Exception per mail an webmaster (warum funzt das nicht auf prod?)

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

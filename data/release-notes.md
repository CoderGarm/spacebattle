### Features

article priorisierung bei pop demand und migration
article orbital modules

- combat theatre
    - battles by conflict and date
    - all battles by date

#### Gameplay

In diesem Release geht es um das Konzept der strategischen Tiefe.

1. Es gibt eine neue Forschung für Gravitationsanomaliedetektoren. Hat man die entsprechenden Level geforscht, kann man die verschiedenen Orbitalmodule bauen.

2. Es gibt einen neuen Typ Gebäude... und auch gleichzeitig ein Raumschiff. Aber ohne Antrieb, wie ein Gebäude halt. Auf jeden Fall wird das später mal ein Raumfort.  
   Jetzt ist es allerdings ein autarker Satellit, der für die Aufklärung der Hypergrenze zuständig ist.  
   Umso stärker die Sensorleistung, umso genauer ist die Auflösung der Hyperabdrücke für einkommende Flotten. Natürlich haben Raumschiffe ebenfalls Gravitationssensoren, Teufel,
   der ganze Antrieb ist ein Grav-Sensor.  
   Beide Effekte interferieren nicht miteinander, aber der stärkere Sensor ist genauer.
3. Damit gibt es nun auch einen Nutzen für die beiden bisher nicht verwendeten Ressourcen Rare Elements und Heavy Metals.

4. Es gibt noch mehr orbitale Module als nur das GADA (fachsprachlich: Gravitationsanomaliedetektorarray), man kann orbitale Habitate bauen.  
   Die Habitate erhöhen den Pop Mining Factor des Planeten und auch den Wohnraum. Indirekt also die Geburtenrate, sozusagen den Pop Faktor. Für alle englisch sprechenden,
   entschuldigt den fremdsprachlichen Wortwitz.  
   Dieser Effekt ist statistisch im **Journal of Orbital Science** beschrieben:
   > Orbitin ist eine hypothetische Substanz, die in den äußeren Schichten von Orbiten vorkommt und sich mit der Zeit in orbitalen Habitaten ansammelt.  
   > Diese Substanz wird durch die Wechselwirkungen zwischen der Atmosphäre des Planeten und den orbitalen Strukturen erzeugt.  
   > Die Entdeckung von Orbitin erfolgte durch Raumsonden, die speziell dafür konzipiert wurden, Proben aus orbitalen Habitaten zu sammeln und zu analysieren.  
   > Die Forschungsergebnisse, veröffentlicht im fiktiven "Journal of Orbital Science" (Band 42, Ausgabe 3, Seiten 245-259), legen nahe, dass Orbitin eine einzigartige Rolle bei
   der Erhöhung der Lebensqualität auf dem Planeten spielt.  
   > Die Substanz interagiert positiv mit biologischen Systemen und fördert das Wohlbefinden von Lebewesen, indem sie den Stoffwechsel verbessert, den Stressabbau unterstützt und
   die kognitiven Funktionen stimuliert.  
   > Die Quelle von Orbitin in orbitalen Habitaten könnte eine mögliche Erklärung für das allgemeine Wohlbefinden von Menschen und anderen Lebewesen in diesen Strukturen sein.  
   > Darüber hinaus könnte die gezielte Freisetzung von Orbitin in die planetare Atmosphäre positive Effekte auf die Gesundheit der Bewohner haben.
   >
   > -- <cite>Δημιουργικό Προ-εκπαιδευμένος Μετασχηματιστής</cite>

   Abschließend ist zu sagen, dass diese Strukturen momentan nicht zerstört werden können. Da es sich gerade bei den Habitaten um zivile Einrichtungen mit besonderem
   Schutzbedürfnis handelt, sind vorher Schutzmöglichkeiten notwendig. Natürlich auch ein Flottenbefehl, zivile Einrichtungen nicht anzugreifen. Aber das folgt später.

5. Das bedeutet in letzter Konsequenz, dass die **Genauigkeit in der Hyperabdruckauswertung** vollkommen vom Eloka-Wert des Systems abhängig ist.  
   Damit sind Wachschiffe im System von überragender aufklärerischer Bedeutung - egal ob sie auf Missionen sind oder sich im Orbit befinden. Alle eigenen Schiffe und orbitale
   Module im System beeinflussen die systemweite Aufklärung.

   **Gibt es keine Ortung, gibt es keine Aufklärung. *Gar keine!***

   Die neue Mechanik hat zwei Ebenen und eine strukturelle Komplexität zur Auswertung von Hyperabdrücken:
    - Höchste Eloka-Punkte im System / 10 (aufgerundet) = Anzahl der individuell auflösbaren Schiffe
    - Höchste Eloka-Punkte im System * Kilotonne = auflösbare individuelle Tonnage
    - Orbitale Sensorarrays sind so aufgebaut, dass einzelne Module kombiniert werden können. Das kennen wir z.B. vom VLA (very large array) Radio Telescope aus New Mexico von
      Alterde. Deswegen werden die Sensorpunkte von Arrays addiert - im Gegensatz zu Schiffssystemen, die in sich abgeschlossen agieren und bei denen immer nur der genaueste Sensor
      Informationen liefert.

   Ein Beispiel:  
   Das CA-Electronic Warfare-Modul hat 150 Eloka-Punkte und kann damit die Tonnage von Schiffe bis zu 150 Kilotonnen ungefähr einschätzen und 15 Punktquellen individuell
   auflösen.  
   Befindet sch ein orbitales Sensorarray mit 1000 Punkten im System, gilt ist natürlich die Bestimmung des Sensorarrays genauer und damit werden die Sensorwerte des schweren
   Kreuzers aus dem Beispiel ignoriert.

6. Ein kleiner, aber wichtiger Change im Job-Modul: Die Konstruktionspunkte werden direkt beim Start eines Jobs abgezogen und auf die Konstruktionsleistung verrechnet.  
   Auf absehbare Zeit bekommt man die Punkte nicht zurück, wenn man einen Job abbricht, die Ressourcen werden allerdings erstattet.

todo. more npc, silesia usw

todo. distance based duration

- all manual transfer stuff takes time
- If pops and resource transfers between your own planets are difficult, you need another option to do it.
  This option is the market place. Then this element become more important.
  And what we need then is a way to let people migrate between the empires. This offers in the end a lot of diplomatic opportunities (migration treaty like in Stellaris e.g.)...

- Raumforts an variablen Positionen?

#### Diverses

1. Der Tech Tree ist in der Library zu sehen, dann kann man sich dort einen Überblick verschaffen, welche Forschungen für die Module notwendig sind.
2. Die Bibliothek und der Tech Tree enthalten für Schiffsmodule und die neuen orbitalen Strukturen jetzt alle relevanten Informationen. Die Schiffswerft ist momentan unverändert,
   allerdings haben die Module zwecks besserem Überblick sprechende Namen bekommen.
3. Es gibt Hinweise zum Beschützen von Konvois direkt am Handelsplatz.
4. Die Silesianische Konföderation ist nun Handelspartner für Spot Offers in ihrem Sektor.  
   Auch die Asgard Association, die Midgard Federation, der Handelsbund von Rembrandt und die neu gegründete Meroa Trading Association bieten ihre Dienst an. Damit sollte der Spot
   Markt an jedem Punkt der Galaxie verlässliche Reisezeiten für die Handelskonvois anbieten.

todo. manual transfer to fleet overrides capacity

todo. highlight searched star
todo. star label at map clickable

todo. imperiumsnamen wählen (-> externe profilseite, link zum chat?), namen in spielerliste zur wiedererkennung, topic: "Char address"
todo. chat search: empire name, ingame name, username

### Bugs fixed

Corben1
03.12.2023 21:34
Ich gehe auf "Kolonisieren" -> "Kolonisierung planen" -> dann kommt die Fehlermeldung

1. Waffen haben nun die korrekte Tonnage und den korrekten Kampfwert.
2. Tooltip bei den Schiffswerten des Battle Reports ist unterdrückt.
3. Forschungspunkte werden nun hoffentlich korrekt berechnet.
4. Man kann keine Jobs in der Werft mehr starten, wenn die Werft nicht wenigstens auf Level 1 aktiv ist.
   todo. migration must respect pop demand on own planet and not exceed the others planet needs

todo. every instajob must be activated if possible
todo. education building shows income at level 0
todo. negative pop development in UI
todo. battlereport new advisory but no new present

todo. chats von anderen werden automatisch gelesen
todo. unload fleet on ship transfer or something

todo. display module properties like barrels, laser pd, autocannon pd ...

#### next steps

todo. ally interner marktplatz
todo. mission items more role play "unprotected convoy changes signature of ship to warship and pirate fled ...""
todo. songbird is out of scope bei researches, aber baubar -> muss nachvollziehbar sein

todo. a overview dashboard for fleet. where are they all and which are they designed for

todo. star map filter für flotten

- eigene inop
- fremde inop
- moves ...
- Sensorreichweite?

todo. forum management

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
todo. wurmlochbrücken nutzbar machen vor nächster season!

#### for later

todo. schiffe am markt verkaufen
todo. journal report via mail

todo. set nav marker by "next stations" dialog?

nope. zu weiter/schneller zoom removed flotten aus map
nope. zoom issue at map -> warten auf d3 replacement
nope. fleetsharks at zoom and move -> warten auf d3 replacement

todo. durchsuchbare release notes?
todo. battle reports mit anderen teilen

todo. placing new players must be improved -> select cluster, by neighbour usw usw

todo. async account creation
todo: JobRunr for running async jobs?

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
3. Population balancing
4. Rework der Flottenbewegungen (nice to have)
5. mich endlich mal um die "corporate identity" kümmern, also eigene Icons, Symbole, Benennungen usw erzeugen

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

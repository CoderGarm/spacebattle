### Features

#### Gameplay

#### Diverses

1. Auf den Einsatz wartende Operationals sind nun im Dashboard sichtbar.
todo. retire fleet button, active ships back to pool fleet
todo. crew display: show needed peoples
todo. Reisezeit beim Handel inkl Tabellendarstellung
todo. gebäudedarstellung für bau verbessern

### Bugs fixed

todo. check fleet separation
todo. article edit stuff

#### next steps

- minimal intelligence on foreign fleet if visible
- sanitize markdown messages without unsafe message
- bootstrap css nur bei markdown-editor möglich
- bug: ftl möglich bei reserveflotte
- write cache to file on shutdown and read post construct
- eingemottete Schiffe tatsächlich außer dienst nehmen
- normale schlachten auch im mission report anzeigen
- fleet management - flotten aufteilen neben flotten mergen
- todo. convoy raid mission
- todo. punkte für missionen
- todo. aktive Spielermissionen, e.g. provoke pirate raid at user or planet
- todo. heat indicator on map

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

todo. use context menu for star map?

todo. wormholes (see mapdata.js file)

todo. fleets in motion needs nav point icon

todo. diagramme mit apache echart einbauen, z.B. in Flotten Anzahl nach Klasse o.ä.

todo. ground constructions show non-collectables correctly

todo. population balancing broken

todo. scroll issue if the last page forbids overflow

todo. switch to "calculate orbit in backend" while travelling

todo. combat with more then 2 opponents
todo. inoperational flotten zerstören bei sieg oder beute?
todo. im bau befindliche flotten während des Baus zerstören
todo. Schiffe erbeuten?

todo. Handelssystem
todo. simulierter ziviler Handel - Kosten und Steuereinnahmen
todo. Pläne von wo nach wo (Lieferzeit) festlegen
todo. piraten plus Flotten zur Systemsicherung nutzen
todo. anti-piraterie-mission schützt vor Diebstahl aus Orbit

todo. wiki: list of contents (dynamically from markdown headlines)
todo. wiki: images and position

- file size?
- stored in?
  todo. wiki: links between articles?
- every article know their links and pass them by a keyword?

todo. support module zum reduzieren der benötigten crew

todo. researches: display what is the result

todo. state that used images are borrowed by the icon page

todo. synchronize enums in frontend
todo. unify getLink for icons
todo. unify mat-icons for purposes

todo. sorting für resourcen, gebäudetype

todo. Wurmlöcher

todo. Forumsbeiträge müssen automatisch runter scrollen

todo. leerzeichen im usernamen verbieten
todo. Anzeigenamen für User plus Dienstgrad je nach Nation

map:
todo. look at the credits
todo. take the style

todo. google fonts selber hosten / per apache forwarden?

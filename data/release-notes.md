### Features

#### Gameplay

1. Man kann seine Konvois nun per Mission schützen.
   Es gibt auch ein kurzes Tutorial dazu!
   Nachteil: Man muss seine Konvois mit Missionen schützen.
2. Die Piraten greifen sich nun auch Konvois.
   Sofern man seine Systeme nicht ausreichend schützt und die Piraten mit ihren Angriffen Erfolg haben, werden die
   Angriffe an der Hypergrenze häufiger.
3. Die Anforderungen an das Level von Antrieben ist deutlich gesenkt.  
   Damit kommt man schneller an sinnvolle Reisezeiten zu seinen Nachbarn. Und bei aller Liebe, da liegen wirklich zu
   viele Ressourcen auf den Hauptplaneten rum!  
   Das ist der Workaround, bis es einen Handel für Schiffsteile gibt.
4. Handelsangebote haben eine entfernungsabhängige Reisezeit.  
   Da sich die Reisezeiten von regulären Konvois in Monaten bemisst, ist nur hervorragende Organisation geneigt, die
   Bedürfnisse heutiger Imperatoren und Präsidenten zu befriedigen. Das geht dann alles etwas flotter.  
   Gleichzeitig wird erstmals das Konzept der Gleichzeitigkeit betrachtet. Da Ereignisse sehr weit weg passieren, wird
   man auf Informationen warten müssen. Falls dein Konvoi überfallen wird, wirst du es erst mitbekommen, wenn er
   eintrifft.  
   Für den Spot Market gilt, dass der dichteste Handelsplatz gewählt wird.

#### Diverses

1. Die Dashboard-Informationen werden ab jetzt bei allen Themen einen Neustart überleben.
2. Die Reihenfolge der Tabs bei StratOps ist getauscht. Damit muss man die Karte nur laden, wenn man die Karte auch
   braucht.
3. Die Forschungen lassen sich jetzt durchsuchen.

### Bugs fixed

1. Man kann einer Mission mehrere Schiffe zuweisen.
2. Man kann nun Forschungen tatsächlich auf das höchste Level erforschen.
3. Mit jedem Mal, dass man die Sternenkarte geladen hat, hat es länger gedauert. Das isst jetzt nicht mehr so.
4. Man kann seine Handelsangebote jetzt zurückziehen.
5. Das Problem mit dem Verifikationslink für die eMail-Adresse ist behoben.
6. Die Migration innerhalb des eigenen Imperiums ist jetzt abhängig von der Menge an verfügbaren Wohnraum.
7. Man kann geplante Kolonisierungen nun abbrechen. Wenn das Kolo-Schiff schon losgeflogen ist, ist es allerdings zu spät dafür.

#### next steps

todo. fetching mining factors prüfen - erzeugt last
todo. housing left for x ticks - calc prüfen
todo. tooltip overlay in construction ressis
todo. Das teilen der Flotten geht immer nur einmal. Danach muss man die seite neu laden, damit der teilen button wieder enabled wird.

heatmap feature:
todo. Die Karte für strategische Operationen ("Mission Map") hat nun eine helle Farbgebung.  
Und das dahinter liegende Feature ist die Heat Map, die ... nun ja, die Hitze anzeigt, die verschiedene
Operationsgebiete akkumulieren.  
Anfangs bleibt sie beschränkt auf Piratenaktivität oder besser, die Angriffslust von Piraten.
todo. heat indicator on map -> -30 to 30 heat scale -> d3 statt svgjs?

todo. take-a-tour updaten
todo. in job-dashboard immer alle planeten aufführen + filter
todo. transportaufträge mit "immer nur einmal"-schalter

- combat sim section: desgin enemy ship classes, create fleets of them and let them fight in combat theatre

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

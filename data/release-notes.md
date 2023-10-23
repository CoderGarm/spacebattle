### Features

#### Gameplay

todo. placing new players must be improved
todo. heat mechanik muss für ganze systeme reduzierbar sein -> "strahlt aus"

1. Aktive und inaktive Kriegsschiffe in der Reserve und Flotten
    - Für Kriegsschiffe die abgewrackt werden, bekommt man jetzt 50 % der Ressourcen zurück. Die Crew wird natürlich nicht dezimiert.
    - Man kann Kriegsschiffe nun per Button direkt in die Reserve schicken.
    - Man kann Schiffe nur auf Planeten mit Schiffswerften verschrotten oder einmotten. Dort lagern eingemottete Schiffe bis zur Wiederverwendung.
    - Schiffe in der Reserve haben keine aktive Crew mehr, die befindet sich auf dem Planeten, in dessen Orbit das Schiff eingemottet wurde.
    - Schiffe können im Detachment Tab entmannt werden. Verzeiht den Wortwitz. Dort kann man sie, wenn genug Besatzung auf dem Planeten steht, direkt wieder aktivieren.
    - Nur aktive Schiffe der Reserve können Missionen zugeteilt werden.
    - Es gibt ein kurzes Tutorial dazu.
2. Angriffe auf Handelskonvois passieren nun auch, wenn eine der großen Mächte im Spiel ist. Allerdings schützen sie ihren Teil des Konvois mit ausreichender Kapazität.

todo. Gebäudeoutput muss deutlicher steigen
todo. tech level 2 und 3 nutzen
todo. Hyperabdruck im System bei aufkommenden Flotten

- Emissionssignatur verrät ungefähre größe und anzahl (siehe "look & feel wie in büchern")
- zwei gebäude (planetare antennen, orbitale antennen) und Eloka-Modul auf Schiffen erzeugen scan-punkte und verraten genauigkeit der ortung

#### Diverses

1. Es gibt jetzt individuelle Icons für die verschiedenen Stati von Flotten und Schiffen.
2. Das Menü ist etwas ansprechender gestaltet, insbesondere enthält die Flottenauswahl nun mehr direkt sichtbare Informationen über Zusammensetzung und Status der Flotten.
3. The map background is dark again.
4. Man kann nun Tutorialbeiträge im Wiki schreiben, die dann an den entsprechenden Tutorials angezeigt werden.  
   Bitte sagt mir, wo am dringendsten Beiträge gebraucht werden. Das kann prinzipiell jeder User machen, der im Wiki editieren möchte.
5. In den Gebäudebau wird nun der aktuell laufende Job vorausgewählt, damit man nicht mehr im Dashboard nachgucken muss.
6. Ebenfalls werden nun Baubeschränkungen durch Preis oder Forschung direkt angezeigt. Das sollte einige unnötige Klickerei ersparen.

todo. mission items more role play "unprotected convoy changes signature of ship to warship and pirate fled ...""
todo. quoting in chat und forum
todo. songbird is out of scope bei researches, aber baubar -> muss nachvollziehbar sein

todo. forum management
todo. light background color more pale?
todo. check fitting yard


todo. star map filter für flotten

- eigene inop
- fremde inop
- moves ...

todo. schadensprofil von schiffsklassen und flotten darstellen
todo. look & feel wie in büchern
todo. emission spectra als gimmick
todo. missionen sollten Rumpfmasse inkl Klasse beachten "großer Rumpf und Frachtertriebwerke sind kein Schlachtschiff"
todo. player comment about the game in profile to display at take-a-tour

### Bugs fixed

1. Der Pop Demand zeigt nun auch den Bedarf von Flotten im Umbau oder in Reparatur an.
2. Das planetare Jobdisplay zeigt nun nur noch den aktuell laufenden Job an, nicht mehr die ganze Queue.
   todo. upgrade/repair job muss dings einklappen und neu laden
   todo. 10-points-problem das leftover

todo. wrong password no second login attempt
todo. battle reports notification wrong
todo. fleet moves werden falsch angezeigt -> isDeletable auf entity und nicht mehr aus cache lesen
todo. spontan leere flotte muss von der map verschwinden

todo. priorisierung bei pop demand und migration problematisch?
todo. population problem bei Samovar - yufiel -> mind aber ein update-problem

Nachrichten:

- Das zeichen für neue nachrichten verschwindet nicht mehr
- Ich habe keinen 'neue nachrichten marker' für den neuen 'battle report' bekommen
- Laut meiner anzeige sind 2 Flotten von dir heute beim hypelimit aufgetaut. Tatsächlich ist eine da, aber die andere bereits beim plani.

#### next steps

todo. jobs due-date nicht auf basis von ticks fest legen sondern die punkte werden runter gerechnet -> ticks left ist immer eine berechnung (mehrere jobs pro tick möglich)
todo. zoom issue at map
todo. fleetsharks at zoom and move
todo. durchsuchbare release notes?

todo. battle reports mit anderen teilen
todo. multiple browser tabs
todo. transportmenge zusammenfassen und bilanz bilden
todo. lock forum threads for admin permission
todo. forum admin area to remove forum_write
todo. exception via mail
todo. async account creation
todo. angular google SEO
todo. fetching mining factors prüfen - erzeugt last
todo. housing left for x ticks - anzeige nach lastring ticks besser aufstellen

?. gebäudelevel deaktivieren (bis auf level 1 für das saubermachen)
?. history for dashboard

todo. fetch eager auf constructions weg vom planet

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
todo. wurmlochbrücken
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

### Änderungen in der Bevölkerungsentwicklung

1. Die Berechnung zur Bevölkerungsentwicklung betrachtet nur noch die Bevölkerung im reproduktionsfähigen Alter und mit entsprechend dem Vorgang notwendigen Organen.  
   Das hat zur Folge, dass die Menge an vorhandener Bevölkerung den größten Einfluss - neben dem Wohnraum - auf die Bevölkerungsentwicklung hat.  
   Jede Altersstufe - ich impliziere ein gewisses Alter pro Education Level, obwohl das spieltechnisch etwas weit hergeholt ist - besitzt eine "Neigung" zur Reproduktion,
   sozusagen:
    - SCHOOL: 0.1
    - COLLEGE: 1
    - UNIVERSITY: 1
    - ENLISTED: 0.4
    - OFFICER: 0.3

   Diese Koeffizienten bestimmen die Menge an reproduktionsbereiten Individuen.  
   → 100 College (oder University) resultieren also in 50 reproduktionsbereiten Leuten mit entsprechenden biologischen Anlagen: 100 Leute · 1 = 100 Leute, 100 / 2 = 50 Leute
   → 100 Enlisted resultieren in 20 Leuten: 100 · 0,4 = 40, 40 / 2 = 20

   Dazu wird es nach dem nächsten Release Wiki-Artikel im Tutorial geben (ist noch buggy).
2. Die vorhandene Bevölkerung im Zustand "frisch geboren" wurde angepasst.  
   Das bisherige Bevölkerungswachstum hat dafür gesort, dass es etwa 4,5 Millionen Neugeborene im Universum gab, bei etwa 360 k Erwachsenen.  
   Diese Verteilung passt tatsächlich sehr gut zu der verwendeten Formel zum Bevölkerungswachstum (Stichwort: r-Strategen), ist aber für Menschen genauso ungeeignet wir für BfH.  
   Die Menge an Neugeborenen wurde auf die Gesamtmenge der sonstigen Bevölkerung reduziert.  
   Diese Änderung ist auf Planeten mit mehr als 1000 Neugeborenen beschränkt.
3. Dadurch haben sich auch Änderungen für neue Accounts ergeben, die im Advisory Board berücksichtigt sind.  
   Es gibt neue Vorschläge, je nachdem wie weit man im Spiel ist.

Das Ergebnis der Punkte 1 und 2 wird sein, dass eine ganze Weile kein Bevölkerungswachstum stattfindet, also bitte wundert euch nicht darüber.  
Die Idee dieses Lösungsansatzes ist es, freien Wohnraum dauerhaft zu ermöglichen, der nicht sofort belegt wird. Damit gewinnt das Thema Migration und Bildung eine größere
Bedeutung - und ganz wichtig: Die Probleme bzw Deadlocks sollten damit alle beseitigt sein.

Und etwas fürs Auge gabs auch noch ;)

----> wiki artikel nach release

### Features

article trading protection -> protecting-trades.md
article priorisierung bei pop demand und migration
article + check ship returns from mission
article für Bevölkerungsentwicklung

#### Gameplay

1. Die Änderungen in der Bevölkerungsentwicklung aus dem "Zwischenrelease" erwähne ich hier der Form halber:  
   Die Berechnung zur Bevölkerungsentwicklung betrachtet nur noch die Bevölkerung im reproduktionsfähigen Zustand.  
   Durch die anderen Änderungen wurde es wichtig, deutlich früher im Gameplay bereits die Bildungseinrichtungen zu leveln. Dafür gibt es Advices im Advisory Board.
2. Operation Sonnenblume war erfolgreich, die Großmächte haben die meisten Basen ausgeräuchert. Aber eben nur die Basen, nicht alle Piraten wurden erwischt.  
   Piraten spawnen nicht mehr für Raids auf nicht kolonialisierte Planeten.  
   Ursprünglich war die Idee dahinter, dass das Universum etwas lebendiger wirkt, weil man auch mal zwischendurch auf fremde Flotten treffen kann. Das Ergebnis war allerdings, dass
   in einigen Systemen zwei, drei oder vier Piratenflotten gespawnt sind und das irritiert mehr, als dass es lebendig wirkt.  
   Die Kersey Association wird euch also ihre ganze Aufmerksamkeit widmen ;)
3. Neue Schiffe landen nun direkt in der Reserve und nicht in einer neuen Flotte. Im Kriegsfall werden unnötige Verluste sowie die Schadenfreude des Gegners vermieden.
4. Schiffe der Reserve können nun im Transport-Menü direkt zwischen eigenen Planeten transportiert werden. Das ganze passiert auf die gleiche magische Weise wie bei den Ressourcen.

todo. tech level 2 und 3 nutzen
todo. Hyperabdruck im System bei aufkommenden Flotten

- Emissionssignatur verrät ungefähre größe und anzahl (siehe "look & feel wie in büchern")
- zwei gebäude (planetare antennen, orbitale antennen) und Eloka-Modul auf Schiffen erzeugen scan-punkte und verraten genauigkeit der ortung

- jeder sektor (? definiere) bekommt seinen eigenen piraten, der Unsinn anstellt

#### Diverses

1. Der Zustand von Schiffen im Transfer-Display der Sternenkarte wird per Farbkodierung "wie gewohnt" dargestellt.
2. Es gibt eine Bibliothek aller Schiffskomponenten und Gebäude im Burgermenü.  
   Die Bib ist, wie üblich, in Entwicklung und es muss noch klar werden, welche Informationen dort sinnvoll sind.

todo. manual transfer to fleet overrides capacity
todo. imperiumsnamen wählen (-> externe profilseite, link zum chat?), namen in spielerliste zur wiedererkennung, topic: "Char address"
todo. chat search: empire name, ingame name, username

todo. mission items more role play "unprotected convoy changes signature of ship to warship and pirate fled ...""
todo. songbird is out of scope bei researches, aber baubar -> muss nachvollziehbar sein

todo. check fitting yard

### Bugs fixed

1. Die planetaren Ressourcen werden beim Flottenwechsel im Flottenmenü aktualisiert.
2. Der Status einer Flotte wird nun bei jeder Änderung der Formation neu bestimmt.
3. Flottenbewegungen wurden im Journal falsch angezeigt, ob eine Flotte ins System gesprungen oder den Planeten angeflogen hat, wurde nicht unterschieden. Das ist repariert.
4. Schiffswerften ohne Auftrag werden wieder im Journal angezeigt.

todo. fleet detachment submit button disable when no change
todo. research punkte -> sandkiste issue with 27 points left vs 160 from inop labs

todo. keine jobs ohne aktiviertes gebäude (werft) -> division / 0 in calcRemainingTicks
todo. negative pop development in UI
todo. battlereport new advisory but no new present
todo. no running job in journal
todo. migration must respect pop demand

todo. wiki is broken -> store full text as easy solution?

todo. chats von anderen werden automatisch gelesen
todo. fix crew exploit
todo. unload fleet on ship transfer or something

#### next steps

todo. researches müssen teurer sein
todo. forum management

todo. star map filter für flotten

- eigene inop
- fremde inop
- moves ...

todo. schadensprofil von schiffsklassen und flotten darstellen, Bedienmannschaften von Modulen darstellen
todo. look & feel wie in büchern
todo. emission spectra als gimmick
todo. missionen sollten Rumpfmasse inkl Klasse beachten "großer Rumpf und Frachtertriebwerke sind kein Schlachtschiff"
todo. player comment about the game in profile to display at take-a-tour
todo. chat search: empire name, ingame name, username

### Bugs fixed

todo. construction issue?

todo. fleet moves werden falsch angezeigt -> isDeletable auf entity und nicht mehr aus cache lesen -> kein Unterschied zw ankunft und abflug
-> Laut meiner anzeige sind 2 Flotten von dir heute beim hypelimit aufgetaut. Tatsächlich ist eine da, aber die andere bereits beim plani.

todo. quoting in chat und forum
todo. preview in chat und forum
todo. wiki is broken -> store full text as easy solution?

todo. battle reports notification wrong
-> Ich habe keinen 'neue nachrichten marker' für den neuen 'battle report' bekommen

#### next steps

todo. increase performance by grpc calls over multiple rest calls?

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

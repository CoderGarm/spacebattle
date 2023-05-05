----------------- Features -------------  
Thema: NPC, Handel und Missionen

Alles, was unter diesem Thema kommt, ist ein Schritt zu einer lebenden Galaxie.

1. Das Antriebsmodell entspricht jetzt dem Honorverse.
   - https://honor-harrington.fandom.com/de/wiki/Impellerantrieb
   - https://honor-harrington.fandom.com/de/wiki/Warshawski-Segel

2. Das Konzept von Jagd- und Breitseitenbewaffnung wurde geändert.
   - Es bleibt (vorerst) dabei, dass Bug- und Heck-Bewaffnung manuell gefittet werden muss.
   - Es wird nicht mehr zwischen Breitseiten- und Jagdwaffen unterschieden - ein Waffensystem ist universell benutzbar.
   - Eine Breitseitenwaffe wird immer zweimal gefittet - eine pro Breitseite.
3. Die Schiffshüllen wurden entfernt.  
   Ganz nach dem Rollenverständnis der RMN folgen Schiffstypen einer Funktion und nicht einer Klassifizierung nach
   Masse.
   - Die einzelnen Module sind weiterhin einer Klasse als Sortier- und Vergleichskriterium zugeordnet.
   - Das Fitting einer Schiffsklasse entscheidet darüber, wie viel Tonnage das Schiff im Ganzen besitzt.  
     Das beinhaltet den physikalischen Zwang, dass in den Bug- und Hecksektionen nur ein gewisser Prozentsatz des
     Schiffsvolumens und der Schiffsmasse zur Verfügung steht.
      - https://honor-harrington.fandom.com/de/wiki/Hammerkopf
4. Berechnung der Tonnage
   - Siehe oben, die Gesamtmasse eines Schiffes definiert sich über die Anforderungen des Impellerantriebs.  
     Damit ergibt sich spielerisch das Konzept der toten Tonnage. Massetechnisch asymmetrisch gefittete Hammerköpfe
     benötigen eine identische Masseverteilung, dass bedeutet,
     dass der "leere" Hammerkopf mit Superstruktur aufgefüllt wird, um den physikalischen Anforderungen zu
     entsprechen.  
     Analoges gilt für schwere Hammerköpfe und eine gewissermaßen leere Breitseite. Dann muss die Breitseite mit bzw.
     der Modulraum des Schiffes mit inaktiver Masse gefüllt
     werden.

Gameplay:

1. Auf der StarMap verändert sich der Cursor, wenn man etwas anklicken kann.
2. Gebäude und Module mit dem Ziel überarbeitet, es insgesamt etwas "runder" zu machen.
   - Es gibt jetzt z.B. Fracht- und Passagiermodule.
   - Die Supportmodule wurden zunächst entfernt, kommen aber wieder.
3. Der TechTree folgt nun einem levelbasierten Konzept. Das jeweils nächste Gebäudelevel muss frei geforscht werden.
4. Es gibt eine Übersicht aller Imperien inklusive eines einfachen Punktesystems.

Diverses:

1. Es gibt eine interaktive Karte als IFrame, die über https://www.battleforhonor.de/external-star-map-manager oder über
   das Burger-Menü erreicht werden kann.  
   Über den Manager kann man Kartenausschnitte erzeugen und per URL bookmarken.  
   Es wird auch ein IFrame angeboten, dass man auf anderen Webseiten einbinden kann.
2. Ein Radien-Tool ermöglicht es, die groben Koordinaten von unbekannt gelegenen Systemen herauszufinden.  
   Dabei sei erwähnt, dass die Entfernung in der Romanreihe alle mit sehr viel kreativer Freiheit verwendet werden.
3. Im Profil kann man entscheiden, ob man Mails über neue Releases bekommen möchte (Opt-In) und man sieht den Status der
   eigenen eMail-Adresse.

----------------- Bugs fixed -----------

1. Der Bau-Button in der Werft war fälschlicherweise disabled.
2. Im Login-Feld wurde das Passwort aufgedeckt, wenn man Enter drückt.

----------------- next steps -----------

Thema: NPC, Handel und Missionen

done. Bestenliste mit Punkten pro Leistungsfeld  
todo. Marktplatz und NPC-Handelssystem  
todo. interne Sektoraufteilung der Map nach Entfernung um wirtschaftliche Leistungszentren  
todo. NPC-Konzept für Systeme und systemfreie NPC (Piraten, fliegende Händler ...)  
todo. Konzept für Missionen und NPC-Missionen

todo. implement freight and passenger modules, check "unload the own crew"-problem
todo. balance kosten vs nutzen bei modulen
todo. schiffstypen nachbauen und daraus die Werte ableiten, um Sinnhaftigkeit zu prüfen

----------------- bugs -----------------

----------------- plannings ------------

1. Die Darstellung des Kampfes aufhübschen.
2. Aktuell kann man nur 1on1 kämpfen, das muss aufgebohrt werden
3. Population balancing
5. Das man Kolonisierungen planen kann
6. Rework der Flottenbewegungen (nice to have)
7. mich den spannenden Aufgaben widmen: ein NPC-System, Missionen, simulierte Handelsflotten, Piraten und sowas (
   multi-combat wäre wohl sehr sinnvoll -> Darstellung des Kampfes
   muss erkennbaren Mehrwert bringen)
8. Änderungen in der Spielmechanik, z.B. dass die Leistung des Antriebs im Verhältnis zur Schiffsmasse steht -> erneutes
   Rebalancing der Module
9. mich endlich mal um die "corporate identity" kümmern, also eigene Icons, Symbole, Benennungen usw erzeugen

todo. kein alter Menüpunkt mehr ausgewählt wenn man sich wieder einloggt
todo. Exception per mail an webmaster
todo. Userliste
todo. restart backend

- look & feel wie in büchern
- emission spectra als gimmick

todo. combat aufhübschen and more infos

todo. fleet movement report im dashboard

todo. Wartung von Schiffen und Flotten -> Abnutzung der Impeller

todo. warship icon instead of fleet shark

todo. library for modules and hulls

todo. provide alliance at account creation to place main planet by friends?

todo. use context menu for star map?

todo. spinner abbrechen

todo. wormholes (see mapdata.js file)

todo. fleets in motion needs nav point icon

todo. diagramme mit apache echart einbauen, z.B. in Flotten Anzahl nach Klasse o.ä.

todo. population balancing broken

todo. scroll issue if the last page forbids overflow

todo. switch to "calculate orbit in backend" while travelling

todo. combat with more then 2 opponents
todo. inoperational flotten zerstören bei sieg oder beute?
todo. im bau befindliche flotten während des Baus zerstören
todo. Schiffe erbeuten?

todo. kolonisierungen müssen demand erzeugen

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


DELIMITER |
CREATE PROCEDURE insertGermanTranslations()
BEGIN
    DECLARE n INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    SELECT COUNT(*) FROM translation INTO n;
    SET i = 0;
    WHILE i < n
        DO
            select t.idTranslation, t.translation from translation t order by t.idTranslation offset i rows fetch next 1 row only into @idTranslation, @translation;

            #@formatter:off
            if @translation ='Rocket Ammunition' then
                select 'Raketenmunition' into @german;
            elseif @translation = 'Counter Rocket Ammunition' then
                select 'Gegenraketenmunition' into @german;
            elseif @translation =        'A bunch of rockets.' then
                select 'Ein Haufen Raketen.' into @german;
            elseif @translation =        'Another bunch of rockets.' then
                select 'Ein weiterer Haufen Raketen.' into @german;
            elseif @translation =        'Armor Mk I' then
                select 'Rüstung Mk I' into @german;
            elseif @translation =        'An armor' then
                select 'Eine Rüstung' into @german;
            elseif @translation =        'Construction Yard' then
                select 'Bauhof' into @german;
            elseif @translation =        'Orbitals Construction Yard' then
                select 'Orbital Bauhof' into @german;
            elseif @translation =        'Research Laboratories' then
                select 'Forschungslabore' into @german;
            elseif @translation =        'Market place' then
                select 'Marktplatz' into @german;
            elseif @translation =        'Metal works' then
                select 'Metallbearbeitung' into @german;
            elseif @translation =        'Special orbital ores' then
                select 'Spezielle orbitale Erze' into @german;
            elseif @translation =        'Asynchronous Investigations' then
                select 'Asynchrone Ermittlungen' into @german;
            elseif @translation =        'Living room' then
                select 'Wohnzimmer' into @german;
            elseif @translation =        'Hospital' then
                select 'Krankenhaus' into @german;
            elseif @translation =        'Elementary schools' then
                select 'Grundschulen' into @german;
            elseif @translation =        'Secondary schools' then
                select 'Weiterführende Schulen' into @german;
            elseif @translation =        'University' then
                select 'Universität' into @german;
            elseif @translation =        'Teams Rank School' then
                select 'Mannschaftsschule' into @german;
            elseif @translation =        'Military Academy' then
                select 'Militärakademie' into @german;
            elseif @translation =        'The construction yard construct constructions.' then
                select 'Der Bauhof baut Bauwerke.' into @german;
            elseif @translation =        'The construction yard construct orbital constructions.' then
                select 'Der Bauhof baut orbitale Konstruktionen.' into @german;
            elseif @translation =        'The lab investigates researches.' then
                select 'Das Labor untersucht Forschungen.' into @german;
            elseif @translation =        'The market makes money.' then
                select 'Der Markt verdient Geld.' into @german;
            elseif @translation =        'Metals for progress.' then
                select 'Metalle für den Fortschritt.' into @german;
            elseif @translation =        'Heavier metals for more progress.' then
                select 'Schwerere Metalle für mehr Fortschritt.' into @german;
            elseif @translation =        'Rare elements for the future.' then
                select 'Seltene Elemente für die Zukunft.' into @german;
            elseif @translation =        'Everyone needs a home' then
                select 'Jeder braucht ein Zuhause' into @german;
            elseif @translation =        'Everyone needs a doctor' then
                select 'Jeder braucht einen Arzt' into @german;
            elseif @translation =        'a school' then
                select 'eine Schule' into @german;
            elseif @translation =        'another school' then
                select 'eine andere Schule' into @german;
            elseif @translation =        'a university' then
                select 'eine Universität' into @german;
            elseif @translation =        'for the guys which are loud' then
                select 'für die Jungs, die laut sind' into @german;
            elseif @translation =        'for the guys which are silent' then
                select 'für die Jungs, die schweigen' into @german;
            elseif @translation =        'Scanner Mk I' then
                select 'Scanner Mk I' into @german;
            elseif @translation =        'A scanner' then
                select 'Ein Scanner' into @german;
            elseif @translation =        'Corvette vessel' then
                select 'Korvettenschiff' into @german;
            elseif @translation =        'Frigate vessel' then
                select 'Fregattenschiff' into @german;
            elseif @translation =        'Cruiser vessel' then
                select 'Kreuzerschiff' into @german;
            elseif @translation =        'The corvette hull' then
                select 'Der Korvettenrumpf' into @german;
            elseif @translation =        'The frigate hull' then
                select 'Der Fregattenrumpf' into @german;
            elseif @translation =        'The cruiser hull' then
                select 'Der Rumpf des Kreuzers' into @german;
            elseif @translation =        'Ship killer launcher Mk I' then
                select 'Schiffskillerwerfer Mk I' into @german;
            elseif @translation =        'Counter missile launcher Mk I' then
                select 'Abwehrraketenwerfer Mk I' into @german;
            elseif @translation =        'The launcher for ship killers' then
                select 'Der Werfer für Schiffskiller' into @german;
            elseif @translation =        'The launcher for counter missiles' then
                select 'Der Werfer für Gegenraketen' into @german;
            elseif @translation =        'Improves armor' then
                select 'Verbessert die Rüstung' into @german;
            elseif @translation =        'Increases the amount of armor' then
                select 'Erhöht die Menge an Rüstung' into @german;
            elseif @translation =        'Speed Mk I' then
                select 'Geschwindigkeit Mk I' into @german;
            elseif @translation =        'FTL Speed Mk I' then
                select 'Überlicht Geschwindigkeit Mk I' into @german;
            elseif @translation =        'A drive' then
                select 'Ein Antrieb' into @german;
            elseif @translation =        'A FTL drive' then
                select 'Ein Überlicht Antrieb' into @german;
            elseif @translation =        'Eternal live' then
                select 'Ewiges Leben' into @german;
            elseif @translation =        'Laboratories' then
                select 'Labore' into @german;
            elseif @translation =        'Laser' then
                select 'Laser' into @german;
            elseif @translation =        'Missile' then
                select 'Rakete' into @german;
            elseif @translation =        'Counter Missile' then
                select 'Gegenrakete' into @german;
            elseif @translation =        'Point Defense' then
                select 'Punktverteidigung' into @german;
            elseif @translation =        'Armor' then
                select 'Rüstung' into @german;
            elseif @translation =        'Shield' then
                select 'Schild' into @german;
            elseif @translation =        'Speed' then
                select 'Geschwindigkeit' into @german;
            elseif @translation =        'FTL Speed' then
                select 'Überlicht Geschwindigkeit' into @german;
            elseif @translation =        'Electronic Warfare' then
                select 'Elektronische Kriegsführung' into @german;
            elseif @translation =        'Rocket Ammunition' then
                select 'Raketenmunition' into @german;
            elseif @translation =        'Point Defense Ammunition' then
                select 'Punktverteidigungsmunition' into @german;
            elseif @translation =        'Counter Rocket Ammunition' then
                select 'Gegenraketenmunition' into @german;
            elseif @translation =        'Armor improvement I' then
                select 'Rüstungsverbesserung I' into @german;
            elseif @translation =        'Corvette' then
                select 'Korvette' into @german;
            elseif @translation =        'Frigate' then
                select 'Fregatte' into @german;
            elseif @translation =        'Cruiser' then
                select 'Kreuzer' into @german;
            elseif @translation =        'How to buy wine.' then
                select 'Wie man Wein kauft.' into @german;
            elseif @translation =        'The construction yard research researches the construction yard.' then
                select 'Die Bauhofforschung erforscht den Bauhof.' into @german;
            elseif @translation =        'The orbitals Construction Yard research researches the orbitals construction yard.' then
                select 'Die Orbital-Bauhofforschung erforscht den Orbital-Bauhof.' into @german;
            elseif @translation =        'The laboratories research researches laboratories.' then
                select 'Die Labore forschen forschen Labore.' into @german;
            elseif @translation =        'The Market place research researches Market places.' then
                select 'Die Marktplatzforschung erforscht Marktplätze.' into @german;
            elseif @translation =        'The Metal works research researches Metal works.' then
                select 'Die Metallwerksforschung erforscht Metallwerke.' into @german;
            elseif @translation =        'The Special orbital ores research researches Special orbital ores.' then
                select 'Die Spezialorbital-Erze-Forschung erforscht Spezialorbital-Erze.' into @german;
            elseif @translation =        'The Asynchronous Investigations research researches Asynchronous Investigations.' then
                select 'Die Forschung zu asynchronen Ermittlungen untersucht asynchrone Ermittlungen.' into @german;
            elseif @translation =        'The Laser research researches ...' then
                select 'Die Laserforschung erforscht ...' into @german;
            elseif @translation =        'The Missile research researches ...' then
                select 'Die Raketenforschung erforscht ...' into @german;
            elseif @translation =        'The Counter Missile research researches ...' then
                select 'Die Counter-Missile-Forschung erforscht ...' into @german;
            elseif @translation =        'The point defense research researches ...' then
                select 'Die Punktverteidigungsforschung forscht ...' into @german;
            elseif @translation =        'The Armor research researches ...' then
                select 'Die Rüstungsforschung erforscht ...' into @german;
            elseif @translation =        'The Shield research researches ...' then
                select 'Die Shield-Forschung erforscht ...' into @german;
            elseif @translation =        'The Speed research researches sub light ...' then
                select 'Die Speed-Forschung erforscht Unterlicht ...' into @german;
            elseif @translation =        'The FTL Speed research researches FTL ...' then
                select 'Die FTL-Speed-Forschung erforscht FTL ...' into @german;
            elseif @translation =        'The EW research researches electronic warfare.' then
                select 'Die EW-Forschung erforscht die elektronische Kriegsführung.' into @german;
            elseif @translation =        'a bunch of rockets.' then
                select 'ein Haufen Raketen.' into @german;
            elseif @translation =        'a bunch of bullets.' then
                select 'ein Haufen Kugeln.' into @german;
            elseif @translation =        'another bunch of rockets.' then
                select 'ein weiterer Haufen Raketen.' into @german;
            elseif @translation =        'Improves the armor improvement module' then
                select 'Verbessert das Rüstungsverbesserungsmodul' into @german;
            elseif @translation =        'The Corvette research researches Corvettes.' then
                select 'Die Korvettenforschung erforscht Korvetten.' into @german;
            elseif @translation =        'The Frigate research researches Frigates.' then
                select 'Die Fregattenforschung erforscht Fregatten.' into @german;
            elseif @translation =        'The Cruiser research researches Cruisers.' then
                select 'Die Kreuzerforschung erforscht Kreuzer.' into @german;
            elseif @translation =        'Shield Mk I' then
                select 'Schild Mk I' into @german;
            elseif @translation =        'A shield' then
                select 'Ein Schild' into @german;
            elseif @translation =        'Laser Mk I' then
                select 'Laser Mk I' into @german;
            elseif @translation =        'Point Defense Mk I' then
                select 'Punktverteidigung Mk I' into @german;
            elseif @translation =        'A laser' then
                select 'Ein Laser' into @german;
            elseif @translation =        'A point defense' then
                select 'Eine Punktverteidigung' into @german;
            elseif @translation =        'Nuclear ship killer war head' then
                select 'Atomschiff-Killer-Kriegskopf' into @german;
            elseif @translation =        'Counter war head' then
                select 'Gegenkriegskopf' into @german;
            elseif @translation =        'Ship Killer Motor Mk I' then
                select 'Schiff Killer Motor Mk I' into @german;
            elseif @translation =        'Counter Motor Mk I' then
                select 'Gegenmotor Mk I' into @german;
            elseif @translation =        'Nuclear ship killer missile Mk I' then
                select 'Nukleare Schiffskillerrakete Mk I' into @german;
            elseif @translation =        'Counter missile Mk I' then
                select 'Gegenrakete Mk I' into @german;
            else
                select concat('ERROR FOR: ', @translation, ' idTranslation: ' , @idTranslation) into @german;
            end if;
            #@formatter:on

            insert into translation (idTranslation, idTranslatable, languageCode, translation) values (null, @idTranslation, 'de', @german);
            select LAST_INSERT_ID() into @idTranslation;
            SET i = i + 1;
        END WHILE;
End |
DELIMITER ;

call insertGermanTranslations();
drop procedure insertGermanTranslations;








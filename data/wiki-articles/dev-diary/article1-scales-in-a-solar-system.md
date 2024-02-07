Ein Dev Diary über die Problematiken des Skalierens von entfernungen in den größenordnungen eines sonnensystems

todo: gif erzeugen mit battle arena: starten an der flotte, rauszoomen und zeigen wie groß ein sonnensystem ist

problembeschreibung:

1. man muss größe von raumschiffen ebenso darstellen wie die des systems
2. gleichzeitig im blick behalten, dass die arena auch benutzbar sein und spaß machen soll
3. die darstellung muss einen mehrwert bringen, d.h. man muss beim raketenbeschuss die skala von "3 millionen km zu 600 km flottenumfang" sinnvoll darstellen
4. reisezeiten in kampfrunden sind absurd - 681 runden bewegung und 19 runden kampf

---

Bitte schreibe mir einen Artikel über die Problematiken des gedanklichen Skalierens von Entfernungen in den Größenordnungen eines Sonnensystems im Vergleich zu einer Flotte von
Raumschiffen.

Es geht darum den Nutzern eines Science-Fiction-Raumkampf-Computerspiels ein bisschen Content für die Wartezeit auf das neue Release zu liefern.

Bitte aus der Perspektive des Akademiedozenten schreiben, der ein Sensorsystem bzw die Darstellung der Sensorwerte auf den Konsolen des Kriegsschiffs beschreiben soll.
Die Zielgruppe sind Kadetten auf der Navy-Akademie.  
Der Dozent möchte zugleich das Wiederholdisplay und dessen Benutzung beschreiben als auch die gedankliche Akrobatik, die nötig ist um Handlngen auf planetaren Distanskalen zu
verstehen.

Der Artikel soll ein Vorwort enthalten, dass folgendes beschreibt:

- es geht um das wiederholdisplay und der letzte kampf zwischen zwei kadettengruppen wird dargestellt
- der simulierte kampf wurde abgebrochen als die kadetten etwas zu emotional wurden - leichte Rüge einfügen

Der Artikel selbst soll folgende Punkte im Fließtext ansprechen:

In Kapitel 1 "Das Wiederholdisplay"

- Man muss die Größe von Raumschiffen ebenso darstellen können wie die des Sonnensystems und seiner Planeten. Es soll gleichzeitig die schieren Ausmaße in der UI/UX transportiert
  werden wie natürlich die Benutzbarkeit des Interfaces.
  Die Benutzung der UI soll natürlich Spaß machen, es geht schließlich um ein Spiel.

- Ein Beispiel soll die Zoom-Skala auf der rechten Seite des Displays sein.  
  Effektiv stellt man nur einen Prozentwert in der Skala dar. Das funktioniert aber nur für große Zoomstufen, denn man sieht eine proportionale Anzeige.  
  Allerdings soll die ein logarithmischer Entfernungswert dargestellt werden und der Wert wird, mathematisch gesprochen, mit größerem Anstieg immer schneller größer.  
  Das führt dazu, dass man die Zahlenwerte "0.001" und "3" optisch sinnvoll unterscheidbar darzustellen - unmöglich in dieser Kompression.  
  Die logische Lösung ist die Normalisierung des Zoomlevels um es als menschenlesbaren Wert zu darzustellen.

- Die Darstellung muss einen Mehrwert bringen, d.h. man muss z.B. beim Raketenbeschuss die Skala von "3 Millionen Kilometer zu 600 km Flottenverteilung" sinnvoll darstellen. Man
  muss weite Entfernungen durch zoomen und scrollen überbrücken können, sehr viele Details unter bringen und gleichzeitig einen strategischen Überblick wie auch taktische Details
  zeigen.

- Das Skalierungsproblem der Entfernungen gibt es auch in der zeitlichen Dimension.
  Eine eindringende Flotte hat eine enorme Entfernung zu überbrücken, selbst wenn die Verteidiger ihr entgegen fliegen. Das Userinterface muss so gestaltet sein, dass man die
  Darstellung der ablaufenden Kampfrunden sinnvoll "vorspulen" kann, so dass die Zeiten, in denen sich die Flotten in Position bringen, zwar unterhaltsam, aber nicht eintönig sind.

In Kapitel 2 "Mechanik von Raketengefechten"

- Nicht vergessen darf man die Geschwindigkeit und die Richtung des Schiffes als Basis für die Raketennavigation. Die Reichweite variiert stark je nach Richtung des Ziels.
- Richtungsunabhängige Raketenwerfer an sich sind natürlich notwendig um das volle Potential aus gelungener Navigation auszuschöpfen - aber auch mit alten Werfern muss man höllisch
  aufpassen und keinen Fehleinschätzungen erliegen.
- Umso größer die Brenndauer des Raketenmotors ist, desto größer ist der reichweiten-stärkende Effekt der Grundgeschwindigkeit. Die Antriebsleistung dahingegen spielt eine
  untergeordnete Rolle, da der effekt sich mit größerer Raketengeschwindigkeit reduziert.  
  Das Beispiel dafür sind lichtschnelle Strahlwaffen wie Laser und Graser, deren Reichweite sich durch die Vektoren der Plattform kaum verändert.

Das Nachwort soll auf einen Folgeartikel zum Thema virtuelle Flottenmanöver im Weltraum, Splines und Bezierkurven hinweisen.

---

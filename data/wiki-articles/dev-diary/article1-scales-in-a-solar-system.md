Ein Dev Diary über die Problematiken des Skalierens von entfernungen in den größenordnungen eines sonnensystems

todo: gif erzeugen mit battle arena: starten an der flotte, rauszoomen und zeigen wie groß ein sonnensystem ist

problembeschreibung:

1. man muss größe von raumschiffen ebenso darstellen wie die des systems
2. gleichzeitig im blick behalten, dass die arena auch benutzbar sein und spaß machen soll
3. die darstellung muss einen mehrwert bringen, d.h. man muss beim raketenbeschuss die skala von "3 millionen km zu 600 km flottenumfang" sinnvoll darstellen
4. reisezeiten in kampfrunden sind absurd - 681 runden bewegung und 19 runden kampf

---

Bitte schreibe mir einen Artikel über die Problematiken des Skalierens von Entfernungen in den Größenordnungen eines Sonnensystems im Vergleich zu einer Flotte von Raumschiffen.

Es geht darum den Nutzern eines Computerspiels ein bisschen Content für die Wartezeit auf das neue Release zu liefern.  
Bitte aus der Perspektive des Spieleentwicklers neu schreiben. Das Thema soll getragen, allerdings nicht konstant angesprochen werden.

Der Artikel soll ein Vorwort enthalten, dass folgendes beschreibt:

Der Artikel selbst soll folgende Punkte im Fließtext ansprechen:

- Man muss die Größe von Raumschiffen ebenso darstellen können wie die des Sonnensystems und seiner Planeten. Es soll gleichzeitig die schieren Ausmaße in der UI/UX transportiert
  werden wie natürlich die Benutzbarkeit des Interfaces.
  Die Benutzung der UI soll natürlich Spaß machen, es geht schließlich um ein Spiel.

- Ein Beispiel soll die Zoom-Skala auf der rechten Seite des Displays sein.  
  Effektiv stellt man nur einen Prozentwert in der Skala dar. Das funktioniert aber nur für große Zoomstufen, denn man sieht eine proportionale Anzeige.  
  Allerdings soll die ein logarithmischer Entfernungswert dargestellt werden und der Wert wird, mathematisch gesprochen, mit größerem Anstieg immer schneller größer.  
  Das führt dazu, dass man die Zahlenwerte "0.001" und "3" optisch sinnvoll unterscheidbar darzustellen - unmöglich in dieser Kompression.

Eine Lösung wäre, die echte Berechnung von der Anzeige abzugrenzen und die Anzeige so zu faken, dass es aussieht als würde es darstellen, was tatsächlich passiert. Das würde
natürlich bedeuten, dass man sich plausible Bruchstellen anlegt und bestimmte Teile der Skala für bestimmte Zahlenbereiche zuständig wären.
Zum Glück kann man immer auf die Mathematik bauen, es gibt immer einen Weg, um irgendwas zu normalisieren.

- Die Darstellung muss einen Mehrwert bringen, d.h. man muss z.B. beim Raketenbeschuss die Skala von "3 Millionen Kilometer zu 600 km Flottenverteilung" sinnvoll darstellen. Man
  muss weite Entfernungen durch zoomen und scrollen überbrücken können, sehr viele Details unter bringen und gleichzeitig einen strategischen Überblick wie auch taktische Details
  zeigen.

- Das Skalierungsproblem der Entfernungen gibt es auch in der zeitlichen Dimension.
  Eine eindringende Flotte hat eine enorme Entfernung zu überbrücken, selbst wenn die Verteidiger ihr entgegen fliegen. Das Userinterface muss so gestaltet sein, dass man die
  Darstellung der ablaufenden Kampfrunden sinnvoll "vorspulen" kann, so dass die Zeiten, in denen sich die Flotten in Position bringen, zwar unterhaltsam, aber nicht eintönig sind.

Das Nachwort soll auf einen Folgeartikel zum Thema virtuelle Flottenmanöver im Weltraum, Splines und Bezierkurven hinweisen.

---

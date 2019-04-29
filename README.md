# BreakNotifier

## Projektwiki

### Inhalt
* Überblick
* Projektstrukturplan
* Anwendungsfalldiagramm  

* Systemarchiktektur  

# Überblick
    In diesem Projekt wird eine App verwirklicht, welche dazu dient, die Pause nicht mehr zu überhören.
    Da wir z.B. in einer sehr großen Schule sind wo wir nicht in jedem Raum die Glocke für die Pause hören.
    Ebenfalls benachrichtigt die App über Supplierungen, dies ist wichtig für Lehrer, da es oftmals vorkommt,
    dass diese nicht mit bekommen, dass Sie an diesem Tag eine Supplierung haben, dadurch sehr verspätet oder 
    garnicht zu dieser erscheinen.
    Dazu kommt noch eine Benachrichtigung für bevorstehende Tests oder Schularbeiten, damit sich Schüler und
    Lehrer rechtzeitig darauf vorbereiten können.
# Projektstrukturplan
![PSP](https://github.com/DavidWeinberger/BreakNotifier/blob/master/Fotos/ProjectStructer.png)
# Anwendungsfalldiagramm
  ![Anwendungsfalldiagramm](https://github.com/DavidWeinberger/BreakNotifier/blob/master/Fotos/Use%20Cases.png)  
# Systemarchitektur
    Wie man in dem folgenden Diagramm sieht, greift man direkt auf den Webuntis-Server zu.
    Dies wird von Montag bis Freitag täglich einmal gemacht, um zu kontrollieren ob sich der
    Referenz-Stundenplan geändert hat. Wenn dies der Fall ist, bekommt man eine Benachrichtigung.
    Dies kann nämlich bei Supplierungen passieren.
  
 ![Systemarchitektur](https://github.com/DavidWeinberger/BreakNotifier/blob/master/Fotos/Untitled%20Diagram.png) 
    

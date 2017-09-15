KontaktyNaUrady.cz
==================

API, ktere poskytuje strukturovane konktatni informace na statni instituce (obce, 
urady, organizacni slozky statu).

Informace se ziskavaji prochazenim a dolovanim dat z registru datovych schranek 
(http://seznam.gov.cz/ovm/welcome.do)


Instalace
---------

1. nainstalujte play 1.4 (ma podporu Java8) framework (https://www.playframework.com/documentation/1.4.x/install)
2. spuste na prikazove radce prikaz "play dependencies" (stahne vsechny zavislosti, vytvori adresare libs a 
modules)
3. spuste na prikazove radce prikaz "play eclipsify" pro vytvoreni projektovych souboru pro 
eclipsu respektive "play idealize" pro vytvoreni projektovych souboru pro ideu
4. importujte projekt do vaseho ide (eclipse: File -> Import -> Existing Projects into 
Workspace)
5. pro snazsi praci stahnte si plugin pro play do vaseho ide (novejsi idea ma podporu 
oficialni formou pluginu, do eclipse napr. https://github.com/erwan/playclipse)

Technologie
-----------

- Java 8
- Play Framework 1.3 (https://www.playframework.com/documentation/1.3.x/home) for api and admin
- JSoup (http://jsoup.org/) pro dolovani dat z webovych stranek registru datovych schranek 
(http://seznam.gov.cz/ovm/welcome.do)

FAQ
-----------

- Debugovani v Eclipse: http://stackoverflow.com/questions/7172013/debug-playframework-in-eclipse

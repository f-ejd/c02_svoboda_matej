# PGRF1 – Úloha 2: Vyplňování a ořezávání

**Autor:** Matěj Svoboda  
**Předmět:** Počítačová grafika 1 (PGRF1) – FIM UHK

## Popis implementace
Aplikace implementuje kompletní požadavky 1. a 2. úlohy včetně bonusových rozšíření:
- **Úsečky a zarovnání (Shift):** Bresenhamův algoritmus (`LineRasterizerBresenham`) s interaktivní pružnou čárou a přichytáváním na vodorovné, svislé a úhlopříčné (45°) úsečky.
- **Semínkové vyplnění (`SeedFill`):** Nerekurzivní zásobníková implementace (`ArrayDeque<Point>`) podporující režim omezení barvou pozadí i barvou hranice.
- **Scan-Line vyplnění (`ScanLine`):** Vektorové vyplnění n-úhelníku se zkrácením nevodorovných hran o 1 pixel (ošetření dotahů a přetahů), vlastním algoritmem řazení průsečíků (Bubble Sort) a obtažením hranice.
- **Ořezávání polygonu (`SutherlandHodgman`):** Ořezání libovolného uzavřeného polygonu konvexním polygonem (výchozí 5-úhelník) s možností volby orientace vrcholů (`AUTO` / `CW` / `CCW`).
- **Ořezávání úsečky (`CohenSutherland`):** Ořezání úseček pravoúhlým oknem pomocí 4bitových kódů polohy (`outcode`).
- **Bonus 1 (`PatternFill`):** Rozhraní `PatternFill` a implementace `CheckerPattern` pro šachovnicové vyplnění v `SeedFill` i `ScanLine`.
- **Bonus 2 (GUI ovládání):** Horní lišta `JToolBar` s dialogem `JColorChooser` pro změnu barvy výplně a barvy hranice a přepínači algoritmů.
- **Bonusové editace:** Přesun vrcholů úseček i polygonů pravým tlačítkem myši (`RMB`), mazání vrcholů prostředním tlačítkem (`MMB`), vložení vrcholu na nejbližší hranu (`I`).

## Ovládání
| Klávesa / Myš | Akce |
| :--- | :--- |
| `1` | Režim kreslení úsečky (LMB tažení, držení `Shift` = vodorovná/svislá/úhlopříčná) |
| `2` | Režim kreslení polygonu (pružná čára k prvnímu a poslednímu vrcholu) |
| `3` | Seed Fill – omezení **barvou pozadí** (LMB kliknutí do plochy) |
| `4` | Seed Fill – omezení **barvou hranice** (LMB kliknutí dovnitř polygonu) |
| `5` | Interaktivní editace obdélníkového ořezávacího okna (LMB tažení) |
| `6` | Interaktivní editace ořezávacího polygonu (`LMB` přidání, `RMB` přesun, `R` vymazání) |
| `S` | Zapnutí / vypnutí **Scan-Line** vyplnění |
| `P` | Zapnutí / vypnutí **Sutherland-Hodgman** ořezání polygonu |
| `L` | Zapnutí / vypnutí **Cohen-Sutherland** ořezání úseček |
| `V` | Zapnutí / vypnutí vyplňování vzorem (**PatternFill**) |
| `O` | Přepnutí orientace ořezávacího polygonu (`AUTO` / `CW` / `CCW`) |
| `I` | Vložení nového vrcholu na nejbližší hranu polygonu |
| `RMB tažení` | Přesun koncového bodu úsečky nebo vrcholu polygonu |
| `MMB klik` | Smazání nejbližšího vrcholu polygonu |
| `Delete` | Smazání celého uživatelského polygonu |
| `C` | Kompletní vymazání plátna, struktur a reset všech režimů |

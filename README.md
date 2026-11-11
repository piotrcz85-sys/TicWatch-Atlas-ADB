# TicWatch Atlas ADB Console

Minimalna aplikacja Android do połączenia z zegarkiem Mobvoi TicWatch Atlas przez debugowanie bezprzewodowe.

## Funkcje
- Parowanie kodem ADB (adres IP i port parowania)
- Połączenie przez osobny port ADB
- Konsola `adb shell` z wynikami i udostępnianiem logu
- **Instalowanie pojedynczych plików APK** wybranych z pamięci telefonu (systemowy selektor dokumentów)
- Zapisywanie ostatniego adresu i portu połączenia

## Instalacja aplikacji na zegarku
1. Włącz na zegarku Opcje programisty → Debugowanie bezprzewodowe.
2. Sparuj aplikację z zegarkiem, podając IP, port parowania i sześciocyfrowy kod.
3. Wpisz port połączenia wyświetlany osobno w ustawieniach debugowania i kliknij Połącz.
4. Wybierz **Wybierz APK z telefonu i zainstaluj na zegarku**, wskaż lokalny plik `.apk`.
5. Aplikacja kopiuje plik do prywatnego folderu tymczasowego telefonu, wywołuje `Kadb.install()` i usuwa plik tymczasowy.

Tylko pojedyncze pliki `.apk`; pakiety split APK (`.apks`, `.xapk`, `.apkm`) nie są jeszcze obsługiwane. Program nie omija ograniczeń zgodności aplikacji z Wear OS.

## Budowanie przez GitHub Actions
Wejdź do **Actions → Build Atlas ADB APK**. Workflow uruchamia się też po zmianach w gałęzi `main`. Po zakończeniu sukcesem pobierz artefakt **Atlas-ADB-Console-debug**, który zawiera `app-debug.apk`.

Projekt wymaga potwierdzenia kompilacji i fizycznych testów połączenia oraz instalacji na TicWatch Atlas. Nie podawaj kodu parowania innym osobom.

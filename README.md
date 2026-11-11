# TicWatch Atlas ADB Console

Aplikacja na Androida w Kotlinie do połączenia ADB przez Wi-Fi z zegarkiem Mobvoi TicWatch Atlas i innymi urządzeniami Wear OS.

## Funkcje
- Parowanie ADB za pomocą adresu IP, portu parowania i sześciocyfrowego kodu
- Połączenie przez osobny port ADB
- Konsola poleceń `adb shell`, wyświetlanie wyników
- Skróty do diagnostyki baterii, temperatur, RAM, ekranu i aplikacji
- Udostępnianie wyników

## APK na GitHubie
Otwórz zakładkę **Actions**, wybierz **Build Atlas ADB APK** i uruchom **Run workflow**, jeśli workflow jeszcze nie wystartował. Po udanym buildzie wybierz artefakt `Atlas-ADB-Console-debug` i pobierz APK.

## Połączenie
Włącz debugowanie bezprzewodowe na zegarku. Wpisz w aplikacji adres IP, port parowania i kod wyświetlony na zegarku. Po sparowaniu użyj **innego portu połączenia** z ekranu debugowania bezprzewodowego.

**Status:** projekt testowy. Automatyczna kompilacja nie zastępuje testu połączenia z fizycznym zegarkiem. Komendy konsoli mogą zmieniać stan urządzenia.

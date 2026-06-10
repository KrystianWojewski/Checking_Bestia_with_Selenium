# Skrypt do sprawdzania wersji Bestii

## Użycie

### Recipients.txt

W tym pliku widnieją wszystkie adresy na jakie ma przyjść mailowe powiadominie o pojawieniu się nowej wersji Bestii na stronie https://budzetjst.pl/pobieranie/instalacja/bestia/

### BestiaVersion.txt

W tym pliku możesz sprawdzić jaka jest/była ostania sprawdzana wersja Bestii. Pliku raczej nie zmieniaj, chyba że do testów.

### Tworzenie pliku wykonywalnego

```
mvn clean package

jpackage --type app-image --name BestiaVersion --input target --main-jar bestiaversion-1.0-SNAPSHOT.jar --main-class com.bestiaversion.Main
```

Tworzymy plik .jar w folderze target, a następnie tworzymy folder BestiaVersion wraz z plikiem .exe

Na sam koniec należy przekopiować pliki .txt do folderu BestiaVersion

## Konfiguracja

### Jak skonfigurować aplikację, aby wysyłka szła z mojego adresu e-mail?

Aplikacja korzysta z wtyczki JakartaMail, która wymaga wprowadzenia od nas kilku informacji do zmiennych środowiskowych systemu.

```
DOWNLOAD_DIR = ścieżka do zapisu plików

EMAIL_FROM = wprowadź adres e-mail z którego ma wychodzić mail

EMAIL_USERNAME = często będzie to samo co w poprzedniej zmiennej (zależy od poczty)

EMAIL_PASSWORD = hasło wygenerowane w ustawieniach poczty (szukaj App Password dla Twojej poczty w google)

EMAIL_HOST = smtp host poczty (nas głównie interesuje Outlook)

EMAIL_HOST = {
    Outlook: smtp.office365.com,
    Gmail: smtp.gmail.com,
    ...
}
```

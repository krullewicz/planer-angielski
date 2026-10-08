# 📒 Planer Angielski

Aplikacja na Androida do codziennej nauki angielskiego, zbudowana na wzór papierowych planerów
językowych (inspiracja: *Planer językowy – Czas na angielski*, wyd. Edgard). Zamiast kartek masz
w telefonie stronę na każdy dzień, plan tygodnia, podsumowanie miesiąca, tracker nawyków
i słowniczek z fiszkami.

> Projekt hobbystyczny, niezwiązany z wydawnictwem Edgard. Wszystkie treści w aplikacji
> (słówka, zwroty, wskazówki gramatyczne, wyzwania) zostały napisane od nowa.

## Co jest w środku

| Zakładka | Zawartość |
|---|---|
| **Dziś** | motto dnia, licznik minut nauki z dziennym celem i serią dni, tracker nawyków (słówka, słuchanie, czytanie, mówienie, pisanie, gramatyka), **słówko dnia**, **zwrot/idiom dnia**, **mini-gramatyka**, **wyzwanie dnia**, lista zadań, nastrój i notatki. Strzałkami można przejść do innych dni. |
| **Tydzień** | cele tygodnia, wykres minut dzień po dniu, plan na każdy dzień (rozwijane listy zadań), podsumowanie tygodnia. |
| **Miesiąc** | cel przewodni, kalendarz aktywności (im ciemniejszy kolor, tym bliżej dziennego celu), tracker nawyków w miesiącu, cele i podsumowanie miesiąca. |
| **Słówka** | własny słowniczek z wyszukiwarką, fiszki z powtórkami metodą pudełek Leitnera (1 → 2 → 4 → 8 → 16 dni), kierunek EN→PL lub PL→EN. |
| **Ustawienia** | dzienny cel w minutach, codzienne przypomnienie (powiadomienie o wybranej godzinie), edycja listy nawyków, eksport/import kopii zapasowej (plik JSON). |

Dane są zapisywane tylko lokalnie na telefonie (bez konta i internetu). Działa jasny i ciemny motyw.

**Technologie:** Kotlin, Jetpack Compose, Material 3, kotlinx.serialization. Minimalny Android: 8.0 (API 26).

---

## Instalacja na Pixelu 9a

Są dwie drogi. **Sposób A** nie wymaga instalowania niczego na komputerze – APK zbuduje GitHub.

### Sposób A: pobranie gotowego APK z GitHuba (najprostszy)

**1. Wrzuć repozytorium na GitHuba** (jednorazowo, na komputerze):

```bash
cd aplikacja-językowa
git init -b main          # pomiń, jeśli repozytorium już istnieje
git add .
git commit -m "Planer Angielski"
# utwórz puste repozytorium na github.com (np. "planer-angielski"), a potem:
git remote add origin https://github.com/TWOJ_LOGIN/planer-angielski.git
git push -u origin main
```

Po każdym `git push` GitHub Actions (zakładka **Actions** w repozytorium) sam zbuduje APK – trwa to ok. 5 minut.

**2. Pobierz APK na telefon.** Masz dwie możliwości:

- **Wydanie (wygodniejsze):** utwórz tag, a APK pojawi się w zakładce **Releases**:
  ```bash
  git tag v1.0
  git push origin v1.0
  ```
  Na Pixelu otwórz w Chrome `https://github.com/TWOJ_LOGIN/planer-angielski/releases`
  i stuknij `PlanerAngielski.apk`.
- **Artefakt z builda:** w zakładce **Actions** wejdź w ostatni udany build i pobierz
  `PlanerAngielski-apk` (wymaga zalogowania na GitHubie). To plik ZIP – na telefonie otwórz go
  w aplikacji **Files by Google** i wypakuj `PlanerAngielski.apk`.

**3. Zainstaluj na Pixelu 9a:**

1. Stuknij pobrany plik `PlanerAngielski.apk` (w powiadomieniu o pobraniu albo w *Files → Pobrane*).
2. Android zapyta o zgodę na instalowanie aplikacji z nieznanych źródeł:
   *„Ze względów bezpieczeństwa telefon nie może instalować nieznanych aplikacji z tego źródła”* →
   stuknij **Ustawienia** → włącz **Zezwalaj z tego źródła** → wróć strzałką.
3. Stuknij **Zainstaluj**.
4. Jeśli pojawi się okno **Google Play Protect** („Nieznana aplikacja”), wybierz
   **Więcej szczegółów → Zainstaluj mimo to**. To normalne dla aplikacji spoza Sklepu Play.
5. Uruchom **Planer Angielski** z szuflady aplikacji. Jeśli chcesz przypomnień, włącz je
   w *Ustawieniach* aplikacji i zezwól na powiadomienia.

### Sposób B: instalacja z komputera przez kabel USB

Wymaga [Android Studio](https://developer.android.com/studio) (zawiera Android SDK i JDK).

1. **Włącz tryb programisty na Pixelu:** *Ustawienia → Informacje o telefonie* → stuknij
   7 razy w **Numer kompilacji**.
2. **Włącz debugowanie USB:** *Ustawienia → System → Opcje programisty → Debugowanie USB*.
3. Podłącz telefon kablem USB-C i na telefonie zaakceptuj **Zezwolić na debugowanie USB?**
4. Następnie **albo**:
   - otwórz folder projektu w Android Studio, wybierz swój telefon na liście urządzeń
     i kliknij ▶ **Run**,
   - **albo** w terminalu (z zainstalowanym Android SDK):
     ```bash
     ./gradlew installRelease
     ```
     (lub `./gradlew assembleRelease` i `adb install app/build/outputs/apk/release/app-release.apk`).

> Jeśli budujesz z terminala bez Android Studio, utwórz plik `local.properties` ze ścieżką
> do SDK, np. `sdk.dir=/home/ty/Android/Sdk`.

---

## Aktualizacje aplikacji i stały klucz podpisu (zalecane)

Android pozwala zainstalować nowszą wersję „na wierzch” tylko wtedy, gdy jest podpisana **tym
samym kluczem**. Bez dodatkowej konfiguracji każdy build na GitHubie jest podpisywany nowym,
tymczasowym kluczem debug – wtedy przed instalacją nowej wersji trzeba odinstalować starą
(co **usuwa dane**, więc najpierw zrób *Ustawienia → Kopia zapasowa → Eksportuj*).

Żeby aktualizacje instalowały się bez utraty danych, raz wygeneruj własny klucz i dodaj go
do sekretów repozytorium:

```bash
keytool -genkeypair -v -keystore release.jks -alias planer \
  -keyalg RSA -keysize 2048 -validity 10000
base64 -w0 release.jks > release.jks.b64
```

W repozytorium na GitHubie: **Settings → Secrets and variables → Actions → New repository secret**
i dodaj cztery sekrety:

| Nazwa | Wartość |
|---|---|
| `KEYSTORE_BASE64` | zawartość pliku `release.jks.b64` |
| `SIGNING_STORE_PASSWORD` | hasło do keystore podane w `keytool` |
| `SIGNING_KEY_ALIAS` | `planer` |
| `SIGNING_KEY_PASSWORD` | hasło klucza (zwykle to samo) |

Plik `release.jks` przechowuj bezpiecznie i **nie dodawaj go do repozytorium** (`.gitignore`
już go pomija). Przy nowej wersji podbij `versionCode` i `versionName` w `app/build.gradle.kts`
i wypchnij nowy tag (`v1.1`, `v1.2`…).

> Uwaga: przejście z klucza debug na własny klucz też wymaga jednorazowej reinstalacji –
> zrób wcześniej eksport danych.

---

## Struktura projektu

```
app/src/main/java/pl/planer/angielski/
├── MainActivity.kt          – nawigacja dolna (5 zakładek)
├── PlannerViewModel.kt      – logika: dni, tygodnie, miesiące, słówka, ustawienia
├── data/
│   ├── Models.kt            – modele danych (zapisywane jako JSON)
│   ├── Repository.kt        – zapis/odczyt pliku, eksport/import
│   └── Content.kt           – słówka, zwroty, gramatyka, wyzwania i motta dnia
├── reminder/Reminders.kt    – codzienne powiadomienie
└── ui/                      – motyw, wspólne komponenty i ekrany
```

Żeby dodać własne słówka dnia, zwroty czy wyzwania, wystarczy dopisać pozycje do list
w `data/Content.kt` – treść dnia jest wybierana rotacyjnie według daty.

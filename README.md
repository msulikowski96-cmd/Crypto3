# Cryptograms — gra na Androida

Gra logiczna: odszyfruj słynny cytat, w którym każda litera alfabetu została
zastąpiona inną literą. Napisana w Kotlinie z Jetpack Compose.

## Funkcje

- **Plansza z kafelkami** — u góry Twoja litera, u dołu litera szyfru. Zielony
  róg oznacza poprawnie odgadniętą literę, czerwona litera — błąd.
- **3 poziomy trudności** (Łatwy / Średni / Trudny) — dotknij etykiety poziomu,
  aby zmienić. Łatwy odkrywa część liter na start i daje 3 podpowiedzi.
- **Licznik czasu i błędów**, pauza (zegar staje też, gdy aplikacja jest w tle).
- **Podpowiedź, Cofnij, Wymaż** w dolnym pasku.
- **Wyniki** — czas, błędy, podpowiedzi, rekordy dla każdego poziomu;
  możliwość pokazania rozwiązania.
- **Ustawienia**: pokazywanie błędów, podświetlanie pasujących symboli,
  automatyczne przejście do następnej litery, licznik czasu, wibracje,
  statystyki.
- **Samouczek „Jak grać”** (5 kart) — pokazywany przy pierwszym uruchomieniu.
- **Cytaty po angielsku i po polsku** (ok. 75 angielskich i 144 polskie) — wybór w Ustawienia →
  Ogólne → Język cytatów. W trybie polskim szyfrowane są też litery
  Ą Ć Ę Ł Ń Ó Ś Ź Ż, a klawiatura ma dodatkowy rząd z polskimi znakami.
  Domyślny język cytatów zależy od języka telefonu.
- Interfejs po angielsku i po polsku (zależnie od języka telefonu).
- Obsługa fizycznej klawiatury (litery + Backspace).

## Budowanie

Wymagania: JDK 17+, Android SDK 34.

```bash
./gradlew testDebugUnitTest   # testy logiki gry
./gradlew assembleDebug       # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease     # app/build/outputs/apk/release/app-release.apk
```

Gotowy APK możesz też budować w GitHub Actions — wystarczy dodać plik
`.github/workflows/build.yml` z poleceniem `./gradlew assembleRelease`.

### Wydanie do Google Play

Identyfikator aplikacji: `io.github.msulikowski96.cryptograms`.

Do podpisu wersji release służy klucz przesyłania (upload key). Utwórz w
katalogu głównym plik `keystore.properties` (jest w `.gitignore`, nigdy go
nie commituj):

```properties
storeFile=/ścieżka/do/upload-keystore.jks
storePassword=...
keyAlias=upload
keyPassword=...
```

Potem:

```bash
./gradlew bundleRelease   # app/build/outputs/bundle/release/app-release.aab
```

Bez `keystore.properties` wersja release podpisuje się kluczem debug (do
testów na własnym telefonie, nie do Google Play).

Przed każdą nową wersją podnieś `versionCode` (i `versionName`) w
`app/build.gradle.kts` — Google Play odrzuca plik z tym samym `versionCode`.

## Struktura

```
app/src/main/java/com/example/cryptograms/
├── MainActivity.kt          # nawigacja gra ↔ ustawienia, cykl życia
├── game/
│   ├── Puzzle.kt            # szyfr (permutacja bez punktów stałych), poziomy
│   ├── Language.kt          # alfabet i układ klawiatury dla EN / PL
│   ├── GameState.kt         # zasady gry: zgadywanie, podpowiedzi, cofanie
│   └── GameViewModel.kt     # stan gry + zegar
├── data/
│   ├── Quotes.kt            # baza cytatów (angielskie i polskie)
│   └── SettingsRepository.kt# ustawienia i statystyki (SharedPreferences)
└── ui/
    ├── GameScreen.kt        # ekran gry: kafelki, klawiatura, pasek akcji
    ├── SettingsScreen.kt    # ekran ustawień
    ├── Sheets.kt            # arkusze „Wyniki” i „Jak grać”
    └── theme/Theme.kt       # kolory, tło w kropki
```

# Number Guess – backend

Backend är spelets API. Det är byggt med Spring Boot och sparar spelare, spel och gissningar i MySQL.

### Lokal körning 

Du behöver Docker. Öppna terminalen i projektmappen som innehåller `docker-compose.yml` och kör:

```sh
docker compose up --build -d
```

Detta startar frontend, backend och MySQL. MySQL skapar databasen automatiskt. API:t blir tillgängligt på (http://localhost:8080).

Docker bygger frontend och backend från källkoden. Du behöver alltså inte ladda ner färdiga app-images eller skapa databasen själv.

### Starta bara backend

För att starta backend utan Docker Compose behöver du Java 21 och MySQL 8. Skapa databasen `localdb` om den inte redan finns (standardanvändare: `root`, standardlösenord: `password`):

```sh
mysql -uroot -ppassword -e "CREATE DATABASE localdb;"
```

Starta sedan backend från den här mappen:

```sh
./mvnw spring-boot:run
```

API:t startar på (http://localhost:8080). Du kan ändra databasanslutningen med `DB_URL`, `DB_USERNAME` och `DB_PASSWORD`.

## Köra tester

Du behöver Java 21 och Docker. Docker startar en tillfällig MySQL-databas för integrationstesterna. Kör testerna från backendmappen:

```sh
./mvnw clean verify
```

## CI/CD

GitHub Actions kör tester när kod pushas till, eller en pull request riktas mot, `dev` eller `main`. Flödet startar MySQL, kompilerar koden och kör testerna.

När kod pushas till `dev` eller `main` byggs en Docker-image och skickas till Docker Hub. Sedan ber GitHub Actions Railway att starta om rätt backend-tjänst:

| Branch | Railway-tjänst | Miljö |
|---|---|---|
| `dev` | `backend-dev` | `dev` |
| `main` | `backend-main` | `production` |

Tester och driftsättning är separata flöden. En pull request kör tester men startar inte driftsättning.

## Live-länkar

Publika URL:er för backend är inte angivna.

- **Development:** Ingen URL angiven (Railway-tjänst: `https://number-guess-backend-dev.up.railway.app/players/3`, miljö `dev`)
- **Production:** Ingen URL angiven (Railway-tjänst: `https://ng-backend-main-production.up.railway.app/players/1`, miljö `production`)

### Github repository
- **Frontend**(https://github.com/moodyambr/number-guess-frontend.git)
- **Backend**(https://github.com/moodyambr/-number-guess-backend.git)

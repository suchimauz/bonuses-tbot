PGPORT    ?= 5429
PGHOST    ?= localhost
PGUSER     = postgres
PGDATABASE = postgres
PGPASSWORD = postgres

USER_URL  = https://breakingbad.top
BOT_TOKEN = 1362311778:AAHC79mhbWqDHlo4ouCvEA2kFJBZhOePJ2Q
BOT_FILES = /home/suchimauz/works/tbot/resources/bot/

.EXPORT_ALL_VARIABLES:

repl:
	lein repl

run:
	lein run

postgres-up:
	docker-compose up -d

postgres-down:
	docker-compose down

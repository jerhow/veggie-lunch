# veggie-lunch

A Slack command integration to help manage the vegetarian lunch orders for our team.

Basically, this is a headless Clojure backend, whose frontend is Slack via the custom command `/veggie-lunch`

Right now I'm backing this app with SQLite, but it can easily be tweaked to work with MySQL/MariaDB or PostgreSQL.

## Usage

Currently, in development I'm running it with this:

`$ lein run -m veggie-lunch.core`

In production, I'm building a .jar with Leiningen:

`$ lein uberjar`

...and deploying it on a lightweight VPS, behind a reverse proxy, over SSL.

Right now the app server is http-kit, but you should be able to drop in any other Ring-compliant server.

## Commands

The app is meant to be used like a command-line tool. As such, it currently supports 7 user commands and 9 additional admin commands:

`--about`
`--status` 
`--delete` 
`--help` 
`--list` 
`--menu`
`--order`
`--new-order` (admin only)
`--set-menu-url` (admin only)
`--lock` (admin only)
`--unlock` (admin only)
`--user-add` (admin only)
`--user-remove` (admin only)
`--user-list` (admin only)
`--user-perm` (admin only)
`--user-status` (admin only)

General help: 
`/veggie-lunch --help`

Specific help with a command:
*(for example, `--order`)*
`/veggie-lunch --help --order`

More docs to follow.

## License

Copyright © 2016 Jerry Howard

Distributed under the MIT License.

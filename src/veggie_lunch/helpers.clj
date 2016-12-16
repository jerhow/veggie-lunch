(ns veggie-lunch.helpers
    (:require [veggie-lunch.db.core :as db]
              [clojure.string :as str]
              [clj-time.core :as time-core]
              [clj-time.coerce :as time-coerce]
              [clj-time.local :as time-local]))

(def permitted-commands (set ["--none" 
                              "--about" 
                              "--status" 
                              "--delete" 
                              "--help" 
                              "--list" 
                              "--lock" 
                              "--menu" 
                              "--new-order" 
                              "--order" 
                              "--set-menu-url"
                              "--unlock"
                              "--user-add"
                              "--user-remove"
                              "--user-list"
                              "--user-perm"
                              "--user-status"]))

(def help-docs {:help           (str "Command: `/veggie-lunch --help [command]`\n"
                                     "Displays a general README about how to use the command")
                :list           (str "Command: `/veggie-lunch --list [date]`\n"
                                     "Lists the entire current order (defaults to current date) \n"
                                     "Provide an optional date to view a previous order (MM/DD/YYYY) \n")
                :order          (str "Command: `/veggie-lunch --order Whatever thing you are ordering`\n"
                                     "Writes your order to today's list.\n"
                                     "No quotes needed, and spaces are fine.\n"
                                     "Anything you write after the command is part of the order text.\n")
                :delete         (str "Command: `/veggie-lunch --delete`\n"
                                     "Removes your order from today's list\n")
                :menu           (str "Command: `/veggie-lunch --menu`\n"
                                     "Returns a link to the online menu of the place we are ordering from "
                                     "(if one exists).\n")
                :lock           (str "Command: `/veggie-lunch --lock`\n"
                                     "Locks today's order (Admins only)\n")
                :unlock         (str "Command: `/veggie-lunch --unlock`\n"
                                     "Unlocks today's order (Admins only)\n")
                :set-menu-url   (str "Command: `/veggie-lunch --unlock`\n"
                                     "Unlocks today's order (Admins only)\n")
                :user-add       (str "Command: `/veggie-lunch --user-add @slack-user full-name`\n"
                                     "Adds a user to the system, enabling them to order lunch (Admins only)\n")
                :user-remove    (str "Command: `/veggie-lunch --user-remove @slack-user`\n"
                                     "Removes a user from the system (Admins only)\n")
                :user-list      (str "Command: `/veggie-lunch --user-list`\n"
                                     "List all users the system (Admins only)\n")
                :user-perm      (str "Command: `/veggie-lunch --user-perm @slack-user [Admin | User]`\n"
                                     "(Updates a user's permission level\n"
                                     "Possible values are *Admin* or *User*\n")
                :user-status    (str "Command: `/veggie-lunch --user-status @slack-user [Active | Inactive]`\n"
                                     "Updates a user's status\n"
                                     "Possible values are *Active* or *Inactive*\n")
                :new-order      (str "Command: `/veggie-lunch --new-order vendor-name`\n"
                                     "Create today's list (Admins only)\n")})

(def emoji [":pig:" ":cow:" ":chicken:" ":rabbit:" ":octopus:" ":hatched_chick:"])

(defn current-time 
    "Returns the time for UI display"
    []
    ; TODO: I'm stuck in GMT for some reason. Must fix this!
    ; For now, I'm hacking it into being correct for the demo (see comment below)
    (let [time-24 (str/replace 
                (second (str/split (time-coerce/to-string (time-local/local-now))  #"T"))
                #"\.\d{3}Z"
                "")
          time-24-parts (str/split time-24 #"\:")
          hour (Integer/parseInt (first time-24-parts))
          minute (second time-24-parts)
          am-pm (if (> hour 12) "PM" "AM")
          hour-fixed (- (if (> hour 12) (- hour 12) hour) 5)] ; <-- The time-zone hack is here (subtracting 5)
        (str hour-fixed ":" minute " " am-pm)))

(defn dispatch 
  "Basically an internal router, since every request comes in on '/'. 
   We dynamically resolve the command's corresponding function name 
   from the 'command' argument. Invalid values for 'command' are
   screened out in 'home' prior to 'dispatch' being called."

  [request command]

  ((ns-resolve 'veggie-lunch.commands (symbol command)) request))

(defn fetch-user-id
    "Pass in a Slack user_name, get back their ID from the users table"
    [slack-user-name]
    (let [rows (db/fetch-user-existence {:slack_user_name slack-user-name})]
        (if (empty? rows)
            -1
            (:id (first rows)))))

(defn random-emoji
    ""
    []
    (rand-nth emoji))

(defn stringify-users-row 
    "Takes a row from the 'users' table and formats it for output in Slack"
    [row] 
    (str "\n\n" 
        "Slack User Name: *" (:slack_user_name row) "*\n"
        "Full Name: *" (:full_name row) "*\n"
        "Level: *" (:level row) "*\n"
        "Created: *" (:created_dt row) "*"))

(defn stringify-order-item-row
    "Takes a row from db/fetch-order-items and formats it for output in Slack"
    [row]
    (str "Name: " (:full_name row) " (@"  (:slack_user_name row) ")\n"
         "Item Requested: *" (:description row) "*\n\n"))

(defn split-command-text 
    ""
    [text-field]
    (let [text-parts (str/split text-field #" ")]
        (hash-map :command (first text-parts)
                  :slack-user-name (first (next text-parts))
                  :full-name (next (next text-parts)))))

(defn todays-date 
    "Returns the date as a YYYY-MM-DD formatted string"
    []
    (first (str/split (time-coerce/to-string (time-core/today))  #"T")))

(defn todays-order-id
    "Returns the int ID of today's order, or -1 if it doesn't exist"
    [order-date]
    (let [rows (db/fetch-order-existence {:order_date order-date})]
        (if (empty? rows)
            -1
            (:id (first rows)))))

(defn user-is-admin? 
    "Pass it a Slack user name, get back a boolean answer"
    [slack-user-name]
    (let [rows (db/fetch-user-as-admin {:slack_user_name slack-user-name})]
        (not (empty? rows))))

(defn user-exists?
    "Pass it a Slack user name, get back a boolean answer"
    [slack-user-name]
    (let [rows (db/fetch-user-existence {:slack_user_name slack-user-name})]
        (not (empty? rows))))

(defn order-exists?
    "Pass it a date string in YYYY-MM-DD format, get back a boolean answer"
    [order-date]
    (let [rows (db/fetch-order-existence {:order_date order-date})]
        (not (empty? rows))))

(ns veggie-lunch.helpers
    (:require [veggie-lunch.db.core :as db]
              [clojure.string :as str]
              [clj-time.core :as time-core]
              [clj-time.coerce :as time-coerce]))

(def permitted-commands (set ["" 
                              "--about" 
                              "--help" 
                              "--list" 
                              "--order" 
                              "--delete" 
                              "--menu" 
                              "--lock"
                              "--unlock"
                              "--set-menu-url"
                              "--user-add"
                              "--user-remove"
                              "--user-list"
                              "--user-perm"
                              "--user-status"
                              "--new-order"]))

(defn dispatch 
  "Basically an internal router, since every request comes in on '/'. 
   We dynamically resolve the command's corresponding function name 
   from the 'command' argument. Invalid values for 'command' are
   screened out in 'home' prior to 'dispatch' being called."

  [request command]

  ((ns-resolve 'veggie-lunch.commands (symbol command)) request))

(defn stringify-users-row 
    "Takes a row from the 'users' table and formats it for output in Slack"
    [row] 
    (str "\n\n" 
        "Slack User Name: " (:slack_user_name row) "\n"
        "Full Name: " (:full_name row) "\n"
        "Level: " (:level row) "\n"
        "Created: " (:created_dt row) "\n"
        "Status: " (:active row)))

(defn stringify-order-item-row
    "Takes a row from db/fetch-order-items and formats it for output in Slack"
    [row]
    (str "Name: " (:full_name row) " ("  (:slack_user_name row) ")\n"
         "Item Requested: " (:description row) "\n\n"))

(defn split-command-text 
    ""
    [text-field]
    (let [text-parts (str/split text-field #" ")]
        (hash-map :command (first text-parts)
                  :slack-user-name (first (next text-parts))
                  :full-name (next (next text-parts)))))

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

(defn fetch-user-id
    "Pass in a Slack user_name, get back their ID from the users table"
    [slack-user-name]
    (let [rows (db/fetch-user-existence {:slack_user_name slack-user-name})]
        (if (empty? rows)
            -1
            (:id (first rows)))))

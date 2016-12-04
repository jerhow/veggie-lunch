(ns veggie-lunch.helpers
    (:require [veggie-lunch.db.core :as db]
              [clojure.string :as str]))

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
                              "--list-users"]))

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
        "Name: " (:full_name row) "\n"
        "Created: " (:created_dt row) "\n"
        "Active: " (:active row)))

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

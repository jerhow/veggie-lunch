(ns veggie-lunch.helpers
    (:require [clojure.string :as str]))

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

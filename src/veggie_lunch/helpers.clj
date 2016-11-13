(ns veggie-lunch.helpers
    (:require [clojure.string :refer [blank?]]))

(defn stringify-users-row 
    "Takes a row from the 'users' table and formats it for output in Slack"
    [row] 
    (str "\n\n" 
        "Slack User Name: " (:slack_user_name row) "\n"
        "Name: " (:full_name row) "\n"
        "Created: " (:created_dt row) "\n"
        "Active: " (:active row)))

(ns veggie-lunch.commands
  (:require [veggie-lunch.db.core :as db]
            [veggie-lunch.helpers :as helpers]
            [ring.util.response :refer [response content-type status header]]
            [clojure.string :refer [split join]]))

(defn --about [request]
    (str "Veggie-Lunch version " (:app-version request)))

(defn --help [request]
    (str "TODO: Fill out the --help documentation"))

(defn --list [request]
    (str "TODO: --list"))

(defn --order [request]
    (str "TODO: --order"))

(defn --delete [request]
    (str "TODO: --delete"))

(defn --menu [request]
    (str "TODO: --menu"))

(defn --lock [request]
    (str "TODO: --lock"))

(defn --unlock [request]
    (str "TODO: --unlock"))

(defn --set-menu-url [request]
    (str "TODO: --set-menu-url"))

(defn --user-add 
    "Add a user to the system"
    [request]
    (let [slack-user-id (:user_id (:params request))
          slack-user-name (:user_name (:params request))
          command-text (:text (:params request))
          payload (helpers/split-command-text command-text)]
          (if (try (db/user-add! {
                :slack_user_name (:slack-user-name payload)
                :full_name (str(:full-name payload))}) (catch Exception e))
              "TODO: Build a 'success' response"
              "TODO: Build a 'not so much' response")))

(defn --user-remove [request]
    (str "TODO: --user-remove"))

(defn --user-list 
    "Fetches users from DB; returns results as a string, formatted for Slack"
    [request]
    (let [users (db/user-list)]
        (join (map helpers/stringify-users-row users))))

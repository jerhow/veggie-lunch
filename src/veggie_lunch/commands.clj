(ns veggie-lunch.commands
  (:require [veggie-lunch.db.core :as db]
            [veggie-lunch.helpers :as helpers]
            [ring.util.response :refer [response content-type status header]]
            [clojure.string :refer [split join]]))

; NOTE: Remember that all of the commands automatically get passed the 'request' map
; by the dispatcher, meaning you need to pick out anything you may need 
; from within your command's specific implementation.

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
    "Add a user to the system. Ensures that only Admin users can execute this command."
    [request]
    (let [op-user-id (:user_id (:params request))
          op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          payload (helpers/split-command-text command-text)
          slack-user-name (:slack-user-name payload)
          full-name (join " " (:full-name payload))]
          
          ; These nested if's are kind of gross. Perhaps I can figure out something more Clojure-idiomatic later.
          ; TODO: Refactor this.
          (if (helpers/user-is-admin? op-user-name)
            
            (if (try (db/user-add! {:slack_user_name slack-user-name :full_name full-name}) (catch Exception e))
              (str "User added: " slack-user-name " (" full-name ")\n:thumbs_up:")
              (str "Oops, something went wrong :disappointed: \n"
                "User " slack-user-name " (" full-name ") not added.\nThanks Obama :unamused:"))

            (str "Oops, only Admins can issue this command.\nThanks Obama :unamused:"))))

(defn --user-remove 
    "Delete a user from the system by Slack user name (@name)"
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          payload (helpers/split-command-text command-text)
          slack-user-name (:slack-user-name payload)
          full-name (join " " (:full-name payload))]
          
          ; Hmm, I thought the nested if's were gross in --user-add.
          ; This is even worse. This seems obviously non-idiomatic, but for now it works.
          ; TODO: Refactor this.
          (if (helpers/user-is-admin? op-user-name)

            (if (helpers/user-exists? slack-user-name)

                (if (try (db/user-remove! {:slack_user_name slack-user-name}) (catch Exception e))
                    (str "User " slack-user-name " removed\n:thumbs_up:")
                    (str "Oops, something went wrong :disappointed: \n"
                        "User " slack-user-name " not removed.\nThanks Obama :unamused:"))

                (str "Oops, this user doesn't exist, so there's nothing to remove.\nThanks Obama :unamused:"))

            (str "Oops, only Admins can issue this command.\nThanks Obama :unamused:"))))

(defn --user-list 
    "Fetches users from DB; returns results as a string, formatted for Slack"
    [request]
    (if (helpers/user-is-admin? (:user_name (:params request)))
        (let [users (db/user-list)] (join (map helpers/stringify-users-row users)))
        (str "Oops, only Admins can issue this command.\nThanks Obama :unamused:")))

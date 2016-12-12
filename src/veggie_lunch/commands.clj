(ns veggie-lunch.commands
  (:require [veggie-lunch.db.core :as db]
            [veggie-lunch.helpers :as helpers]
            [ring.util.response :refer [response content-type status header]]
            [clojure.string :refer [capitalize join lower-case split]]
            [clj-time.core :as time-core]
            [clj-time.coerce :as time-coerce]))

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

(defn --new-order 
    "Admin command. Initialize a new order in the system for today's date."
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          command-text-parts (split command-text #" ")
          vendor-name (join " " (rest command-text-parts))
          todays-date (first (split (time-coerce/to-string (time-core/today))  #"T"))]

          (if (helpers/user-is-admin? op-user-name)

            (if (helpers/order-exists? todays-date)
                (str "There is already an order in the system for today.\nThanks Obama :unamused:")
                (if (try (db/create-order! {:vendor_name vendor-name}) (catch Exception e))
                    (str "Today's order successfully added aww yea")
                    (str "Oops, something went wrong :disappointed: \n"
                        "New order not added.\nThanks Obama :unamused:")))

            (str "Oops, only Admins can issue this command.\nThanks Obama :unamused:"))))

(defn --user-add 
    "Admin command. Add a user to the system."
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          command-text-parts (split command-text #" ")
          slack-user-name (second command-text-parts)
          full-name (join " " (next (next command-text-parts)))]
          
          ; These nested if's are kind of gross. Perhaps I can figure out something more Clojure-idiomatic later.
          ; TODO: Refactor this.
          (if (helpers/user-is-admin? op-user-name)
            
            (if (try (db/user-add! {:slack_user_name slack-user-name :full_name full-name}) (catch Exception e))
              (str "User added: " slack-user-name " (" full-name ")\n:thumbs_up:")
              (str "Oops, something went wrong :disappointed: \n"
                "User " slack-user-name " (" full-name ") not added.\nThanks Obama :unamused:"))

            (str "Oops, only Admins can issue this command.\nThanks Obama :unamused:"))))

(defn --user-remove 
    "Admin command. Delete a user from the system by Slack user name (@name)"
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          command-text-parts (split command-text #" ")
          slack-user-name (second command-text-parts)]
          
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
    "Admin command. Fetches users from DB; returns results as a string, formatted for Slack"
    [request]
    (if (helpers/user-is-admin? (:user_name (:params request)))
        (let [users (db/user-list)] (join (map helpers/stringify-users-row users)))
        (str "Oops, only Admins can issue this command.\nThanks Obama :unamused:")))

(defn --user-perm 
    "Admin command. Updates a user's (permission) level by Slack @id. 
     Allowed values for new-level are: 'User' or 'Admin' (case-insensitive). 
     If new-level is omitted, this function will default to 'User'."
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          command-text-parts (split command-text #" ")
          slack-user-name (nth command-text-parts 1 "")
          level-whitelist #{"admin" "user"} ; <- a set
          new-level (nth command-text-parts 2 "User")
          new-level-int (if (= (lower-case new-level) "admin") 2 1)]
          
          ; TODO: If you're going to refactor the cases above, you must do this one,
          ; as we're up to 4 levels (!) of nested if's. Barf.
          (if (helpers/user-is-admin? op-user-name)

            (if (contains? level-whitelist (lower-case new-level))

                (if (helpers/user-exists? slack-user-name)

                    (if (try (db/user-perm! {:slack_user_name slack-user-name :level new-level-int}) (catch Exception e))
                        (str "User " slack-user-name " changed to " (capitalize new-level) "\n:thumbs_up:")
                        (str "Oops, something went wrong :disappointed: \n"
                            "User " slack-user-name " was not changed.\nThanks Obama :unamused:"))

                    (str "Oops, this user doesn't exist, so there's nothing to remove.\nThanks Obama :unamused:"))

                (str "Oops, you can't change a user to that.\nThanks Obama :unamused:"))

            (str "Oops, only Admins can issue this command.\nThanks Obama :unamused:"))))

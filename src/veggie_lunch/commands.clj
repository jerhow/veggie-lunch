(ns veggie-lunch.commands
  (:require [veggie-lunch.db.core :as db]
            [veggie-lunch.helpers :as helpers]
            [ring.util.response :refer [response content-type status header]]
            [clojure.string :as str]
            [selmer.parser :refer [render-file]]
            [selmer.util :refer [without-escaping]]))

; NOTE: Remember that all of the commands automatically get passed the 'request' map
; by the dispatcher, meaning you need to pick out anything you may need 
; from within your command's specific implementation.

(def ftn helpers/fix-tmpl-newlines) ; since we use this function all over the place, let's alias it to something short

; ================
; User commands:
; ================
(defn --about 
    "Standard 'About this app' info for the user"
    [request]
    (let [command-text (:text (:params request))
          tmpl-path (helpers/tmpl-path (str/split command-text #" "))
          emoji (helpers/random-emoji)]
        (ftn (render-file tmpl-path {:cmd-text command-text :emoji emoji :version (:app-version request)}))))

(defn --delete 
    "How a user removes their item from the current order."
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          tmpl-path (helpers/tmpl-path (str/split command-text #" "))
          emoji (helpers/random-emoji)]

        (if (helpers/order-exists? (helpers/todays-date))
            (if (try (db/delete-order-item! {:slack_user_name op-user-name :order_date (helpers/todays-date)}) 
                    (catch Exception e))
                (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "200"}))
                (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "500"})))

            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "404"})))))

(defn --help 
    "Parses out the command for which help is being requested, 
     and returns that entry from the helpers/help-docs hashmap.
     If no command is passed in, we just output the top-level
     messaging, which lists the available commands and explains
     how to get help for them."
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          command-text-parts (str/split command-text #" ")
          requested-help-command (nth command-text-parts 1 "--none")
          tmpl-path (helpers/tmpl-path command-text-parts)
          emoji (helpers/random-emoji)]

        (if (= requested-help-command "--none")
            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "404"
                :available-commands (str/join "\n" (sort helpers/permitted-commands))}))
            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "200"
                :help-text ((keyword (subs requested-help-command 2)) helpers/help-docs)})))))

(defn --list 
    "List out the requested items in a given order.
     If no argument is passed in, we default to the current date.
     Alternatively, if a YYYY-MM-DD formatted date string is passed in,
     we will fetch the order from that date (if one exists)."
    [request]        
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          command-text-parts (str/split command-text #" ")
          order-date (if (nil? (second command-text-parts)) (helpers/todays-date) (second command-text-parts))
          rows (try (db/fetch-order-items {:order_date order-date}) (catch Exception e))
          vendor-name (:vendor_name (first rows))
          menu-url (:menu_url (first rows))
          status (:status (first rows))
          order-items (str/join (map helpers/stringify-order-item-row rows))
          tmpl-path (helpers/tmpl-path command-text-parts)
          emoji (helpers/random-emoji)]

        (if (helpers/order-exists? order-date)
            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "200"
                :order-date order-date :vendor-name vendor-name :menu-url menu-url :status status
                :order-items order-items :row-count (count rows)}))
            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "404"})))))

(defn --menu 
    "Returns the menu URL (or default value) from the current order"
    [request]
    (if (helpers/order-exists? (helpers/todays-date))
        (let [result (db/fetch-menu-url {:order_date (helpers/todays-date)})]
            (:menu_url (first result)))))

(defn --none
    "This command stands in when we have not been passed a legit command.
     We just pass the request along to --help, and return the top-level messaging."
    [request]
    (--help request))

(defn --order 
    "How a user adds an item to the current order."
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          command-text-parts (str/split command-text #" ")
          order-text (str/join " " (rest command-text-parts))
          slack-user-name (helpers/fetch-user-id op-user-name)
          order-id (helpers/todays-order-id (helpers/todays-date))
          tmpl-path (helpers/tmpl-path command-text-parts)
          emoji (helpers/random-emoji)]

        (if (helpers/order-exists? (helpers/todays-date))
            (if (try (db/upsert-order-item! 
                {:user_id slack-user-name :order_id order-id :order_text order-text}) (catch Exception e))
                (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "200"}))
                (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "500"})))

            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "404"})))))

(defn --status
    "Find out what's going on in the system at a given moment.
     Right now we're just telling the user whether there is a list
     going yet today."
    [request]
    (let [command-text (:text (:params request))
          todays-date (helpers/todays-date)
          tmpl-path (helpers/tmpl-path (str/split command-text #" "))
          emoji (helpers/random-emoji)]
        (if (helpers/order-exists? todays-date)
            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "200"}))
            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "404"})))))

; ================
; Admin commands:
; ================
(defn --lock 
    "Admin command. Locks the current order."
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          tmpl-path (helpers/tmpl-path (str/split command-text #" "))
          emoji (helpers/random-emoji)]
        (if (helpers/user-is-admin? op-user-name)

            (if (helpers/order-exists? (helpers/todays-date))

                (if (try (db/lock-order! {:order_date (helpers/todays-date)}) (catch Exception e))
                    (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "200"}))
                    (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "500"})))

                (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "404"})))

            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "403"})))))

(defn --unlock 
    "Admin command. Unlocks the current order."
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          tmpl-path (helpers/tmpl-path (str/split command-text #" "))
          emoji (helpers/random-emoji)]
        (if (helpers/user-is-admin? op-user-name)

            (if (helpers/order-exists? (helpers/todays-date))

                (if (try (db/unlock-order! {:order_date (helpers/todays-date)}) (catch Exception e))
                    (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "200"}))
                    (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "500"})))

                (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "404"})))

            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "403"})))))

(defn --set-menu-url 
    "Admin command. Set the menu URL for today's order."
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          command-text-parts (str/split command-text #" ")
          url (str/join " " (rest command-text-parts))
          tmpl-path (helpers/tmpl-path command-text-parts)
          emoji (helpers/random-emoji)]

      (if (helpers/user-is-admin? op-user-name)

        (if (helpers/order-exists? (helpers/todays-date))

            (if (try (db/set-menu-url! {:url url :order_date (helpers/todays-date)}) (catch Exception e))
                (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "200"}))
                (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "500"})))

            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "404"})))

        (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "403"})))))

(defn --new-order 
    "Admin command. Initialize a new order in the system for today's date."
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          command-text-parts (str/split command-text #" ")
          tmpl-path (helpers/tmpl-path command-text-parts)
          vendor-name (str/join " " (rest command-text-parts))
          emoji (helpers/random-emoji)]

        (if (helpers/user-is-admin? op-user-name)

            (if (= vendor-name "")
                (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "404"}))

                (if (helpers/order-exists? (helpers/todays-date))
                    (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "409"}))

                    (if (try (db/create-order! {:vendor_name vendor-name}) (catch Exception e))
                        (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "200"}))
                        (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "500"})))))

            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "403"})))))

(defn --user-add 
    "Admin command. Add a user to the system."
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          command-text-parts (str/split command-text #" ")
          slack-user-name (str/replace (second command-text-parts) #"^\@" "")
          full-name (str/join " " (next (next command-text-parts)))
          tmpl-path (helpers/tmpl-path command-text-parts)
          emoji (helpers/random-emoji)]
          
        (if (helpers/user-is-admin? op-user-name)
        
            (if (try (db/user-add! {:slack_user_name slack-user-name :full_name full-name}) (catch Exception e))
                (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "200" 
                    :slack-user-name slack-user-name :full-name full-name}))
                (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "500" 
                    :slack-user-name slack-user-name :full-name full-name})))

            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "403"})))))

(defn --user-remove 
    "Admin command. Delete a user from the system by Slack user name (@name)"
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          command-text-parts (str/split command-text #" ")
          slack-user-name (str/replace (second command-text-parts) #"^\@" "")
          tmpl-path (helpers/tmpl-path command-text-parts)
          emoji (helpers/random-emoji)]
          
        (if (helpers/user-is-admin? op-user-name)

            (if (helpers/user-exists? slack-user-name)

                (if (try (db/user-remove! {:slack_user_name slack-user-name}) (catch Exception e))
                    (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "200" 
                        :slack-user-name slack-user-name}))
                    (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "500" 
                        :slack-user-name slack-user-name})))

                (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "404" 
                    :slack-user-name slack-user-name})))

            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "403" 
                        :slack-user-name slack-user-name})))))

(defn --user-list 
    "Admin command. Fetches users from DB; returns results as a string, formatted for Slack"
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          tmpl-path (helpers/tmpl-path (str/split command-text #" "))
          emoji (helpers/random-emoji)]
        
        (if (helpers/user-is-admin? op-user-name)
            (ftn (without-escaping (render-file tmpl-path {
                :emoji emoji :cmd-text command-text :tmpl-block "200" 
                :rows (str/join (map helpers/stringify-users-row (db/user-list)))})))
            
            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "403"})))))

(defn --user-status
    "Admin command. Update the status of a user in the DB."
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          command-text-parts (str/split command-text #" ")
          slack-user-name (str/replace (nth command-text-parts 1 "") #"^\@" "") ;; leading '@' is optional, so drop it
          status-whitelist #{"active" "inactive"} ; <- a set
          new-status (nth command-text-parts 2 "active")
          new-status-int (if (= (str/lower-case new-status) "active") 1 0)
          tmpl-path (helpers/tmpl-path command-text-parts)
          emoji (helpers/random-emoji)]

        (if (helpers/user-is-admin? op-user-name)
        
            (if (contains? status-whitelist (str/lower-case new-status))

                (if (helpers/user-exists? slack-user-name)

                    (if (try (db/update-user-status! {:slack_user_name slack-user-name :active_status new-status-int}) 
                        (catch Exception e))
                        (ftn (render-file tmpl-path {
                            :emoji emoji :cmd-text command-text :tmpl-block "200" :slack-user-name slack-user-name 
                            :new-status (str/capitalize new-status)}))
                        (ftn (render-file tmpl-path {
                            :emoji emoji :cmd-text command-text :tmpl-block "500" :slack-user-name slack-user-name})))

                    (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "404"})))

                (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "406"})))

            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "403"})))))

(defn --user-perm 
    "Admin command. Updates a user's (permission) level by Slack @id. 
     Allowed values for new-level are: 'User' or 'Admin' (case-insensitive). 
     If new-level is omitted, this function will default to 'User'."
    [request]
    (let [op-user-name (:user_name (:params request))
          command-text (:text (:params request))
          command-text-parts (str/split command-text #" ")
          slack-user-name (str/replace (nth command-text-parts 1 "") #"^\@" "") ;; leading '@' is optional, so drop it
          level-whitelist #{"admin" "user"} ; <- a set
          new-level (nth command-text-parts 2 "User")
          new-level-int (if (= (str/lower-case new-level) "admin") 2 1) ;; 1 for 'User', 2 for 'Admin'
          tmpl-path (helpers/tmpl-path command-text-parts)
          emoji (helpers/random-emoji)]
          
        (if (helpers/user-is-admin? op-user-name)

            (if (contains? level-whitelist (str/lower-case new-level))

                (if (helpers/user-exists? slack-user-name)

                    (if (try (db/user-perm! {:slack_user_name slack-user-name :level new-level-int}) 
                        (catch Exception e))
                        (ftn (render-file tmpl-path {
                            :emoji emoji :cmd-text command-text :tmpl-block "200" :slack-user-name slack-user-name 
                            :new-level (str/capitalize new-level)}))
                        (ftn (render-file tmpl-path {
                            :emoji emoji :cmd-text command-text :tmpl-block "500" :slack-user-name slack-user-name})))

                    (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "404"})))

                (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "406"})))

            (ftn (render-file tmpl-path {:emoji emoji :cmd-text command-text :tmpl-block "403"})))))

(ns veggie-lunch.controllers
  (:require [veggie-lunch.commands :as commands]
            [veggie-lunch.helpers :refer [dispatch permitted-commands user-exists?]]
            [ring.util.response :refer [response content-type status header]]
            [clojure.string :as str]))

(defn home 
  "Handler for the home route (which is the only route we can have 
  because of the way that Slack commands work)"

  [request]

  (let [op-user-name (:user_name (:params request))
        text-field (:text (:params request))
        text-parts (str/split text-field #" ")
        command (first text-parts)]
    
    (if (user-exists? op-user-name)

      (if (contains? permitted-commands command)
        (response (dispatch request command))
        (header (response text-field) "status" "500 Error"))

      (str "Oops, this user doesn't exist in our system.\n"
        "Please see an admin to be added.\n"
        "Thanks Obama :unamused:"))))

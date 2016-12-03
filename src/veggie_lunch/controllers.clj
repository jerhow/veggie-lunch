(ns veggie-lunch.controllers
  (:require [veggie-lunch.commands :as commands]
            [veggie-lunch.helpers :refer [dispatch permitted-commands]]
            [ring.util.response :refer [response content-type status header]]
            [clojure.string :as str]))

(defn home 
  "Handler for the home route (which is the only route we can have 
  because of the way that Slack commands work)"

  [request]

  (let [text-field (:text (:params request))
        text-parts (str/split text-field #" ")
        command (first text-parts)
        command-no-dashes (str/replace command #"^\-\-" "")]
    (if (contains? permitted-commands command)
      (response (dispatch request command))
      (header (response text-field) "status" "500 Error"))))

(defn home-ORIG [request]
  (str "Veggie-Lunch version " (:app-version request) 
    "<br /><br />"
    "Add query string param 'foo' to test:<br />"
    "foo=" (:foo (:params request))))

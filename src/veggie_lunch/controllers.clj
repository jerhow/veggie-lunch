(ns veggie-lunch.controllers
  (:require [veggie-lunch.commands :as commands]
            [ring.util.response :refer [response content-type status header]]
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

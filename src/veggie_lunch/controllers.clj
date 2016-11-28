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
  Once I figure out how to resolve/invoke a function from a string (without using eval),
  I will replace this hard-coded case block with something more elegant.
  
  NOTE: I'm working through some of the ideas here:
  http://stackoverflow.com/questions/3407921/clojure-resolving-function-from-string-name
  ...and while I can get it working in the REPL, I haven't quite gotten the app to cooperate yet."

  [request command]
  
  (case command
    "--about" (commands/--about request)
    "--help" (commands/--help request)
    "--list" (commands/--list request)
    "--order" (commands/--order request)
    "--delete" (commands/--delete request)
    "--menu" (commands/--menu request)
    "--lock" (commands/--lock request)
    "--unlock" (commands/--unlock request)
    "--set-menu-url" (commands/--set-menu-url request)
    "--user-add" (commands/--user-add request)
    "--user-remove" (commands/--user-remove request)
    "--user-list" (commands/--user-list request)
    ""))

(defn home 
  "Handler for the home route (which is the only route we can have 
  because of the way that Slack commands work)"

  [request]

  (let [text-field (:text (:params request))
        text-parts (str/split text-field #" ")
        command (first text-parts)
        command-no-dashes (str/replace command #"^\-\-" "")]
    (if (contains? permitted-commands command)
      (dispatch request command)
      (header (response text-field) "status" "500 Error"))))

(defn home-ORIG [request]
  (str "Veggie-Lunch version " (:app-version request) 
    "<br /><br />"
    "Add query string param 'foo' to test:<br />"
    "foo=" (:foo (:params request))))

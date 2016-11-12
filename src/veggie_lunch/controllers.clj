(ns veggie-lunch.controllers
  (:require [ring.util.response :refer [response content-type status header]]
            [clojure.string :refer [split]]))

(def permitted-commands (set ["" 
                              "--help" 
                              "--list" 
                              "--order" 
                              "--delete" 
                              "--menu" 
                              "--lock"
                              "--unlock"
                              "--set-menu-url"
                              "--user-add"
                              "--user-remove"]))

; ========================================
; HTTP route handlers
(defn home [request]
  (let [text-field (:text (:params request))
        text-parts (split text-field #" ")
        command (first text-parts)]
    (println command)
    (println (contains? permitted-commands command))
    
    (if (contains? permitted-commands command)
      (response (str "\"" command "\"" " valid command"))
      (response "Error: Sorry, I don't recognize that command."))
      ; (header (response text-field) "status" "200 OK")
      ; (header (response text-field) "status" "500 Error"))
  )
)

(defn home-ORIG [request]
  (str "Veggie-Lunch version " (:app-version request) 
    "<br /><br />"
    "Add query string param 'foo' to test:<br />"
    "foo=" (:foo (:params request))))

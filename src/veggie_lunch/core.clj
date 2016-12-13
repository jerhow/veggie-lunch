(ns veggie-lunch.core
  (:gen-class)
  (:require [veggie-lunch.middleware :as middleware]
            [veggie-lunch.controllers :as controllers]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.reload :as reload]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.params :refer :all]
            [org.httpkit.server :refer [run-server]]))

(defroutes all-routes
    (POST "/" [request] controllers/home))

(defn -main 
    "The entry point from which the app is launched by the JVM.
     We launch the app from Leiningen by calling 'run' and passing
     in the 'core' namespace:

     $ lein run -m veggie-lunch.core

     NOTE: This is the idiomatic way of applying the various middleware pieces,
     using the -> macro. It works, and all of the middleware still functions correctly;
     however I still must get a better understanding of how this works. I basically
     just lined the middleware from inside to out based on the original -main implementation."
    
    []
    (run-server
        (-> all-routes
            (wrap-defaults api-defaults)
            (wrap-params)
            (middleware/wrap-version)
            ; (middleware/wrap-spy)
            (reload/wrap-reload)
            ) {:port 5000}))

; ===============================================================================
; === Not currently in use
(defn -main-ORIGINAL 
    "This is the non-idiomatic way of applying the various middleware pieces.
     It works, but the nesting gets a little crazy."

    []
    (run-server 
        (reload/wrap-reload 
            (middleware/wrap-spy 
                (middleware/wrap-version
                    (wrap-defaults #'all-routes site-defaults)))) {:port 5000}))
    ; (run-server (reload/wrap-reload #'all-routes) {:port 5000}))

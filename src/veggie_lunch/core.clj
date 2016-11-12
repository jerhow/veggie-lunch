(ns veggie-lunch.core
  (:require [veggie-lunch.middleware :as middleware]
            [veggie-lunch.controllers :as controllers]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.reload :as reload]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.params :refer :all]
            [org.httpkit.server :refer [run-server]]))

(defroutes all-routes
    (GET "/" [request] controllers/home))

(defn -main []
    "This is the idiomatic way of applying the various middleware pieces,
     using the -> macro. It works, and all of the middleware still functions correctly;
     however I still must get a better understanding of how this works. I basically
     just lined the middleware from inside to out based on the original -main implementation."
    (run-server
        (-> all-routes
            (wrap-defaults site-defaults)
            (wrap-params)
            (middleware/wrap-version)
            ; (middleware/wrap-spy)
            (reload/wrap-reload)
            ) {:port 5000}))

; ===============================================================================
; === Not currently in use
(defn -main-ORIGINAL []
    "This is the non-idiomatic way of applying the various middleware pieces.
     It works, but the nesting gets a little crazy."
    (run-server 
        (reload/wrap-reload 
            (middleware/wrap-spy 
                (middleware/wrap-version
                    (wrap-defaults #'all-routes site-defaults)))) {:port 5000}))
    ; (run-server (reload/wrap-reload #'all-routes) {:port 5000}))

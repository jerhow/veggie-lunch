(ns veggie-lunch.db.core
    (:require [clojure.java.jdbc :as sql])
    (:require [yesql.core :refer [defqueries]]))

(def db-spec {
    :classname "org.sqlite.JDBC"
    :subprotocol "sqlite"
    :subname "resources/db/veggie-lunch.sqlite3"}) ;; creates DB if not already present

(defqueries "queries.sql"
   {:connection db-spec})

; (defn get-all-names []
;     (try
;         (let [results (get-names)]
;             (cond
;                 (empty? results) {:status 404}
;                 :else results))
;     (catch Exception e 
;         (println e))))

(defn bootstrap []
    (create-table-users!)
    (populate-users!))

(defn raw-fetch-query [query]
    (clojure.java.jdbc/query db-spec [query]))

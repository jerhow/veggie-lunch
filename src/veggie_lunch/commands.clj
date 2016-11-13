(ns veggie-lunch.commands
  (:require [ring.util.response :refer [response content-type status header]]
            [clojure.string :refer [split]]))

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

(defn --user-add [request]
    (str "TODO: --user-add"))

(defn --user-remove [request]
    (str "TODO: --user-remove"))

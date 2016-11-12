(ns veggie-lunch.commands
  (:require [ring.util.response :refer [response content-type status header]]
            [clojure.string :refer [split]]))

(defn help []
    (str "TODO: Fill out the --help documentation"))
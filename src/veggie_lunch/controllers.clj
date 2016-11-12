(ns veggie-lunch.controllers)

; ========================================
; HTTP route handlers
(defn home [request]
    (str "Veggie-Lunch version " (:app-version request) 
      "<br /><br />"
      "Add query string param 'foo' to test:<br />"
      "foo=" (:foo (:params request))))

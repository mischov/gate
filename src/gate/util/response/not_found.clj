(ns gate.util.response.not-found)


(defn return-404
  [request settings]
  
  (let [handler (get settings :404-handler "404: Not Found")]
    {:status 404
      :headers {"content-type" "text/html; charset=utf-8"}
      :body (if (fn? handler)
              (handler request)
              handler)}))

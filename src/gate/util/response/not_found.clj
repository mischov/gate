(ns gate.util.response.not-found)


(defn return-404
  [request settings]
  
  (let [handler (get settings :not-found "404: Not Found")]
    {:status 404
     :headers {"content-type" "text/html; charset=utf-8"}
     :body (if (fn? handler)
             (handler request)
             handler)}))

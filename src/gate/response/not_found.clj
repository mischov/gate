(ns gate.response.not-found)

(defn issue-404
  [handler request]
   (let [handler (if (fn? handler) (handler request) handler)]
     {:status 404
      :headers {"content-type" "text/html; charset=utf-8"}
      :body handler}))

(defn add-not-found
  [request settings]
  (let [on-404 (get settings :on-404 "404: Not Found")]
    (assoc request :not-found (issue-404 on-404 request))))

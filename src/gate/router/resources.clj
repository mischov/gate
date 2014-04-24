(ns gate.router.resources
  (:require [clojure.string :as string]
            [ring.util.mime-type :refer [ext-mime-type]]
            [ring.util.response :refer [resource-response
                                        get-header]]))


(defn ^:private expand-uri
  [uri resource-settings]
  (let [default-settings {:path "/" :root "public"}
        {:keys [path root]} (merge default-settings resource-settings)]
    (str root "/" (string/replace-first uri path ""))))


(defn ^:private set-content-type
  [response content-type]
  (assoc-in response [:headers "Content-Type"] content-type))


(defn ^:private add-content-type
  "If response has no Content-Type header, attempts
   to determine content type from the request's uri
   and adds that type to the response.

   Defaults to 'application/octet-stream'."
  [response uri additional-mime-types]
  (if (get-header response "Content-Type")
    response
    (let [mime-type (ext-mime-type uri additional-mime-types)]
      (set-content-type response (or mime-type
                                     "application/octet-stream")))))


(defn ^:private search-for-resource-matching
  [request resource-settings]
  (let [uri (get request :uri)
        additional-types (get resource-settings :mime-types)
        possible-resource (re-find #"\.[A-Za-z]+" uri)]
    (when possible-resource
        (let [resource-uri (expand-uri uri resource-settings)
              response (resource-response resource-uri)]
          (when response
            (add-content-type response uri additional-types))))))


(defn get-resource-matching
  "When there are resource settings, attempts to find a
   resource matching request."
  [request settings]
  (when-let [resource-settings (get settings :resources)]
    (search-for-resource-matching request resource-settings)))

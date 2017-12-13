(ns dashboard.db
  (:require
   [re-frame.core :refer [reg-cofx]]))


(def default-db
  {:active {:page :login}})

(defn token->local-store
  "Puts todos into localStorage"
  [token]
  (.setItem js/localStorage "token" token))

(reg-cofx
  :local-store-token
  (fn [cofx _]
    (assoc cofx :local-store-token
      (.getItem js/localStorage "token"))))

(ns dashboard.event
  (:require [dashboard.inflater :as inflater]
            [dashboard.transformer :as transformer]
            [taoensso.timbre :as timbre :refer (tracef debugf infof warnf errorf)]
            [clojure.spec.alpha :as s]))

(timbre/set-level! :trace) 

(s/def ::time integer?)
(s/def ::label keyword?)
(s/def ::value number?)
(s/def ::event (s/keys :req-un [::time ::label ::value]))

;; atom holding all the state as events
(def events (atom []))

(defn epoch->date
  [millis]
  (str (java.util.Date. millis)))

(defn- post->event
  [post]
  (debugf "Received raw post: %s" post)
  {:time (or (post :time) (System/currentTimeMillis))
   :label (keyword (first (keys post)))
   :value (read-string (first (vals post)))}) 

(defn- process-data
  [data [time value]]
  [(conj (or (first data) []) time)
   (conj (or (second data) []) value)])


(defn- process-content
  [{:keys [time label value]} state]
  (-> state
      (update-in [:content label :data] process-data [(epoch->date time) value])
      (assoc-in [:content label :meta :labels] [:time label])))

(defn- process-config
  [{:keys [time label value]} state]
  (assoc-in state [:config label :type] :line))

(defn- process-event
  [event state]
  (->> state
      (process-content event)
      (process-config event)))

(defn- process-events
  ([events]
   (process-events events {}))
  ([to-process processed]
   (if (empty? to-process)
     processed
     (process-events (rest to-process) (process-event (first to-process) processed)))))

(defn- make-renderable
  [data]
  (-> data
      (inflater/inflate)
      (transformer/transform)))

(defn- store-event!
  [event]
  (swap! events conj event)
  (tracef "Stored event: %s" event)
  (make-renderable (process-events @events)))

(defn store-post!
  [post broadcast-state]
  (let [event (post->event post)]
    (if (s/valid? ::event event)
      (broadcast-state (store-event! event))
      (throw (IllegalArgumentException. (str "Tried to store malformed event: " event))))))

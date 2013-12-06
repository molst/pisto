(ns pisto.core "Formalization of the lifecycle of stateful systems.")

(defmulti start-part "Delegates the starting of the system part to anyone that knows how to start it by type. 'part-entry' is on the form [type part] where type is a keyword and part is a map. Returns a part state on success (may be nil)."
  (fn [part-entry] (key part-entry)))

(defmulti stop-part "Delegates the stopping of the system part to anyone that knows how to stop it by type. 'part-entry' is on the form [type part] where type is a keyword and part is a map. Returns a part state on success (may be nil)."
  (fn [part-entry] (key part-entry)))

(defn do-to-part [system f [type part :as part-entry]] (assoc-in system [:parts type] (assoc part :state (f part-entry))))

(defn set-life-cycle-state [system part-type life-cycle-state] (assoc-in system [:parts part-type :life-cycle-state] life-cycle-state))

(defn add-error [system part-type error] (update-in system [:parts part-type :errors] #(conj % error)))

(defn stopped? [{:keys [life-cycle-state]}]
  (or (= :stopped life-cycle-state)
      (= nil life-cycle-state)))

(defn started? [part] (not (stopped? part)))

(defn try-to-start-part [system [type part :as part-entry]]
  (if (stopped? part)
    (-> (do-to-part system start-part part-entry)
        (set-life-cycle-state type :started))
    (add-error system type {:type :life-cycle :message "Skipped starting an already started part."})))

(defn try-to-stop-part  [system [type part :as part-entry]]
  (if (started? part)
    (-> (do-to-part system stop-part part-entry)
        (set-life-cycle-state type :stopped))
    (add-error system type {:type :life-cycle :message "Skipped stopping an already stopped part."})))

(defn start-parts "Starts all parts of the system using 'start-part'. Returns a new non-nil system on success."
  [system] (reduce try-to-start-part system (sort-by #(.indexOf (:start-order system) (first %)) (seq (:parts system)))))

(defn stop-parts "Stops all parts of the system using 'stop-part'. Returns a new non-nil system on success."
  [system] (reduce try-to-stop-part system (sort-by #(.indexOf (reverse (:start-order system)) (first %)) (seq (:parts system)))))

(defn clear-stateful-info [part] (dissoc part :errors :state))

(defn clear-all-stateful-info [system]
  (reduce (fn [sys [type part]] (update-in sys [:parts type] clear-stateful-info)) system (seq (:parts system))))

(defn restart-parts [system]
  (-> (stop-parts system)
      (clear-all-stateful-info)
      (start-parts)))

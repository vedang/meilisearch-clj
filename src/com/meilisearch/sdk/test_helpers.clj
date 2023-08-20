(ns com.meilisearch.sdk.test-helpers
  (:require [com.meilisearch.sdk.core :as core]))

(defn wait-for-task-to-complete
  ([client task-info]
   (wait-for-task-to-complete client task-info 5))
  ([client task-info num-attempts]
   (let [task (core/get-task client (:task-uid task-info))]
     (when (not= "succeeded" (:status task))
       (if (pos? num-attempts)
         (do (Thread/sleep 500)
             (recur client task-info (dec num-attempts)))
         (do (println "Exhausted attempts, something is wrong with our server")
             (println task)))))))

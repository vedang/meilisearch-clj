(ns user
  #:nextjournal.clerk{:visibility {:code :show, :result :show}, :toc true}
  (:require
   [com.meilisearch.sdk.core :as core]
   [hyperfiddle.rcf :as rcf]))

(rcf/enable!)
(comment (set! *warn-on-reflection* true))

;; # 🚀 Getting started

;; ## Add documents
(def documents
  [{:id 1 :title "Carol" :genres ["Romance" "Drama"]}
   {:id 2 :title "Wonder Woman" :genres ["Action" "Adventure"]}
   {:id 3 :title "Life of Pi" :genres ["Drama" "Adventure"]}
   {:id 4 :title "Mad Max: Fury Road" :genres ["Science Fiction" "Adventure"]}
   {:id 5 :title "Moana" :genres ["Action" "Fantasy"]}
   {:id 6 :title "Philadelphia" :genres ["Drama"]}])

(rcf/tests
 (def task-info "Store this to demonstrate the get-task function later"
       ;; Client Configuration
   (-> {:host "http://localhost:7700"
        :api-key "aWildSalherApplicationAppears"}
       ;; Create a new client
       core/client!
       ;; Get the index where we plan to store the documents
       (core/index "movies")
       ;; If the index 'movies' does not exist, Meilisearch creates it
       ;; when you first add the documents.
       (core/add-documents! documents)))

 (select-keys task-info [:status :index-uid :type])
 := {:status "enqueued", :index-uid "movies", :type "documentAdditionOrUpdate"})

;; With the `task-uid`, you can check the status (`enqueued`,
;; `processing`, `succeeded` or `failed`) of your documents ongoing
;; addition using the [task
;; endpoint](https://www.meilisearch.com/docs/reference/api/tasks).

;; Continuing on from previous example, let's formalize things a bit
;; and see the status of our task.

(def config "Configuration to connect to our Meilisearch Client"
  {:host "http://localhost:7700"
   :api-key "aWildSalherApplicationAppears"})

(def client "A Meilisearch Client Instance for the rest of our examples"
  (core/client! {:host "http://localhost:7700"
                 :api-key "aWildSalherApplicationAppears"}))

#_{:clj-kondo/ignore [:unused-value]}
(rcf/tests
 (let [task (core/get-task client (:task-uid task-info))]
   (boolean (some #{(:status task)} ["processing" "succeeded"]))
   := true

   (select-keys task [:type :index-uid])
   := {:type "documentAdditionOrUpdate" :index-uid "movies"}))

;; ## Basic Search

;; A basic search can be performed by calling the `search` method,
;; with a simple string query.

;; First, let's make sure our documents have been added, and wait for
;; the task to succeed.

(rcf/tests
 (loop [num-attempts 5]
   (let [task (core/get-task client (:task-uid task-info))]
     (when (not= "succeeded" (:status task))
       (if (pos? num-attempts)
         (do (Thread/sleep 500)
             (recur (dec num-attempts)))
         (do (println "Exhausted attempts, something is wrong with our server")
             (println task)))))))

;; Now we are ready to search

(def index "The Movies Index against which we want to execute a search"
  (core/index client "movies"))

(rcf/tests
 (core/search index "life")
 := {:hits '({:genres ["Drama" "Adventure"],
             :id 3.0,
             :title "Life of Pi"}),
    :facet-distribution nil,
    :processing-time-ms 0,
    :query "life",
    :offset 0,
    :limit 20,
    :estimated-total-hits 1})

(ns user
  #:nextjournal.clerk{:visibility {:code :show, :result :show}, :toc true}
  (:require
   [com.meilisearch.sdk.core :as core]
   [com.meilisearch.sdk.test-helpers :refer [wait-for-task-to-complete]]
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

(rcf/tests (wait-for-task-to-complete client task-info))

;; Now we are ready to search

(def index "The Movies Index against which we want to execute a search"
  (core/index client "movies"))

(rcf/tests
 (-> index
     (core/search "life")
     (select-keys [:hits :query :estimated-total-hits]))
 := {:hits '({:genres ["Drama" "Adventure"],
             :id 3.0,
             :title "Life of Pi"}),
    :query "life",
    :estimated-total-hits 1})

;; ## Custom Search

;; If you want a custom search, the easiest way is to create a
;; `SearchRequest` object, and set the parameters that you need. All
;; the supported options are described in the [search
;; parameters](https://www.meilisearch.com/docs/reference/api/search#search-parameters)
;; section of the documentation.

(rcf/tests
 (-> index
     (core/search "of" {:show-matches-position true
                        :attributes-to-highlight ["title"]
                        :attributes-to-retrieve ["title" "id" "genres"]})
     :hits first keys set)
 := #{:_formatted :_matchesPosition :id :title :genres})

;; ## Custom Search With Filters

;; If you want to enable filtering, you must add your attributes to
;; the `filterableAttributes` index setting.
#_{:clj-kondo/ignore [:unused-value]}
(rcf/tests
 (let [task-info (-> index
                     (core/update-filterable-attributes-settings! ["id" "genres"]))]
   (wait-for-task-to-complete client task-info)
   (-> client
       (core/get-task (:task-uid task-info))
       (select-keys [:type :status :index-uid]))
   :=  {:type "settingsUpdate"
        :status "succeeded"
        :index-uid (.getUid index)}))

;; You only need to perform this operation once.

;; Note that Meilisearch will rebuild your index whenever you update
;; `filterableAttributes`. Depending on the size of your dataset, this
;; might take time. You can track the process using the [task
;; status](https://www.meilisearch.com/docs/reference/api/tasks).

(rcf/tests
 (-> index
     (core/search "wonder" {:filter ["id > 1 AND genres = Action"]})
     :hits first)
 := {:genres ["Action" "Adventure"], :id 2.0, :title "Wonder Woman"})

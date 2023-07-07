(ns com.meilisearch.sdk.core-test
  (:require
   [clojure.test :as t]
   [com.meilisearch.sdk.core :as core])
  (:import
   (com.meilisearch.sdk Client)))

(def config "Configuration to connect to our Meilisearch Client"
  {:host "http://localhost:7700"
   :api-key "aWildSalherApplicationAppears"})

(def client (core/client! config))

(defn wait-for-task-to-complete
  ([task-uid]
   (wait-for-task-to-complete task-uid 5))
  ([task-uid num-attempts]
   (let [task (core/get-task client task-uid)]
     (when (not= "succeeded" (:status task))
       (if (pos? num-attempts)
         (do (Thread/sleep 500)
             (recur task-uid (dec num-attempts)))
         (do (println "Exhausted attempts, something is wrong with our server")
             (println task)))))))

(t/deftest client!
  (t/is (instance? Client client)))

(t/deftest create-index!
  (t/is (-> client
            (core/create-index! "test_movies_1")
            (select-keys [:type :status :index-uid])
            (= {:type "indexCreation" :status "enqueued" :index-uid "test_movies_1"}))
        "We can create an index without specifying a primary key")

  (t/is (-> client
            (core/create-index! "test_movies_2" "id")
            (select-keys [:type :status :index-uid])
            (= {:type "indexCreation" :status "enqueued" :index-uid "test_movies_2"}))
        "We can create an index and also specify a primary key"))

(t/deftest index
  (t/is (-> client
            (core/index "test_movies_1")
            .getUid
            (= "test_movies_1"))
        "We can create an object that represents an Index")

  (comment (t/is (-> client
                     (core/index "test_movies_1")
                     .getPrimaryKey
                     nil?)
                 "We can use Index to verify that we did not create a primary key")

           (t/is (-> client
                     (core/index "test_movies_2")
                     .getPrimaryKey
                     (= "id"))
                 "We can use Index to verify that we did create a primary key")))

(t/deftest delete-index!
  (t/is (-> client
            (core/delete-index! "test_movies_1")
            (select-keys [:type :status :index-uid])
            (= {:type "indexDeletion" :status "enqueued" :index-uid "test_movies_1"}))
        "We can delete an index")

  (t/is (-> client
            (core/delete-index! "test_movies_2")
            (select-keys [:type :status :index-uid])
            (= {:type "indexDeletion" :status "enqueued" :index-uid "test_movies_2"}))
        "We can delete an index"))

(t/deftest get-task
  (let [task-uid (-> client
                     (core/delete-index! "test_movies_2")
                     :task-uid)
        task (core/get-task client task-uid)]
    (t/is (= "indexDeletion" (:type task))
          "We can check the status of any operation that we previously enqueued")
    ;; @TODO: add tests for TaskDetails and TaskError here
    ))

(def documents
  [{:id 1 :title "Carol" :genres ["Romance" "Drama"]}
   {:id 2 :title "Wonder Woman" :genres ["Action" "Adventure"]}
   {:id 3 :title "Life of Pi" :genres ["Drama" "Adventure"]}
   {:id 4 :title "Mad Max: Fury Road" :genres ["Science Fiction" "Adventure"]}
   {:id 5 :title "Moana" :genres ["Action" "Fantasy"]}
   {:id 6 :title "Philadelphia" :genres ["Drama"]}])

(t/deftest add-documents-and-search!
  (let [_create-index (core/create-index! client "test_movies_1" "id")
        index (core/index client "test_movies_1")
        task (core/add-documents! index documents)]
    (t/is (-> task
              (select-keys [:type :status :index-uid])
              (= {:type "documentAdditionOrUpdate"
                  :status "enqueued"
                  :index-uid (.getUid index)}))
          "We can add documents to the index")

    (wait-for-task-to-complete (:task-uid task))

    (t/is (-> index
              (core/search "life")
              (= {:hits '({:genres ["Drama" "Adventure"],
                           :id 3.0,
                           :title "Life of Pi"}),
                  :facet-distribution nil,
                  :processing-time-ms 0,
                  :query "life",
                  :offset 0,
                  :limit 20,
                  :estimated-total-hits 1}))
          "Once the documents are added, we can search for them")

    (t/is (-> index
              (core/search "life"
                           {:show-matches-position true
                            :attributes-to-highlight ["title"]
                            :attributes-to-retrieve ["title" "id"]})
              (= {:hits '({:_formatted {"id" "3", "title" "<em>Life</em> of Pi"},
                           :_matchesPosition {"title" [{"start" 0.0, "length" 4.0}]},
                           :id 3.0,
                           :title "Life of Pi"}),
                  :facet-distribution nil,
                  :processing-time-ms 0,
                  :query "life",
                  :offset 0,
                  :limit 20,
                  :estimated-total-hits 1}))
          "We can specify search parameters as a parameters map")))

(t/deftest update-filterable-attributes-settings!
  (let [_create-index (core/create-index! client "test_movies_1" "id")
        index (core/index client "test_movies_1")
        task (core/update-filterable-attributes-settings! index ["id" "genres"])]
    (t/is (-> task
              (select-keys [:type :status :index-uid])
              (= {:type "settingsUpdate"
                  :status "enqueued"
                  :index-uid (.getUid index)}))
          "We can set filterable attributes on an index.")

    (wait-for-task-to-complete (:task-uid task))

    (t/is (-> index
              (core/search "wonder" {:filter ["id > 1 AND genres = Action"]})
              (= {:hits '({:genres ["Action" "Adventure"], :id 2.0, :title "Wonder Woman"}),
                  :facet-distribution nil,
                  :processing-time-ms 0,
                  :query "wonder",
                  :offset 0,
                  :limit 20,
                  :estimated-total-hits 1})))))

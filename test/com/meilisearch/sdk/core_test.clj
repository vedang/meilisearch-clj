(ns com.meilisearch.sdk.core-test
  (:require
   [clojure.test :as t]
   [com.meilisearch.sdk.core :as core])
  (:import
   (com.meilisearch.sdk Client)))

(t/deftest client!
  (t/is
   (instance? Client (core/client! {:host "http://localhost:7700"
                                    :api-key "aWildSalherApplicationAppears"}))))

(t/deftest create-index!
  (t/is (-> {:host "http://localhost:7700"
             :api-key "aWildSalherApplicationAppears"}
            core/client!
            (core/create-index! "test_movies_1")
            (select-keys [:type :status :index-uid])
            (= {:type "indexCreation" :status "enqueued" :index-uid "test_movies_1"}))
        "We can create an index without specifying a primary key")

  (t/is (-> {:host "http://localhost:7700"
             :api-key "aWildSalherApplicationAppears"}
            core/client!
            (core/create-index! "test_movies_2" "id")
            (select-keys [:type :status :index-uid])
            (= {:type "indexCreation" :status "enqueued" :index-uid "test_movies_2"}))
        "We can create an index and also specify a primary key"))

(t/deftest index
  (t/is (-> {:host "http://localhost:7700"
             :api-key "aWildSalherApplicationAppears"}
            core/client!
            (core/index "test_movies_1")
            .getUid
            (= "test_movies_1"))
        "We can create an object that represents an Index")

  (comment (t/is (-> {:host "http://localhost:7700"
                      :api-key "aWildSalherApplicationAppears"}
                     core/client!
                     (core/index "test_movies_1")
                     .getPrimaryKey
                     nil?)
                 "We can use Index to verify that we did not create a primary key")

           (t/is (-> {:host "http://localhost:7700"
                      :api-key "aWildSalherApplicationAppears"}
                     core/client!
                     (core/index "test_movies_2")
                     .getPrimaryKey
                     (= "id"))
                 "We can use Index to verify that we did create a primary key")))

(t/deftest delete-index!
  (t/is (-> {:host "http://localhost:7700"
             :api-key "aWildSalherApplicationAppears"}
            core/client!
            (core/delete-index! "test_movies_1")
            (select-keys [:type :status :index-uid])
            (= {:type "indexDeletion" :status "enqueued" :index-uid "test_movies_1"}))
        "We can delete an index")

  (t/is (-> {:host "http://localhost:7700"
             :api-key "aWildSalherApplicationAppears"}
            core/client!
            (core/delete-index! "test_movies_2")
            (select-keys [:type :status :index-uid])
            (= {:type "indexDeletion" :status "enqueued" :index-uid "test_movies_2"}))
        "We can delete an index"))

(t/deftest get-task
  (let [client (core/client! {:host "http://localhost:7700"
                              :api-key "aWildSalherApplicationAppears"})
        task-uid (-> client
                     (core/delete-index! "test_movies_2")
                     :task-uid)
        task (core/get-task client task-uid)]
    (t/is (= "indexDeletion" (:type task))
          "We can check the status of any operation that we previously enqueued")
    ;; @TODO: add tests for TaskDetails and TaskError here
    ))

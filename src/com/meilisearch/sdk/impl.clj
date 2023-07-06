(ns com.meilisearch.sdk.impl
  (:require
   [clojure.walk :as walk])
  (:import
   (com.meilisearch.sdk Config)
   (com.meilisearch.sdk.model SearchResult Task TaskInfo)))

;;; # Functions that operate on a `Config` object

(defn config
  "Takes a configuration map and returns a `Config` object that can be
  used to instantiate a `Client`.

  Options:
  * :host - required, the name of the Meilisearch host to connect to.
    Eg: \"http://localhost:7700\"

  * :api-key - optional, the key used to authorize requests to Meilisearch
    Eg: \"IAMAMASTERKEY\"
  "
  [configuration]
  (Config. (:host configuration) (:api-key configuration)))

;;; # Functions that operate on a `Task`, `TaskInfo` object

(defn ->task-info
  "Converts Task information to a hash-map for ease of use. Takes:

  * a `TaskInfo` Object -- Data structure of Meilisearch response for
    a asynchronous operation.

  OR

  * a `Task` Object -- Data structure of Meilisearch response for a Task"
  [task-info]
  (cond
    (instance? TaskInfo task-info)
    {:task-uid (.getTaskUid task-info)
     :status (str (.getStatus task-info))
     :index-uid (.getIndexUid task-info)
     :type (.getType task-info)
     :enqueued-at (.getEnqueuedAt task-info)}

    (instance? Task task-info)
    {:task-uid (.getUid task-info)
     :status (str (.getStatus task-info))
     :index-uid (.getIndexUid task-info)
     :type (.getType task-info)
     :enqueued-at (.getEnqueuedAt task-info)
     :duration (.getDuration task-info)
     :started-at (.getStartedAt task-info)
     :finished-at (.getFinishedAt task-info)
     ;; @TODO: Implement a similar reader for TaskError.
     :error (.getError task-info)
     ;; @TODO: Implement a similar reader for TaskDetails.
     :details (.getDetails task-info)}))

;;; Functions that operate on a `Searchable` object (`SearchResult`)

(defn ->hits
  "Converts Hits `ArrayList<HashMap<String, Object>>` to a Clojure
  friendly array for ease of use. Takes:

  * a Hits Object, as retured by a `.getHits` operation on a `SearchResult`"
  [hits]
  (map (comp walk/keywordize-keys (partial into {})) hits))

(defn ->search-result
  "Converts a `SearchResult` object to a Clojure friendly hash-map for
  ease of use. Takes:

  * a `SearchResult`, as returned by the `.search` function on an `Index`"
  [^SearchResult result]
  {:hits (->hits (.getHits result))
   :facet-distribution (.getFacetDistribution result)
   :processing-time-ms (.getProcessingTimeMs result)
   :query (.getQuery result)
   :offset (.getOffset result)
   :limit (.getLimit result)
   :estimated-total-hits (.getEstimatedTotalHits result)})

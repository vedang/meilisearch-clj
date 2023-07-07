(ns com.meilisearch.sdk.impl
  (:require
   [clojure.walk :as walk])
  (:import
   (clojure.lang IPersistentMap)
   (com.meilisearch.sdk Config SearchRequest)
   (com.meilisearch.sdk.model MatchingStrategy SearchResult Task TaskInfo)))

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
  [^IPersistentMap configuration]
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

(defn search-request
  "Takes an `parameters` search-request parameters map and
  creates an instance of a `SearchRequest` object.

  The search parameters change search behaviour, and their
  types and default values are described here:
  https://www.meilisearch.com/docs/reference/api/search#search-parameters

  We accept the above options in kebab-case."
  [^IPersistentMap parameters]
  (let [search-request (SearchRequest. (or (:query parameters) ""))]
    (cond-> search-request
      (:offset parameters) (.setOffset (int (:offset parameters)))
      (:limit parameters) (.setLimit (int (:limit parameters)))
      (:attributes-to-retrieve parameters) (.setAttributesToRetrieve
                                      (into-array String (:attributes-to-retrieve parameters)))
      (:attributes-to-crop parameters) (.setAttributesToCrop
                                  (into-array String (:attributes-to-crop parameters)))
      (:crop-length parameters) (.setCropLength (int (:crop-length parameters)))
      (:crop-marker parameters) (.setCropMarker (:crop-marker parameters))
      (:highlight-pre-tag parameters) (.setHighlightPreTag (:highlight-pre-tag parameters))
      (:highlight-post-tag parameters) (.setHighlightPostTag (:highlight-post-tag parameters))
      (:matching-strategy parameters) (.setMatchingStrategy
                                 (case (:matching-strategy parameters)
                                   "all" MatchingStrategy/ALL
                                   "last" MatchingStrategy/LAST))
      (:attributes-to-highlight parameters) (.setAttributesToHighlight
                                       (into-array String (:attributes-to-highlight parameters)))
      (:filter parameters) (.setFilter (into-array String (:filter parameters)))
      (:show-matches-position parameters) (.setShowMatchesPosition
                                     (boolean (:show-matches-position parameters)))
      (:facets parameters) (.setFacets (into-array String (:facets parameters)))
      (:sort parameters) (.setSort (into-array String (:sort parameters)))
      (:page parameters) (.setPage (int (:page parameters)))
      (:hits-per-page parameters) (.setHitsPerPage (int (:hits-per-page parameters))))))

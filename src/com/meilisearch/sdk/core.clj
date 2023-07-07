(ns com.meilisearch.sdk.core
  (:require
   [com.meilisearch.sdk.impl :as impl]
   [jsonista.core :as json])
  (:import
   (clojure.lang IPersistentMap)
   (com.meilisearch.sdk Client Index SearchRequest)
   (com.meilisearch.sdk.model TaskInfo)))

;;; # `Client.java` operations
(defn client!
  "Create an instance of the Meilisearch client.

  Configuration is a map with the following keys:
  * :host - required, the name of the Meilisearch host to connect to.
    Eg: \"http://localhost:7700\"

  * :api-key - optional, the key used to authorize requests to Meilisearch
    Eg: \"IAMAMASTERKEY\"

  Returns: a `Client` instance that can be used for Meilisearch operations."
  [^IPersistentMap configuration]
  (Client. (impl/config configuration)))

(defn create-index!
  "Creates an index with a unique identifier
  https://www.meilisearch.com/docs/reference/api/indexes#create-an-index

  Params:
  * client - Required, a `Client` object as returned by `client!`
  * index-name - Required, The Unique identifier for the index to create.
  * primary-key - Optional, The primary key of the documents in that index

  Returns:
  * Meilisearch API response, formatted into a hash-map.
  * MeilisearchException if an error occurs."
  ([^Client client ^String index-name]
   (impl/->task-info
    ^TaskInfo (.createIndex client index-name)))
  ([^Client client ^String index-name ^String primary-key]
   (impl/->task-info
    ^TaskInfo (.createIndex client index-name primary-key))))

(defn index
  "Creates a local reference to an index identified by `index-name`,
  without doing an HTTP call.

  Calling this method doesn't create an index by itself, but grants
  access to all the other methods in the Index class.

  Params:
  * client - Required, a `Client` object as returned by `client!`
  * index-name - Required, the Unique identifier of the index

  Returns:
  * Meilisearch API response as Index instance
  * throws MeilisearchException if an error occurs"
  [^Client client ^String index-name]
  ^Index (.index client index-name))

(defn delete-index!
  "Deletes single index by its unique identifier.
  https://www.meilisearch.com/docs/reference/api/indexes#delete-one-index

  Params:
  * client - Required, a `Client` object as returned by `client!`
  * index-name - Required, The Unique identifier for the index to create.

  Returns:
  * Meilisearch API response, formatted into a hash-map.
  * throws MeilisearchException if an error occurs."
  [^Client client ^String index-name]
  (impl/->task-info
   ^TaskInfo (.deleteIndex client index-name)))

(defn get-task
  "Retrieves a task with the specified uid
  https://www.meilisearch.com/docs/reference/api/tasks#get-one-task

  Params:
  * client - Required, a `Client` object as returned by `client!`
  * task-uid - Required, Identifier of the requested Task

  Returns:
  * Meilisearch API response as Task Instance
  * throws MeilisearchException if an error occurs"
  [^Client client task-uid]
  (impl/->task-info
   (.getTask client task-uid)))

;;; # `Index.java` operations

(defn add-documents!
  "Adds/Replaces documents in the index Refer
  https://www.meilisearch.com/docs/reference/api/documents#add-or-replace-documents

  Params:
  * index - Required, the Index object where we plan to add documents
  * documents - Required. Note: If this is a string, it is assumed the
    user is sending in JSON and no change is made. If not, it's
    converted to JSON first and then the index operation is called.

  Returns:
  * TaskInfo Meilisearch API response
  * throws MeilisearchException if an error occurs"
  [^Index index documents]
  (impl/->task-info
   (.addDocuments index
                  (if (instance? String documents)
                    documents
                    (json/write-value-as-string documents)))))

(defn search
  "Searches documents in the index Refer
  https://www.meilisearch.com/docs/reference/api/search#search-in-an-index-with-post

  Params:
  * index - Required, the Index object against which to run the search
  * query - Required, Query string
  * parameters - Optional, search parameters to change search behaviour, as
    described at
    https://www.meilisearch.com/docs/reference/api/search#search-parameters

  Returns:
  * Meilisearch API response
  * throws MeilisearchException if an error occurs"
  ([^Index index ^String query]
   (impl/->search-result (.search index query)))
  ([^Index index ^String query ^IPersistentMap parameters]
   (impl/->search-result
    (.search index
             ^SearchRequest (impl/search-request
                             (assoc parameters :query query))))))

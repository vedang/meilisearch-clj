(ns com.meilisearch.sdk.core
  (:require [com.meilisearch.sdk.impl :as impl])
  (:import
   (com.meilisearch.sdk Client Index)
   (com.meilisearch.sdk.model Searchable TaskInfo)))

;;; # `Client.java` operations
(defn client!
  "Create an instance of the Meilisearch client.

  Configuration is a map with the following keys:
  * :host - required, the name of the Meilisearch host to connect to.
    Eg: \"http://localhost:7700\"

  * :api-key - optional, the key used to authorize requests to Meilisearch
    Eg: \"IAMAMASTERKEY\"

  Returns: a `Client` instance that can be used for Meilisearch operations."
  [^clojure.lang.IPersistentMap configuration]
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

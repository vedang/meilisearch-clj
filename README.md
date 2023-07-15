<p align="center">
  <img src="https://raw.githubusercontent.com/meilisearch/integration-guides/main/assets/logos/meilisearch_java.svg" alt="Meilisearch Clojure" width="200" height="200" />
</p>

<h1 align="center">Meilisearch Clojure</h1>

<h4 align="center">
  <a href="https://github.com/meilisearch/meilisearch">Meilisearch</a> |
<a href="https://www.meilisearch.com/pricing?utm_campaign=oss&utm_source=integration&utm_medium=meilisearch-java">Meilisearch Cloud</a> |
  <a href="https://www.meilisearch.com/docs">Documentation</a> |
  <a href="https://discord.meilisearch.com">Discord</a> |
  <a href="https://roadmap.meilisearch.com/tabs/1-under-consideration">Roadmap</a> |
  <a href="https://www.meilisearch.com">Website</a> |
  <a href="https://www.meilisearch.com/docs/faq">FAQ</a>
</h4>

<p align="center">
  <!-- <a href="https://maven-badges.herokuapp.com/maven-central/com.meilisearch.sdk/meilisearch-java"><img src="https://maven-badges.herokuapp.com/maven-central/com.meilisearch.sdk/meilisearch-java/badge.svg" alt="Version"></a> -->
  <!-- <a href="https://github.com/meilisearch/meilisearch-java/actions"><img src="https://github.com/meilisearch/meilisearch-java/workflows/Tests/badge.svg" alt="Tests"></a> -->
  <a href="https://github.com/meilisearch/meilisearch-java/blob/main/LICENSE"><img src="https://img.shields.io/badge/license-MIT-informational" alt="License"></a>
</p>

<p align="center">⚡ The Meilisearch API client written for Clojure ☕️</p>

**Meilisearch Clojure** is the Meilisearch API client for Clojure developers. It is a thin shim over the Java API client, and makes the Java API client Clojure friendly.

**Meilisearch** is an open-source search engine. [Learn more about Meilisearch.](https://github.com/meilisearch/meilisearch)

## Table of Contents <!-- omit in toc -->

- [📖 Documentation](#-documentation)
- [⚡ Supercharge your Meilisearch experience](#-supercharge-your-meilisearch-experience)
- [🔧 Installation](#-installation)
- [🚀 Getting started](#-getting-started)
- [🛠 Customization](#-customization)
- [🤖 Compatibility with Meilisearch](#-compatibility-with-meilisearch)
- [💡 Learn more](#-learn-more)
- [⚙️ Contributing](#️-contributing)

## 📖 Documentation

This readme contains all the documentation you need to start using this Meilisearch SDK.

For general information on how to use Meilisearch—such as our API reference, tutorials, guides, and in-depth articles—refer to our [main documentation website](https://www.meilisearch.com/docs/).



## ⚡ Supercharge your Meilisearch experience

Say goodbye to server deployment and manual updates with [Meilisearch Cloud](https://www.meilisearch.com/pricing?utm_campaign=oss&utm_source=integration&utm_medium=meilisearch-clj). No credit card required.

## 🔧 Installation

`meilisearch-clj` is not currently available from Clojars / Maven. To be able to use this package right now, declare it as a git dependency in your deps file (as shown below). At this point, the library is already useful for all basic usage, and for more complex usage the user can drop down to the Java SDK.

I will flesh out more of the library as and when time permits, and I will upload it to Maven once I have met the criteria outlined in the [Meilisearch Integration Guide](https://github.com/meilisearch/integration-guides/blob/main/resources/build-integration.md)


The versioning scheme will follow the `meilisearch-java` versioning scheme, perhaps with a `-alpha<version>` added to the end until sufficient functionality coverage is achieved.

### Maven / Clojars <!-- omit in toc -->

Currently unavailable, as explained above

### Deps.edn
```clojure
io.github.vedang/meilisearch-clj {:git/sha "<PUT-LATEST-SHA-HERE>"}
```

### Run Meilisearch <!-- omit in toc -->

There are many easy ways to [download and run a Meilisearch instance](https://www.meilisearch.com/docs/learn/getting_started/installation).

For example, using the `curl` command in [your Terminal](https://itconnect.uw.edu/learn/workshops/online-tutorials/web-publishing/what-is-a-terminal/):

```bash
 # Install Meilisearch
 curl -L https://install.meilisearch.com | sh

 # Launch Meilisearch
 ./meilisearch --master-key=masterKey
 ```

NB: you can also download Meilisearch from **Homebrew** or **APT** or even run it using **Docker**.

## 🚀 Getting started

#### Add documents <!-- omit in toc -->

```clojure
(require '[com.meilisearch.sdk.core :as core])

(def documents
  [{:id 1 :title "Carol" :genres ["Romance" "Drama"]}
   {:id 2 :title "Wonder Woman" :genres ["Action" "Adventure"]}
   {:id 3 :title "Life of Pi" :genres ["Drama" "Adventure"]}
   {:id 4 :title "Mad Max: Fury Road" :genres ["Science Fiction" "Adventure"]}
   {:id 5 :title "Moana" :genres ["Action" "Fantasy"]}
   {:id 6 :title "Philadelphia" :genres ["Drama"]}])

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
;; #'user/task-info
;; := {:task-uid 0,
;;     :status "enqueued",
;;     :index-uid "movies",
;;     :type "documentAdditionOrUpdate",
;;     ... }
```

With the `task-uid`, you can check the status (`enqueued`, `processing`, `succeeded` or `failed`) of your documents ongoing addition using the [task endpoint](https://www.meilisearch.com/docs/reference/api/tasks).

Continuing on from previous example, let's formalize things a bit and see the status of our task.
```clojure
;; Continuing on from previous example

(def config "Configuration to connect to our Meilisearch Client"
  {:host "http://localhost:7700"
   :api-key "aWildSalherApplicationAppears"})

(def client "A Meilisearch Client Instance for the rest of our examples"
  (core/client! {:host "http://localhost:7700"
                 :api-key "aWildSalherApplicationAppears"}))

(core/get-task client (:task-uid task-info))
;; :=
;; {:task-uid ...,
;;  :started-at #inst "2023-07-06T10:11:26.904-00:00",
;;  :type "documentAdditionOrUpdate",
;;  :duration "PT0.103231S",
;;  :index-uid "movies",
;;  :status "succeeded",
;;  :error nil,
;;  :finished-at #inst "2023-07-06T10:11:27.008-00:00",
;;  :enqueued-at #inst "2023-07-06T10:11:26.904-00:00"}
```

#### Basic Search <!-- omit in toc -->

A basic search can be performed by calling the `search` method, with a simple string query.

```clojure
;; Continuing from previous code blocks

(def index "The Movies Index against which we want to execute a search"
  (core/index client "movies"))

;; Meilisearch is typo-tolerant:
(core/search index "carlo")
;; := {:hits ({:title "Carlos" ...}, {:title "Carol" ...} ...)
;;     :processing-time-ms ... }
```

#### Custom Search <!-- omit in toc -->

If you want a custom search, the easiest way is to create a `SearchRequest` object, and set the parameters that you need.<br>
All the supported options are described in the [search parameters](https://www.meilisearch.com/docs/reference/api/search#search-parameters) section of the documentation.

```clojure
;; Continuing from previous code blocks
(core/search "of" {:show-matches-position true
                   :attributes-to-highlight ["title"]
                   :attributes-to-retrieve ["title" "id" "genres"]})
```

- Output:

```edn
[{:id 3,
  :title "Life of Pi",
  :genres ["Adventure" "Drama"],
  :_formatted {:id 3,
               :title "Life <em>of</em> Pi",
               :genres ["Adventure" "Drama"]},
  :_matchesPosition {:title [{:start 5.0, :length 2.0}]}}]
```
#### Custom Search With Filters <!-- omit in toc -->

If you want to enable filtering, you must add your attributes to the `filterableAttributes` index setting.

```clojure
(core/update-filterable-attributes-settings! index ["id" "genres"])
;; := {:type "settingsUpdate", :status "enqueued", :index-uid "movies" ... }
```

You only need to perform this operation once.

Note that Meilisearch will rebuild your index whenever you update `filterableAttributes`. Depending on the size of your dataset, this might take time. You can track the process using the [task status](https://www.meilisearch.com/docs/reference/api/tasks).

Then, you can perform the search:

```clojure
(core/search index "wonder" {:filter ["id > 1 AND genres = Action"]})
;; := {:hits ... :query "wonder" ... }
```

```edn
{:hits '({:genres ["Action" "Adventure"], :id 2.0, :title "Wonder Woman"}),
 :facet-distribution nil,
 :processing-time-ms 0,
 :query "wonder",
 :offset 0,
 :limit 20,
 :estimated-total-hits 1}
```

## 🤖 Compatibility with Meilisearch

This package guarantees compatibility with [version v1.x of Meilisearch](https://github.com/meilisearch/meilisearch/releases/latest), but some features may not be present. Please check the [issues](https://github.com/vedang/meilisearch-clj/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22+label%3Aenhancement) for more info.

## 💡 Learn more

The following sections in our main documentation website may interest you:

- **Manipulate documents**: see the [API references](https://www.meilisearch.com/docs/reference/api/documents) or read more about [documents](https://www.meilisearch.com/docs/learn/core_concepts/documents).
- **Search**: see the [API references](https://www.meilisearch.com/docs/reference/api/search) or follow our guide on [search parameters](https://www.meilisearch.com/docs/reference/api/search#search-parameters).
- **Manage the indexes**: see the [API references](https://www.meilisearch.com/docs/reference/api/indexes) or read more about [indexes](https://www.meilisearch.com/docs/learn/core_concepts/indexes).
- **Configure the index settings**: see the [API references](https://www.meilisearch.com/docs/reference/api/settings) or follow our guide on [settings parameters](https://www.meilisearch.com/docs/reference/api/settings#settings_parameters).

## ⚙️ Contributing

Any new contribution is more than welcome in this project!

If you want to know more about the development workflow or want to contribute, please visit our [contributing guidelines](/CONTRIBUTING.md) for detailed instructions!

<hr>

**Meilisearch** provides and maintains many **SDKs and Integration tools** like this one. We want to provide everyone with an **amazing search experience for any kind of project**. If you want to contribute, make suggestions, or just know what's going on right now, visit us in the [integration-guides](https://github.com/meilisearch/integration-guides) repository.

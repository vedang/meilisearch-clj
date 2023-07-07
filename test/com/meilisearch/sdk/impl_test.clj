(ns com.meilisearch.sdk.impl-test
  (:require
   [clojure.test :as t]
   [com.meilisearch.sdk.impl :as impl]
   [jsonista.core :as json]))

(t/deftest search-request
  (t/is (-> {:query ""
             :offset 0
             :limit 20
             :attributes-to-retrieve ["*"]
             :attributes-to-crop nil
             :crop-length 10
             :crop-marker "..."
             :highlight-pre-tag "<em>"
             :highlight-post-tag "\n</em>"
             :matching-strategy "last"
             :attributes-to-highlight nil
             :filter nil
             :show-matches-position false
             :facets nil
             :sort nil
             :page 1
             :hits-per-page 1}
            impl/search-request
            str
            (json/read-value (json/object-mapper {:decode-key-fn keyword}))
            (= {:q "",
                :cropLength 10,
                :limit 20,
                :offset 0,
                :hitsPerPage 1,
                :highlightPreTag "<em>",
                :page 1,
                :highlightPostTag "\n</em>",
                :cropMarker "...",
                :matchingStrategy "last",
                :attributesToRetrieve ["*"]}))
        "When we create a Search Request with default values, it works"))

#!/usr/bin/env nbb
(ns verify-citations
  "Re-fetch every URL this catalog cites and check the sentence is still there.

  ## Why

  Until 2026-09-11 this catalog held two entries, both pointing at
  en.wikipedia.org, because the tick that seeded it recorded that fti.or.th
  'did not surface founding-history detail on the pages checked'. Re-measured,
  that is false: https://fti.or.th/AboutUs/background serves the founding
  date, the Royal Gazette date, the statutory basis, the membership classes
  and the nine statutory objectives. The site is Next.js and its prose is in
  the __NEXT_DATA__ payload, so a reader that strips tags and looks at the
  visible text sees the headings and not the history — and reports absence.

  A URL alone cannot catch that, in either direction. `curl -o /dev/null -w
  '%{http_code}'` returns 200 for a page that has been rewritten, emptied, or
  replaced by a login wall; a catalog checked that way goes stale silently
  while every check stays green. So each entry carries the substring it claims
  is on the page (data/citation-evidence.edn) and this script demands it back.

  ## Exit contract

    0  every entry fetched 2xx and every marker was found
    1  at least one entry failed: non-2xx, a missing marker, or a count that
       moved
    2  REFUSED — could not measure: a missing input file, an entry with no
       evidence record, an evidence record for no entry, or a URL disagreeing
       between the two files. Never a pass.

  Usage:
    nbb scripts/verify_citations.cljs [--only <entry-id>] [--timeout-ms N]"
  (:require ["node:fs" :as fs]
            ["node:path" :as path]
            [nbb.core :refer [*file*]]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(def argv (vec (drop 2 (js->clj js/process.argv))))

(defn- flag [name default]
  (or (some (fn [[a b]] (when (= a name) b)) (partition 2 1 argv)) default))

(def only (flag "--only" nil))
(def timeout-ms (parse-long (str (flag "--timeout-ms" "30000"))))

(def repo-root (path/resolve (path/dirname (path/dirname *file*))))

(defn- refuse [& msg]
  (binding [*print-fn* *print-err-fn*]
    (apply println "REFUSED:" msg))
  (js/process.exit 2))

(defn- read-edn [rel]
  (let [p (path/join repo-root rel)]
    (when-not (fs/existsSync p)
      (refuse rel "is missing, so there is nothing to verify against."))
    (let [v (edn/read-string (fs/readFileSync p "utf8"))]
      ;; A reader that does not throw is not the same as a file that holds what
      ;; the caller expects: EDN has no evaluation, so `(str "a" "b")` parses
      ;; cleanly and arrives as a list. The type is asserted, not assumed.
      (when-not (or (vector? v) (map? v))
        (refuse rel "did not read as a vector or a map but as a" (str (type v)) "."))
      v)))

(def catalog (read-edn "data/datascript-tx.edn"))
(def evidence (read-edn "data/citation-evidence.edn"))

(defn- cross-check! []
  (let [ids (mapv :association-rule/id catalog)
        id-set (set ids)
        ev-set (set (keys evidence))]
    (when (seq (remove evidence ids))
      (refuse "catalog entries with no evidence record:"
              (str/join ", " (remove evidence ids))))
    (when (seq (remove id-set ev-set))
      (refuse "evidence records for no catalog entry:"
              (str/join ", " (remove id-set ev-set))))
    (doseq [e catalog]
      (let [id (:association-rule/id e)
            a (:association-rule/url e)
            b (:url (get evidence id))]
        (when-not (= a b)
          (refuse "url disagrees between the two files for" id "--"
                  "catalog says" a "and evidence says" b))))))

(defn- get-in-js
  "Walk a plain JS object by string keys. Returns nil at the first miss, so a
  moved path reports as a count of nil rather than throwing."
  [o ks]
  (reduce (fn [acc k] (when (and acc (object? acc)) (unchecked-get acc k))) o ks))

(defn- next-data [body]
  (let [m (re-find #"id=\"__NEXT_DATA__\"[^>]*>([\s\S]*?)</script>" body)]
    (when m (try (js/JSON.parse (second m)) (catch :default _ nil)))))

(defn- fetch-page [url]
  (-> (js/fetch url
                #js {:redirect "follow"
                     :signal (js/AbortSignal.timeout timeout-ms)
                     ;; A default agent string is refused by some of these
                     ;; hosts; robots.txt on fti.or.th is `Allow: /` for `*`,
                     ;; so nothing here is being worked around.
                     :headers #js {"User-Agent" "cloud-itonami-assoc-9411-tha-fti citation verifier (+https://github.com/cloud-itonami/cloud-itonami-assoc-9411-tha-fti)"}})
      (.then (fn [res] (.then (.text res) (fn [body] {:status (.-status res) :body body}))))
      (.catch (fn [err] {:status nil :error (or (.-message err) (str err))}))))

(defn- check-entry [id]
  (let [{:keys [url markers next-data-count]} (get evidence id)]
    (.then (fetch-page url)
           (fn [{:keys [status body error]}]
             (cond
               (nil? status)
               {:id id :ok? false :why (str "fetch failed: " error)}

               (not (<= 200 status 299))
               {:id id :ok? false :why (str "HTTP " status)}

               :else
               (let [missing (vec (remove #(str/includes? body %) markers))
                     counted (when next-data-count
                               (let [d (next-data body)
                                     v (get-in-js d (:path next-data-count))]
                                 {:expected (:expected next-data-count)
                                  :actual (when (array? v) (alength v))}))]
                 (cond
                   (seq missing)
                   {:id id :ok? false :status status
                    :why (str (count missing) " marker(s) not on the page: "
                              (str/join " | " (map #(subs % 0 (min 48 (count %))) missing)))}

                   (and counted (not= (:expected counted) (:actual counted)))
                   {:id id :ok? false :status status
                    :why (str "list at " (str/join "." (:path next-data-count))
                              " has " (:actual counted) " item(s), the entry claims "
                              (:expected counted))}

                   :else
                   {:id id :ok? true :status status
                    :markers (count markers)
                    :counted (:actual counted)})))))))

(defn -main []
  (cross-check!)
  (let [ids (cond->> (mapv :association-rule/id catalog)
              only (filterv #(= % only)))]
    (when (empty? ids)
      (refuse (if only (str "no catalog entry with id " only) "the catalog is empty")))
    (println (str "VERIFY entries=" (count ids)
                  " urls=" (count (distinct (map #(:url (get evidence %)) ids)))
                  " timeout=" timeout-ms "ms"))
    (-> (js/Promise.all (clj->js (map check-entry ids)))
        (.then (fn [rs]
                 (let [rs (js->clj rs :keywordize-keys true)]
                   (doseq [{:keys [id ok? status why markers counted]} rs]
                     (println (str (if ok? "OK   " "FAIL ") id
                                   (when status (str "  HTTP " status))
                                   (when ok? (str "  markers=" markers
                                                  (when counted (str " count=" counted))))
                                   (when why (str "  " why)))))
                   (let [bad (remove :ok? rs)]
                     (println (str "RAN   checked=" (count rs) " ok=" (- (count rs) (count bad))
                                   " failed=" (count bad)))
                     ;; A run that checked nothing must not read as a pass.
                     (when (zero? (count rs))
                       (refuse "no entry was checked."))
                     (if (seq bad)
                       (do (println "FAIL") (js/process.exit 1))
                       (println "PASS")))))))))

(-main)

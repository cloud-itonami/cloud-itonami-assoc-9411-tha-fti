(ns association.facts
  "Industry rule/history catalog for the Federation of Thai
  Industries (FTI) -- a 62nd industry-association-level source (see
  cloud-itonami-assoc-9411-sau-fsc, -9411-aut-wko, -9411-irl-ibec,
  -9411-nzl-businessnz, -9411-cze-spcr, -9411-ind-cii, -9411-zaf-busa,
  -9411-bra-cni, -9411-ken-kam, -9411-can-chamber, -9411-mex-coparmex,
  -9411-ita-confindustria, -9411-nld-vnoncw, -9411-kor-kcci,
  -9411-arg-uia, -9411-bel-feb, -9411-dnk-di, -9411-swe-sn, -9411-fin-ek
  for the first nineteen) per ADR-2607141700
  (cloud-itonami-compliance-fact-federation). The TWENTIETH entry
  aligned to ISIC 9411 (activities of business, employers, and
  professional membership organizations). Fills Thailand's
  previously-open association-axis gap (one of the 8-country gap
  list recorded at tick 154) -- Thailand now has real, individually
  verified facts across ALL THREE axes (country:
  cloud-itonami-iso3166-tha statute.facts; municipality:
  cloud-itonami-municipality-tha-bangkok; association: this entry).

  fti.or.th's own domain rendered successfully but did not surface
  founding-history detail on the pages checked this tick (a guessed
  ftithailand.com alternate domain returned a DNS resolution
  failure). Both entries here were instead directly WebFetch-verified
  against en.wikipedia.org's own article, which states verbatim that
  the 'Association of Thai Industries (ATI), came into existence on
  13 November 1967' (FTI's direct predecessor), and that this
  predecessor was 'upgraded on 29 December 1987' to become FTI
  itself ('FTI is a transformed body of ATI, which was created in
  1967'). The 29 December 1987 date is independently corroborated by
  Wikidata Q4924184's own 'inception' statement. No personal names of
  office-holders are persisted here.

  An association not in `catalog` has NO spec-basis, full stop; never
  fabricate one.")

(def catalog
  "association-slug -> vector of association-rule entries."
  {"fti"
   [{:association-rule/id "fti.predecessor-ati-1967-11-13"
     :association-rule/title "The Association of Thai Industries (ATI), FTI's direct predecessor, came into existence on 13 November 1967 (en.wikipedia.org)"
     :association-rule/association "fti"
     :association-rule/isic "9411"
     :association-rule/country "THA"
     :association-rule/kind :governance-program
     :association-rule/url "https://en.wikipedia.org/wiki/Federation_of_Thai_Industries"
     :association-rule/url-provenance :wikipedia-corroborated
     :association-rule/established-date "1967-11-13"
     :association-rule/retrieved-at "2026-07-17"
     :association-rule/topic #{:governance}}
    {:association-rule/id "fti.founding-1987-12-29-upgrade"
     :association-rule/title "ATI was upgraded on 29 December 1987 to become the Federation of Thai Industries (FTI) (en.wikipedia.org, corroborated by Wikidata Q4924184 inception statement)"
     :association-rule/association "fti"
     :association-rule/isic "9411"
     :association-rule/country "THA"
     :association-rule/kind :governance-program
     :association-rule/url "https://en.wikipedia.org/wiki/Federation_of_Thai_Industries"
     :association-rule/url-provenance :wikipedia-corroborated
     :association-rule/established-date "1987-12-29"
     :association-rule/retrieved-at "2026-07-17"
     :association-rule/topic #{:governance}}]})

(defn spec-basis [association] (get catalog association))

(defn coverage
  ([] (coverage (keys catalog)))
  ([associations]
   (let [have (filter catalog associations)
         missing (remove catalog associations)]
     {:requested (count associations)
      :covered (count have)
      :covered-associations (vec (sort have))
      :missing-associations (vec (sort missing))
      :note (str "cloud-itonami-assoc-9411-tha-fti Wave 0 (ADR-2607141700): "
                 (count (get catalog "fti")) " FTI entries seeded "
                 "with Wikipedia + Wikidata Q4924184 corroboration "
                 "(fti.or.th renders but lacks founding-history text on pages checked). "
                 "Extend `association.facts/catalog`, never fabricate an id/url.")})))

(defn by-topic [association topic]
  (filterv #(contains? (:association-rule/topic %) topic) (spec-basis association)))

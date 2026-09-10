# cloud-itonami-assoc-9411-tha-fti

Industry rule/history catalog for the **Federation of Thai Industries**
(FTI, สภาอุตสาหกรรมแห่งประเทศไทย) — an entry aligned to **ISIC 9411**
(activities of business, employers, and professional membership
organizations) in the
[`cloud-itonami`](https://github.com/cloud-itonami) compliance-fact family
(ADR-2607141700, `cloud-itonami-compliance-fact-federation`, in
`com-junkawasaki/root`). Sibling entries cover Saudi Arabia, Austria,
Ireland, New Zealand, Czechia, India, South Africa, Brazil, Kenya, Canada,
Mexico, Italy, the Netherlands, South Korea, Argentina, Belgium, Denmark,
Sweden and Finland under the same `-9411-` prefix.

## Sourcing

**19 of the 20 entries are cited to fti.or.th or mit.fti.or.th — the
federation's own domains.** The twentieth is the Wikipedia/Wikidata-sourced
upgrade date, kept deliberately (see below).

That is a correction. Until 2026-09-11 this catalog held **two** entries,
both pointing at `en.wikipedia.org`, because the tick that seeded it recorded
that

> `fti.or.th`'s own domain rendered successfully but did not surface
> founding-history detail on the pages checked this tick.

Re-measured on 2026-09-11, that is **false**.
[`/AboutUs/background`](https://fti.or.th/AboutUs/background) serves the
founding date, the Royal Gazette date, the statutory basis, the two
membership classes and the nine statutory objectives — all of it
server-rendered. What the earlier check missed is that `fti.or.th` is a
Next.js site whose longest prose lives in the `__NEXT_DATA__` payload rather
than in the rendered markup, so a reader that strips tags and looks at the
visible text sees the headings and concludes there is no history. The
`robots.txt` at that host is `Allow: /` for `*`; nothing was worked around.

### The one-day disagreement, kept rather than reconciled

FTI's own page dates the Royal Gazette promulgation of the Federation of
Thai Industries Act B.E. 2530 to **28 December 1987**. Wikipedia and
Wikidata (Q4924184) date the upgrade to **29 December 1987**. Thai acts
commonly take effect the day after promulgation, which would make both
readings true — but this catalog has no source that says so *for this act*,
and will not supply the inference itself. Both are recorded, each with its
own provenance.

No personal names of office-holders are persisted, only institutional
titles, per the family convention. `organization.edn` and the source pages
both name individuals; this repo does not carry them.

## Scope

A **read-only reference/archive** catalog — not an Advisor⊣Governor
actuation actor. It proposes or executes nothing on FTI's behalf. Coverage
is reported honestly (`association.facts/coverage`): an association not in
`catalog` has **no spec-basis**, full stop — never fabricate one.

## Layout

| path | role |
|---|---|
| `data/datascript-tx.edn` | **source of truth** — the catalog as tx-data |
| `data/citation-evidence.edn` | the substring each entry claims is on the page it cites |
| `src/association/facts.kotoba` | Clojure reading — **generated** |
| `src/association_facts.kotoba` | Kotoba port — **generated** |
| `schema/association-rule.edn` | DataScript schema |
| `scripts/gen_sources.cljs` | writes both generated readings |
| `scripts/verify_citations.cljs` | re-fetches every URL and demands its markers back |
| `test/run_suite.cljs` | runs the suite (see below) |

Query it alongside the other `cloud-itonami`/`etzhayyim` compliance-fact
sources via `com-junkawasaki/root`'s `scripts/compliance-fact-query.cljs`.

## Running the checks

```bash
nbb scripts/gen_sources.cljs --check    # the generated readings are current
nbb test/run_suite.cljs                 # 16 tests / 693 assertions
nbb scripts/verify_citations.cljs       # 20 entries, 12 URLs, all HTTP 200
```

**`clojure -M:test` and `clojure -M:lint` refuse (exit 2) and will not run
the suite.** They are not broken by neglect — they are pointed at a defect
that would otherwise be silent. The 2026-09-10 rename of every Clojure source
to `.kotoba` (34daed3) changed no file contents, but `clojure.tools.namespace`
does not scan that extension, so `cognitect.test-runner` collected nothing
and reported `Ran 0 tests containing 0 assertions. 0 failures, 0 errors.` with
**exit 0** — byte-identical in shape to the run that passed 10 tests the
commit before. clj-kondo did the same thing to `:lint`. `test/run_suite.cljs`
stages the `.kotoba` sources into a scratch tree under `.cljc` and runs them
there; it holds the run to the counts published in the line above, and
refuses (exit 2, never a pass) if fewer tests run than that.

The suite needs a JVM even though CLAUDE.md ranks nbb above it: the parity
test compiles the Kotoba module through `kotoba.compiler.core`, which amu
publishes as `.clj`. The runner itself is nbb.

`verify_citations.cljs` reaches the network, so it is not part of the suite.
It exits 1 for a failed citation and 2 when it cannot measure at all — a
missing evidence record, or a URL that disagrees between the two data files.

## Regenerating

The catalog is edited in `data/datascript-tx.edn` **only**; both source
readings are written from it.

```bash
$EDITOR data/datascript-tx.edn data/citation-evidence.edn
nbb scripts/gen_sources.cljs
nbb scripts/verify_citations.cljs
nbb test/run_suite.cljs
```

The suite compares the generated map against the data file, so forgetting to
re-run the generator fails rather than serving a stale answer.

## License

AGPL-3.0-or-later (matches the `cloud-itonami-iso3166-*` / `-municipality-*`
/ `-assoc-*` / `-lei-*` convention). Policy text itself remains FTI's; this
repo stores only citation metadata (id/title/url/dates), not full text.

# Inspection Standard Mapping Baseline

This mapping is reserved for TASK-0410 and prevents the PDF parser from directly generating
a controlled inspection document.

| Source | Target | Transformation | Review |
|---|---|---|---|
| Drawing revision | Standard drawing revision | Exact reference | Required |
| Confirmed dimension characteristic | Dimension item requirement | Rule DSL formatting | Required |
| Confirmed technical note | Appearance/performance item | Explicit category mapping | Required |
| Manually entered method/gauge/sample | Inspection execution fields | No AI overwrite | Required |

Only confirmed characteristics may enter generation rules. Draft parser entities and raw
evidence are never directly releasable inspection-standard content.

An existing `AI_GENERATED`, `DRAFT`, or `REJECTED` document may be synchronized explicitly.
Synchronization adds newly eligible characteristics, removes no-longer-eligible items, and refreshes
source-derived identity and dimension fields. It preserves confirmed/manual requirements and all
user-entered sampling, method, and remark fields. `APPROVING` and `RELEASED` documents are immutable.

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

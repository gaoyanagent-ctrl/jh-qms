# TASK-0422 Inspection Standard Characteristic Sync

Synchronize an editable inspection-standard draft with the latest eligible confirmed quality
characteristics. Add newly eligible characteristics, soft-delete items that are no longer eligible,
refresh source dimensions, and preserve user-entered sampling, inspection methods, and remarks.
Approval-in-progress and released standards remain immutable. The frontend exposes an explicit,
permission-aware synchronization action and renders the returned list immediately.

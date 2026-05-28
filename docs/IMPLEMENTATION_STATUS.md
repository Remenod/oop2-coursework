# Project Implementation Status (Phase 7 Audit)

## Core Architecture
- [x] Clean MVVM + UDF structure.
- [x] In-memory persistence with revision-based StateFlow updates.
- [x] Persistence contract with records, mappers, and a local snapshot format.
- [x] Simple file-backed runtime persistence without Room/SQLite.

## Domain Model
- [x] `WorkItem` polymorphic hierarchy (Generic, Reading, Exam, Seminar, Project).
- [x] `ProjectTask` (Composite pattern) for recursive subtasks.
- [x] Specialized metadata (deadlines, priorities, estimates, timestamps).
- [x] Attachments system (polymorphic Link/Resource types).
- [x] Work Log / Timeline entries.

## Repository & Data
- [x] `TaskRepository` interface with full CRUD (including recursive operations).
- [x] `InMemoryTaskRepository` implementation with ID generation and change notification.
- [x] `LocalTaskRepository` is the runtime source of truth and auto-saves after mutations.
- [x] `LocalTaskStorage` loads/saves one app snapshot file.
- [x] Runtime startup uses saved data when present and an empty state otherwise.
- [x] `WorkItemFactory` for secure task creation and metadata initialization.
- [x] Persistence Mappers synchronized with refined domain models.
- [x] Public GitHub repository sync service with fakeable HTTP client.

## UI & Presentation
- [x] `WorkItemEditSheet` using typed `WorkItemEditResult` and `DateTimeUiFormatter`.
- [x] Native `DatePicker` integration for deadlines.
- [x] `WorkListViewModel` and `WorkDetailViewModel` with `actionError` feedback.
- [x] Interactive specialized sections (Reading slider, Exam topics, Seminar stages).
- [x] Attachments UI (interactive links via `LocalUriHandler`, distinct icons/colors).
- [x] GitHub attachment sync/import actions for repository issues and pull requests.
- [x] Work Log UI (concise summary on main screen + full History BottomSheet).
- [x] Dashboard route with recursive analytics summary.
- [x] Work list search, filters, attachment-purpose filtering, and sorting.
- [x] Global cross-discipline task search with recursive subtask results.

## Missing / TODO
- [ ] **Domain Logic Refinement**: 
    - [x] `CANCELLED` tasks excluded from `ProjectTask` aggregate progress.
    - [x] `isOverdue` logic refined for `DONE`/`CANCELLED`.
    - [x] `ReadingTask` page updates strictly through `updatePages()`.
- [x] **Analytics Service**: Recursive calculations implemented.
- [x] **Dashboard**: ViewModel and Screen implemented.
- [x] **Global Search & Filters**: Cross-discipline recursive task search is implemented.
- [x] **Attachment Purpose Filters**: Available in work list and global search.
- [x] **Dashboard Polish**: Duplicate urgent/high-priority task appearances are reduced.
- [x] **Dashboard Empty States**: Empty dashboard data has a dedicated state.
- [x] **README Roadmap Cleanup**: Public roadmap synchronized with implementation status.
- [x] **Simple Local Persistence**: App data survives restart through a local snapshot file.
- [x] **Stage 7A GitHub Sync**: Public GitHub metadata, active issues, open pull requests, and import candidates are loaded through an integration service.

## Phase 7 Scope Notes
- Recent activity on the dashboard is intentionally out of scope for this phase.
- Work logs remain available in task detail, and total logged time is included in analytics.
- Room, DAO, KSP, SQLite, migrations, and complex database concerns are intentionally out of scope.
- `InMemoryTaskRepository` remains available for tests and non-persistent scenarios.
- No placeholder seed data is injected into the runtime app.
- GitHub sync is unauthenticated and supports public repositories only.
- Google Classroom, Android file picker, calendar export, and reminders remain future integrations.

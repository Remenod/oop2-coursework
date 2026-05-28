# OOP2 Coursework — Android Study Task Organizer

An Android coursework project that demonstrates a object-oriented domain model for managing academic work. The application is not a generic to-do list: it models disciplines, nested projects, specialized task types, deadlines, typed attachments, progress calculation, and task activity history.

The current product is a local Android application built with Kotlin, Jetpack Compose, MVVM, `StateFlow`, and a repository abstraction. It intentionally avoids Room/SQLite for now and uses a simple local snapshot file for runtime persistence.

## Product Vision

The final product is a study-oriented task management application for Android. It helps users manage academic disciplines and all work items related to them: repository-based coursework, exam preparation, seminars, reading tasks, general tasks, and complex projects with nested subtasks.

The main goal of the project is to demonstrate real OOP usage in a practical Android application:

- inheritance through the `WorkItem`, `AtomicWorkItem`, and `CompositeWorkItem` hierarchy;
- polymorphism through task-specific progress calculation;
- composition through `ProjectTask`, which can contain nested `WorkItem` objects;
- encapsulation through controlled mutation methods such as `updateMetadata()`, `updatePages()`, `setUserStatus()`, and checklist operations;
- repository abstraction through `TaskRepository`, `LocalTaskRepository`, and `InMemoryTaskRepository`;
- reactive UI updates through `StateFlow`.

## Current Status

The current implementation provides a feature-complete local workflow:

- discipline CRUD;
- root task creation and deletion;
- nested project task structure;
- recursive work item lookup and deletion;
- metadata editing for tasks;
- deadlines, priority, status, estimates, created/updated timestamps;
- overdue detection, including nested subtasks;
- specialized task behavior;
- checklist interaction;
- typed attachments;
- GitHub-aware attachment parsing;
- attachment actions and mock sync/submit flows;
- work logs / activity timeline;
- dashboard with recursive analytics;
- global task search across all disciplines and nested subtasks;
- discipline-level task search, filters, and sorting;
- filters by status, priority, task type, overdue state, attachment purpose, GitHub attachments, and tasks with logs;
- sorting by deadline, priority, progress, and updated timestamp;
- persistence-ready record and mapper layer;
- simple local file persistence across application restarts.

The application state is loaded from a local snapshot file on startup and saved after repository mutations. If no saved snapshot exists yet, the app starts empty.

## Implemented Domain Model

### Disciplines

`Discipline` groups academic work items. It is not a task itself. It owns root-level `WorkItem` objects and can calculate aggregate progress and recursive overdue items.

Key responsibilities:

- store discipline metadata;
- group root work items;
- calculate discipline progress;
- detect overdue tasks recursively;
- preserve work items during metadata updates.

### WorkItem hierarchy

```text
WorkItem
├── AtomicWorkItem
│   ├── GenericTask
│   ├── ReadingTask
│   ├── ExamTask
│   └── SeminarTask
└── CompositeWorkItem
    └── ProjectTask
```

`WorkItem` contains shared task metadata:

- `title`;
- `description`;
- `status`;
- `priority`;
- `deadline`;
- `createdAt`;
- `updatedAt`;
- `estimatedMinutes`;
- `attachments`;
- `logs`.

Every concrete task type implements its own `calculateProgress()` and `validateCompletion()` logic.

### GenericTask

A general task that uses checklist progress. If it has no checklist and is not completed, its progress is `0%`. If it is marked as done, its progress is `100%`.

### ReadingTask

Tracks reading progress using:

- `readPages`;
- `totalPages`.

Progress is calculated as `readPages / totalPages`. Page updates are validated through `updatePages()`.

### ExamTask

Tracks exam preparation through a list of topics. Each topic has a confidence percentage. Progress is the average confidence across all topics.

### SeminarTask

Tracks seminar preparation through multiple preparation stages:

- topic selected;
- materials collected;
- speech prepared;
- slides prepared;
- rehearsal done.

### ProjectTask

A composite task that can contain any other `WorkItem`, including nested `ProjectTask` objects. Its progress is calculated from child tasks. Cancelled subtasks are excluded from aggregate project progress.

## Attachments

Attachments are no longer plain text fields. They are typed resources with purpose, metadata, validation, and task-specific meaning.

Supported attachment subtypes:

- GitHub repository link;
- Google Classroom link;
- generic web link;
- local file resource;
- cloud file resource.

Supported attachment purposes:

- source code;
- assignment brief;
- reference;
- submission;
- dataset;
- rubric;
- notes;
- output artifact;
- local resource;
- cloud resource.

### GitHub-specific behavior

GitHub attachments are parsed into structured repository information:

- owner;
- repository name;
- branch;
- canonical URL;
- clone URL;
- commits URL;
- issues URL;
- pull requests URL.

GitHub attachments are task-agnostic. They can be attached to any task that needs repository context, without forcing a separate task subtype or manual repository metrics into progress calculation.

Current GitHub sync is intentionally mocked. It refreshes a local placeholder snapshot with active issue count, open pull request count, last repository activity time, and importable issue / pull request candidates. Future real sync can replace that stub without changing the task model.

Importable GitHub candidates can be copied into an atomic task checklist. This keeps repository work actionable while preserving the existing checklist-based completion rules.

## Work Logs and Activity Timeline

Tasks can store activity log entries. Logs are used to represent manual notes, work sessions, and automatic history entries generated by important actions.

Examples of logged events:

- metadata update;
- status change;
- checklist update;
- reading progress update;
- exam topic update;
- seminar stage update;
- attachment added or removed;
- subtask added;
- manual work session.

This makes the application more than a CRUD interface. Each task can show its history and accumulated work context.

## Architecture

The project follows a layered Android architecture.

```text
Presentation layer
├── Jetpack Compose screens
├── ViewModels
├── UI models
└── UI-specific factories and forms

Domain layer
├── WorkItem hierarchy
├── Discipline
├── Attachments
├── Work logs
├── Progress calculation
└── Domain services/interfaces

Data layer
├── TaskRepository interface
├── LocalTaskRepository
├── InMemoryTaskRepository
└── Persistence-ready records and mappers
```

Current runtime storage is provided by `LocalTaskRepository`. It owns the in-process `StateFlow` state, uses the same mutation behavior as `InMemoryTaskRepository`, restores data from the local snapshot when it exists, starts empty otherwise, and writes a local snapshot after changes.

The domain layer is kept separate from Android UI. UI-specific input models such as `WorkItemEditResult` and attachment form results belong to the presentation layer.

## Local Persistence Layer

The project contains a persistence-ready mapping layer that is also used by the current lightweight local storage implementation.

Implemented persistence contract:

- `DisciplineRecord`;
- `WorkItemRecord`;
- `AttachmentRecord`;
- `ChecklistItemRecord`;
- `ExamTopicRecord`;
- `WorkLogEntryRecord`;
- `PersistenceBundle`;
- `PersistenceSnapshot`;
- domain-to-record mappers;
- record-to-domain restoration logic.

Runtime persistence is intentionally simple: `LocalTaskRepository` creates one source of truth in memory, writes a full snapshot after mutations, and restores the snapshot on the next launch. The domain model does not become a storage model.

## Development Progress

### Phase 1 — OOP Domain Model

Completed.

Implemented the central object model: `Discipline`, `WorkItem`, atomic tasks, composite tasks, attachments, progress snapshots, and basic completion rules.

### Phase 2 — Persistence-Ready Architecture

Completed.

Added flat record classes and mappers for future database integration. Room was intentionally not introduced at this stage.

### Phase 3A — Core In-Memory CRUD

Completed.

Implemented discipline CRUD, root task CRUD, project subtasks, recursive task lookup, recursive deletion, and reactive updates through `StateFlow`.

### Phase 3B — Specialized Task CRUD

Completed.

Added interactive behavior for `ReadingTask`, `ExamTask`, `SeminarTask`, and checklist-based `GenericTask`.

### Phase 3C — Metadata, Deadlines, and Validation Stabilization

Completed.

Added status, priority, deadline, estimated time, timestamps, date formatting, overdue state, recursive overdue detection, atomic metadata updates, and stricter validation rules.

### Phase 4A — Typed Attachments

Completed.

Converted attachments from plain text data into typed resources with purpose, validation, GitHub parsing, cloud provider detection, and task-specific meaning.

### Phase 4B — Work Logs and Activity Timeline

Completed.

Added manual logs, work session logs, automatic task history entries, and timeline-oriented UI data.

### Phase 5 — Dashboard, Search, Filters, and Analytics

Completed.

Added a dashboard and search/filter layer on top of the in-memory repository:

- dashboard route as the application start screen;
- recursive analytics through `AnalyticsService`;
- total, active, done, cancelled, overdue, due today, and due this week counters;
- average progress and workload time summaries;
- total estimated time and total logged time;
- progress and overdue counts by discipline;
- at-risk task detection;
- high-priority active task list;
- global recursive task search across all disciplines;
- discipline-level work list filtering;
- filters by status, priority, task type, overdue state, attachment purpose, GitHub repository, and logs;
- sorting by deadline, priority, progress, and updated date;
- empty states for dashboard/search/filter results;
- focused tests for analytics and recursive global search.

Recent activity on the dashboard is intentionally out of scope for this phase. Work logs remain available in task detail, and total logged time is included in analytics.

### Phase 6 — Simple Local Persistence

Completed.

Added lightweight snapshot persistence without Room or another database:

- app state is restored from `study_tasks.snapshot` in internal app storage;
- an empty state is used when no snapshot exists;
- repository mutations automatically save the latest snapshot;
- existing record/mapping layer is reused as the persistence contract;
- `LocalTaskRepository` is the runtime source of truth;
- `InMemoryTaskRepository` remains available for tests and non-persistent scenarios;
- local persistence is covered by unit tests.

## Roadmap

### Phase 7 — Real External Integrations

Planned after local persistence.

Possible integrations:

- real GitHub repository sync;
- fetch repository activity;
- fetch issue and pull request status;
- import selected GitHub issue and pull request items into task checklists;
- Google Classroom assignment metadata import;
- Google Classroom submission status;
- Android file picker;
- persisted URI permissions;
- cloud file opening;
- calendar export;
- deadline reminders.

These integrations are intentionally deferred because they add external API and permission complexity.

### Phase 8 — UX Polish and Release Preparation

Planned final stage.

Planned work:

- empty states;
- confirmation dialogs;
- undo delete;
- clearer validation messages;
- accessibility pass;
- responsive layout cleanup;
- visual consistency;
- sample screenshots;
- architecture diagram;
- test coverage improvement;
- final coursework documentation.

## Known Limitations

- Local persistence is snapshot-based, not a relational database.
- GitHub sync is mocked.
- Google Classroom sync and submit actions are mocked.
- Local file opening is not backed by Android file picker permissions yet.
- No background reminders or notifications are implemented yet.
- Recent activity is not shown on the dashboard by design; logs are available in task detail.

## Technical Debt

- Some UI screens are feature-rich and should be split further into smaller composables if they continue to grow.
- Search/filter controls are implemented separately for the work list and global search; a shared presentation helper could reduce duplication later.
- Snapshot persistence is intentionally simple; a database can still be added later if the coursework scope changes.
- ViewModel tests should be expanded after the current repository/domain tests are stable.

## Testing Status

The project includes unit tests for:

- progress calculation;
- repository operations;
- recursive project behavior;
- metadata validation;
- persistence mapper round trips;
- attachment enhancement logic;
- attachment and log repository behavior.
- recursive analytics calculations;
- recursive global search with attachment-purpose filtering.
- local snapshot save/load behavior.

Current check command:

```bash
./gradlew test
```

The command passes in the current development environment.

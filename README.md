# OOP2 Coursework — Android Study Task Organizer

An Android coursework project that demonstrates a object-oriented domain model for managing academic work. The application is not a generic to-do list: it models disciplines, nested projects, specialized task types, deadlines, typed attachments, progress calculation, and task activity history.

The current product is an in-memory Android application built with Kotlin, Jetpack Compose, MVVM, `StateFlow`, and a repository abstraction. It is intentionally not connected to Room/SQLite yet. The project first stabilizes domain behavior and UI workflows before adding persistent storage.

## Product Vision

The final product is a study-oriented task management application for Android. It helps users manage academic disciplines and all work items related to them: programming assignments, exam preparation, seminars, reading tasks, general tasks, and complex projects with nested subtasks.

The main goal of the project is to demonstrate real OOP usage in a practical Android application:

- inheritance through the `WorkItem`, `AtomicWorkItem`, and `CompositeWorkItem` hierarchy;
- polymorphism through task-specific progress calculation;
- composition through `ProjectTask`, which can contain nested `WorkItem` objects;
- encapsulation through controlled mutation methods such as `updateMetadata()`, `updatePages()`, `setUserStatus()`, and checklist operations;
- repository abstraction through `TaskRepository` and `InMemoryTaskRepository`;
- reactive UI updates through `StateFlow`.

## Current Status

The current implementation provides a feature-complete in-memory workflow:

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
- persistence-ready record and mapper layer.

The application state is still stored in memory. Data is expected to reset after application restart until the Room persistence phase is implemented.

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
│   ├── ProgrammingTask
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

### ProgrammingTask

Tracks programming assignment progress using:

- current commits;
- required commits;
- resolved issues;
- required issues;
- test progress;
- optional checklist;
- optional linked repository URL and branch.

Programming progress is calculated by normalized weighted components. Empty checklists no longer provide free progress.

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

When a GitHub attachment is added to a `ProgrammingTask`, the task can use it as its primary repository reference. The implementation keeps a clear extension point for future real GitHub API integration.

Current GitHub sync is intentionally mocked. Future sync can update programming statistics such as commits, issues, and test status.

## Work Logs and Activity Timeline

Tasks can store activity log entries. Logs are used to represent manual notes, work sessions, and automatic history entries generated by important actions.

Examples of logged events:

- metadata update;
- status change;
- checklist update;
- reading progress update;
- programming stats update;
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
├── InMemoryTaskRepository
├── DemoDataFactory
└── Persistence-ready records and mappers
```

Current runtime storage is provided by `InMemoryTaskRepository`. It uses `StateFlow` with revision-based updates to reliably notify the UI after in-place domain mutations.

The domain layer is kept separate from Android UI. UI-specific input models such as `WorkItemEditResult` and attachment form results belong to the presentation layer.

## Persistence-Ready Layer

The project already contains a persistence-ready mapping layer. It is not active runtime storage yet.

Implemented persistence contract:

- `DisciplineRecord`;
- `WorkItemRecord`;
- `AttachmentRecord`;
- `ChecklistItemRecord`;
- `ExamTopicRecord`;
- `WorkLogEntryRecord`;
- `PersistenceBundle`;
- domain-to-record mappers;
- record-to-domain restoration logic.

This layer is intended to make the future Room implementation safer. The domain model should not become a Room entity directly.

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

Added interactive behavior for `ReadingTask`, `ProgrammingTask`, `ExamTask`, `SeminarTask`, and checklist-based `GenericTask`.

### Phase 3C — Metadata, Deadlines, and Validation Stabilization

Completed.

Added status, priority, deadline, estimated time, timestamps, date formatting, overdue state, recursive overdue detection, atomic metadata updates, and stricter validation rules.

### Phase 4A — Typed Attachments

Completed.

Converted attachments from plain text data into typed resources with purpose, validation, GitHub parsing, cloud provider detection, and task-specific meaning.

### Phase 4B — Work Logs and Activity Timeline

Completed.

Added manual logs, work session logs, automatic task history entries, and timeline-oriented UI data.

## Roadmap

### Phase 5 — Dashboard, Search, Filters, and Analytics

Planned next.

The next major step is to use existing task data to provide a higher-level overview.

Planned features:

- dashboard screen;
- total tasks count;
- active, completed, cancelled, and overdue tasks;
- tasks due today;
- tasks due this week;
- total estimated time;
- total logged time;
- progress by discipline;
- overdue count by discipline;
- recent activity timeline;
- search by title and description;
- filter by status;
- filter by priority;
- filter by task type;
- filter by overdue state;
- filter by attachment purpose;
- filter by tasks with GitHub repository;
- sort by deadline;
- sort by priority;
- sort by updated date;
- sort by progress.

### Phase 6 — Room Persistence

Planned after analytics and filtering are stable.

The goal is to replace in-memory runtime storage with local SQLite persistence using Room while keeping the existing domain model intact.

Planned work:

- add Room and KSP dependencies;
- create Room entities from existing record models;
- create DAO interfaces;
- implement `RoomTaskRepository`;
- keep `TaskRepository` as the public abstraction;
- preserve `InMemoryTaskRepository` for tests and demo mode;
- add migrations;
- test domain-to-entity-to-domain round trips;
- verify nested `ProjectTask` restoration;
- verify attachments and logs persistence.

Important rule: domain classes must not become Room entities directly.

### Phase 7 — Real External Integrations

Planned after persistence.

Possible integrations:

- real GitHub repository sync;
- fetch commit count;
- fetch issue status;
- update `ProgrammingTask` stats from repository data;
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

- Runtime data is currently in memory only.
- Data is lost after application restart.
- GitHub sync is mocked.
- Google Classroom sync and submit actions are mocked.
- Local file opening is not backed by Android file picker permissions yet.
- Analytics service is still mostly a future extension point.
- No background reminders or notifications are implemented yet.

## Technical Debt

- Some UI screens are feature-rich and should be split further into smaller composables if they continue to grow.
- Analytics functionality should be promoted from placeholder service methods to a real dashboard layer.
- Room persistence should be implemented only after the domain behavior and analytics requirements are stable.
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

In this review environment, Gradle tests could not be executed because the Gradle wrapper attempted to download Gradle from `services.gradle.org`, and the environment had no internet access. The source code and test structure were inspected directly.

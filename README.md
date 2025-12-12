# CS-5800.01-Final-Startup-Submission

Jack Morris

Prof. Nima Davarpanah

CS 5800.01

December 12, 2025





> **IMPORTANT:**  
> If you need to run the project then the `.env` file containing the OpenAI API key must be added to the root directory.

## Steps

1. **Clone the repository**

bash

git clone https://github.com/jrmorris29/CS-5800.01-Final-Startup-Submission.git

cd CS-5800.01-Final-Startup-Submission




2. **Build the project and run tests**

mvn clean test




3. **Run the EchoNote demo**

EchoNote.App.AppMain






# Part 1

### Design Pattern Implementation

| # | Pattern    | Implementation |
|---|------------|----------------|
| 1 | **Singleton** | `OpenAiClientFactory` – single static instance |
| 2 | **Factory**   | `OpenAiClientFactory` – encapsulates client creation |
| 3 | **Builder**   | `MeetingRecordBuilder` – fluent API construction |
| 4 | **Iterator**  | `MeetingRecordIterator` + `MeetingRecordCollection` |
| 5 | **Proxy**     | `TranscriberProxy` – caching proxy for Transcriber |


Github Commit(s): https://github.com/jrmorris29/CS-5800.01-Final-Startup-Submission/commit/7c76814a5f214d3f41ccc7baf23a237f90cc78cc







# Part 2

## Major Features Addition

### 1. Persistent Meeting History *(Implemented by Jack Morris)*

**Overview**

EchoNote now persists all meetings to disk so the workspace survives app restarts.

**Key Files**

- `src/main/java/EchoNote/Jack/MeetingStorage.java`  
  Handles JSON serialization/deserialization of meeting records using Jackson.
- `src/main/java/EchoNote/Jack/MeetingRecordDto.java`  
  DTO with nested classes for serializing participants, transcript, summary, and action items.
- `src/main/java/EchoNote/Jack/StorageException.java`  
  Runtime exception for storage errors.
- `src/test/java/EchoNote/Jack/MeetingStorageTest.java`  
  Tests for persistence functionality.

**Notable Changes**

- `MeetingRecord.java` – Protected constructor added to support UUID restoration from storage.  
- `MeetingRecordBuilder.java` – `withId(UUID)` added for deserialization.  
- `Workspace.java` – Integrated `MeetingStorage` (auto-load on init, auto-save on modifications, `clearAll()` support).  
- `SearchService.java` – `clearIndex()` and auto-indexing of existing records on init.  
- `AppConfig.java` – Creates `MeetingStorage` and wires it into `Workspace`.  
- `SwingUI.java` – New **“Clear History”** button with confirmation dialog.  
- `pom.xml` – Added `jackson-datatype-jsr310` for Java 8 date/time support.

**Behavior**

- Meetings are saved to `echonote-meetings.json` in the project root.  
- On startup, previously saved meetings are automatically loaded.  
- After creating or updating a meeting, changes are automatically persisted.  
- The **“Clear History”** button removes all saved meetings (with confirmation).

---

### 2. Multi-Format Export *(Implemented by Arpit)*

**Overview**

Meetings can now be exported in multiple formats, with a UI flow for choosing the desired format.

**Key Files**

- `src/main/java/EchoNote/Arpit/MeetingExporter.java`  
  Interface for format-specific exporters.
- `src/main/java/EchoNote/Arpit/MarkdownExporter.java`  
  Markdown exporter (extracted from existing export logic).
- `src/main/java/EchoNote/Arpit/HtmlExporter.java`  
  HTML exporter with styled output.

**Notable Changes**

- `src/main/java/EchoNote/Jack/ExportFormat.java` – Added `HTML` to the enum.  
- `src/main/java/EchoNote/Arpit/ExportService.java` – Refactored to support multiple formats via `export(record, format)` while keeping `exportAsMarkdown()` backward compatible.  
- `src/main/java/EchoNote/App/SwingUI.java` – “Export as Markdown” button updated to **“Export Meeting”** with a format-selection dialog.  
- `src/test/java/EchoNote/Arpit/ExportServiceTest.java` – Tests for HTML export and multi-format behavior.  
- `src/test/java/EchoNote/Jack/WorkspaceTest.java` – Additional tests for `clearAll()` (ensuring integration with export and persistence).

**Behavior**

- Clicking **“Export Meeting”** opens a dialog to choose export format (Markdown or HTML).  
- HTML exports include basic CSS for a more professional look.  
- `ExportService.getSupportedFormats()` returns the list of available export formats.

---

### 3. Email Draft Flow *(Implemented by Mihail)*

**Overview**

EchoNote can now prepare an email draft of the meeting summary using the user’s default email client—no SMTP backend required.

**Key Files**

- `src/main/java/EchoNote/Arpit/EmailNotifier.java`  
  Extended to support client-assisted email draft creation.
- `src/main/java/EchoNote/App/SwingUI.java`  
  Updated “Email Summary” action and dialog flow.
- `src/test/java/EchoNote/Arpit/EmailNotifierTest.java`  
  Comprehensive tests for email validation, subject/body building, and error handling.
- `src/main/resources/uml/Arpit (Export, Search, Notify).puml`  
  Updated to show EmailNotifier relationships and the draft flow.
- `src/main/resources/uml/Master.puml`  
  Updated to include new persistence/export classes and email relationships.

**EmailNotifier Enhancements**

- `openEmailDraft(MeetingRecord record, String recipientEmail)` – Opens the default mail client with a prefilled draft.  
- `validateEmail(String email)` / `isValidEmail(String email)` – Validation helpers, throwing `InvalidEmailException` on invalid input.  
- `buildDraftSubject(MeetingRecord)` – Builds subject line: **"EchoNote Summary – {Meeting Title}"**.  
- `buildDraftBody(MeetingRecord)` – Formats the body similar to the Details panel (summary, topics, decisions, action items).  
- `openMailClient(String to, String subject, String body)` – Uses `Desktop.mail()` with a properly URL-encoded `mailto:` URI and truncates overly long bodies to avoid size limits.

**User Experience Flow**

1. Click **“Email Summary”** → dialog prompts for recipient email.  
2. Enter email → format validated via regex; invalid input shows an error message.  
3. Valid email → default mail client opens with:
   - **To:** entered address  
   - **Subject:** `"EchoNote Summary – {Meeting Title}"`  
   - **Body:** formatted summary with topics, decisions, and action items  
4. Cancel or empty input → no changes, handled gracefully.  
5. Mail client unavailable → user sees a clear error dialog.

**Design Notes**

- Reuses the existing `buildBody()` pattern from older console printing code.  
- Uses Java’s `Desktop.mail()` API for cross-platform mail client support.  
- Applies proper URL encoding with `URLEncoder` for `mailto` components and guards against URI length limits (~1800 chars).  
- Leverages the existing `InvalidEmailException`.  
- No SMTP or backend server required—pure client-assisted draft feature.  
- Backward compatible with the older `emailParticipants()` method.



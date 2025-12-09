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

Design Pattern Implementation:

### Design Pattern Implementation

| # | Pattern    | Implementation |
|---|------------|----------------|
| 1 | **Singleton** | `OpenAiClientFactory` – single static instance |
| 2 | **Factory**   | `OpenAiClientFactory` – encapsulates client creation |
| 3 | **Builder**   | `MeetingRecordBuilder` – fluent API construction |
| 4 | **Iterator**  | `MeetingRecordIterator` + `MeetingRecordCollection` |
| 5 | **Proxy**     | `TranscriberProxy` – caching proxy for Transcriber |


Github Commit(s): https://github.com/jrmorris29/CS-5800.01-Final-Startup-Submission/commit/7c76814a5f214d3f41ccc7baf23a237f90cc78cc


# Development

## Module Structure

💡*For a visual representation of the module structure, see
this [diagram](./.images/project-architecture.png).*

[gradle]: https://docs.gradle.org/current/userguide/userguide.html

The project use [Gradle build tool][gradle]. The main modules (directories) are:

- `rule-engine`: Implements the game board and the rules.
- `ai-engine`: An rule-based AI engine that generates computer moves for single-player mode.
- `server`: An HTTP server supports account management and online 2 player mode.
- `client`: An client for the server that encapsulates the HTTP requests.
- `protocol`: A shared module that contains the data classes and interfaces for the communication
  between the client and the server.
- `app`: The Android App.

Supplementary modules include:

- `utils`: Logging and coroutines utilities shared by all modules.
- `docs`: All documentations.
- `buildSrc`: Contains build scripts.

## Building and Running

See [Build](build) for details on building the Android app and running the server.

## Modules

- [Android App](app/README)
- [Rule Engine](rule-engine/README)
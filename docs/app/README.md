# Android APP

## Architecture

💡*For a visual representation of the module structure, see
this [diagram](../.images/project-architecture.png).*

Just like typical Android app development, Keizar consists of several layers from the outside
in:
UI layer, domain layer, data/network layer

## UI Layer

The UI layer primarily uses the MVVM pattern. In the MVVM design pattern, the Model part in Keizar
is
the data classes provided . The View and ViewModel parts are located in the `ui`
directory, with associated Views and ViewModels placed in the same directory.

### Organization of the UI Layer

In Android, Keizar has only one Activity, `MainActivity`.

The content of an Activity is a Screen. MainActivity uses MainScreen.

A Screen is a container for a navigation controller, supporting transitions between multiple scenes.
The content of a Scene is a Page. The Scene connects Pages, which are unrelated to navigation, to
the navigation system.

Typically, a Page is used by only one Scene. This design is just to avoid considering
navigation-related compatibility issues during UI preview.
A Page is a container that actually contains UI widgets (Column, Button, etc.).

## Data Layer


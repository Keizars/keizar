# Server

The server-side application of KEIZAR is built using the [Ktor](https://ktor.io/) framework.

The server is responsible for handling the user's accounts and account-related data, 
as well as managing the game rooms for online 2-player games.

For a visual representation of the module structure, see this [diagram](../.images/project-architecture.png).

## Running the server

See [Build](../build.md#running-the-server).

## ServerContext

The `ServerContext` class represents a context object for the server application in order to 
achieve dependency injection. It creates and assembles all the business layer and data layer services
and are used by the server's HTTP routing in the access layer.

Instances of `ServerContext` can be created using the constructor or using the `setupServerContext` function.

See the documentation in the `ServerContext` class file for detailed usage.

## Access Layer

The access layer is responsible for handling the HTTP requests and responses as well as
the websocket connections. It is also responsible for the authentication of the users.

### Authentication

The server uses bearer tokens for authentication. The `AuthTokenManager` class in the `utils` 
package is responsible for creating and validating the tokens. 
Currently, it uses the AES algorithm to encrypt the user's ID and the expiration time of the token.

The authentication is installed as a Ktor plugin in `plugins/Security.kt`

### HTTP Routing

The HTTP routing functions use [Ktor's routing API](https://ktor.io/docs/routing-in-ktor.html) 
and are defined in the `routing` package. They are installed in `plugins/Routing.kt`.

The routing functions usually does not involve any business logic. 
Instead, they call the business layer services to complete the requests, and then send
appropriate HTTP responses to reflect the result.

## Business Layer

The business layer contains all business logic of the server application separated into modules.
The modules are defined in the `logic` package.

The job of most modules are to provide methods to access and manipulate the data in a certain table 
in the database. It is done by using the data layer service instances injected into the module as 
constructor parameters.

The exception is the `GameRoomModule`. It is responsible for managing the online 'game rooms'.
A game room is a stateful object that maintains an online game session with 2 clients. 
The operation logic of a game room is as follows:
1. The host client makes an HTTP request to create a game room and get a room number in the response.
2. The host client share the room number with a guest client, and then the guest client makes an 
   HTTP request to join the room via the room number.
3. The two clients connects to the room respectively via websocket. The following communications 
   are all via websocket.
4. The host client may send a `ChangeBoard` message to propose a new `BoardProperties` for the upcoming game.
5. The two clients may send `SetReady` messages to the room.
6. After both clients are ready, the game starts. The room will create a `GameSession` using the 
   proposed `BoardProperties` and will send the snapshot of the `GameSession` to the two players.
7. The two clients both construct a `RemoteGameSession` from the snapshot received from the server.
8. One client make a valid move on the `RemoteGameSession`, and then the `RemoteGameSession` sends 
   the moves to the server by a `Move` message. 
9. As the server room receives the move, it updates its own `GameSession`, and if the move is valid, 
   forwards the move to the other client.
10. The other client's `RemoteGameSession` receives the move, and updates itself accordingly.
11. Step 8 to 10 are repeated until the round ends. When one round is finished, both clients sends 
    a `ConfirmNextRound` message to the server to proceed to the next round, and then repeat step 8 to 10.
12. When the whole game is finished, the client disconnects from the room, and the room is destroyed.
- If a client disconnects from the room before the game ends, the room will mark the client as `DISCONNECTED`. 
  If the client reconnects, the room will send a snapshot of its own `GameSession` to the client, 
  so that the client can recover the game state and continue the game. 
  If both clients disconnect before the game ends, the room will be destroyed after waiting for a 
  certain period of time (currently 5 minutes).

## Data Layer

The data layer is responsible for handling the persistent data storage. 
It is defined in the `data` package. The data layer contains two components, 
the database and the static object storage.

The database is managed by the `DatabaseManager` class. Refer to the documentation in the class file
for its structure and detailed usage.

The static object storage currently only contains the `AvatarStorage` class, which is used to
store and retrieve the avatar images of the users.
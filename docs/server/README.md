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
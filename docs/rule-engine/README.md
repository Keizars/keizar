# Rule Engine

The rule engine functions as the central component of a game program.
It is responsible for defining and enforcing the game rules, managing the game state, and 
validating player interactions.

The mutable states of the game are stored in `StateFlow`s instead of variables, so that states
can be actively updated and observed by the frontend components, instead of being passively queried.
If you are not familiar with Kotlin Flows, you can refer to [the official Android tutorial](https://developer.android.com/kotlin/flow).


## Public API

### BoardProperties
The `BoardProperties` is a data class that encapsulates the basic game rules and 
the board setup. 

To set up a `BoardProperties`, you can call `BoardProperties.getPlainBoard()`
to get an object with standard game rules and a board setup with no special tiles except the 
KEIZAR tile. You can also all `BoardProperties.getStandardProperties()` with a random seed to 
get an object with standard rules a random board setup generated according to the KEIZAR rulebook.

You can also create a customized `BoardProperties` via the `BoardPropertiesBuilder` factory class
using DSL. An example usage is shown below:
```kotlin
val customProperties = BoardPropertiesBuilder(
    prototype = BoardPropertiesPrototypes.Standard(seed = 100),
) {
    winningCount { 5 }
    roundsCount { 4 }
    tiles {
        change("d5" to TileType.PLAIN)
        change("a1" to TileType.KEIZAR)
        change("e8" to TileType.BISHOP)
    }
}.build()
```

### RoundSession
The `RoundSession` class represents a single round of the KEIZAR game. 
In each round, players compete to stay in the KEIZAR tile for 3 opponent's turns.
When one round is finished, the pieces are reset and the players switch sides.

The `RoundSession` class maintains the state of a single round. The states can be observed via
the fields of the class:
```kotlin
val pieces: List<Piece>
val winner: StateFlow<Role?>
val winningCounter: StateFlow<Int>
val curRole: StateFlow<Role>
val canUndo: StateFlow<Boolean>
val canRedo: StateFlow<Boolean>
```

Players can interact with the game by calling the methods of the class. Examples include:
```kotlin
suspend fun move(from: BoardPos, to: BoardPos): Boolean
suspend fun undo(role: Role): Boolean
suspend fun redo(role: Role): Boolean
```

Players can also call the query methods to get useful information about the current round. Examples include:
```kotlin
fun getAvailableTargets(from: BoardPos): List<BoardPos>
fun getAllPiecesPos(role: Role): List<BoardPos>
fun getLostPiecesCount(role: Role): StateFlow<Int>
fun pieceAt(pos: BoardPos): Role?
```

### GameSession
The `GameSession` class is the most important class and also the entry point of this module. 
It represents a whole KEIZAR game consisting of 2 rounds. The two rounds have the exact same 
game rules and board setup, except that the players switch sides in the second round. 

The `GameSession` class maintains the instances of `RoundSession` and other game states 
and properties, including a `BoardProperties` object which stores the randomly generated
board configuration and a set of game rules. They can be accessed via the fields of the class:
```kotlin
val properties: BoardProperties
val rounds: List<RoundSession>
val currentRound: Flow<RoundSession>
val currentRoundNo: StateFlow<Int>
val finalWinner: Flow<GameResult?>
```

Similar to `RoundSession`, players can interact with the game by calling the methods of the class:
```kotlin
suspend fun confirmNextRound(player: Player): Boolean
fun replayCurrentRound(): Boolean
fun replayGame(): Boolean
```

And players can also call methods to get useful information about the current game:
```kotlin
fun currentRole(player: Player): StateFlow<Role>
fun wonRounds(player: Player): Flow<Int>
fun lostPieces(player: Player): Flow<Int>
fun getRoundWinner(roundNo: Int): Flow<Player?>
```

You can use the `GameSession.create` factory method to create a `GameSession` instance.
You can optionally pass in a seed or a `BoardProperties` object to customize the game rules and board setup.

Additionally, you can also obtain a serializable snapshot `GameSnapshot` of a `GameSession` instance by calling `GameSession.getSnapshot()`, 
and restores a `GameSession` instance from the `GameSnapshot` by calling `GameSession.restore()`.
You can also build your own `GameSnapshot` through the `GameSnapshotBuilder` factory class using DSL,
so that you can create `GameSession` in a customized state, convenient for testing and debugging.
An example usage is shown below:
```kotlin
val gameSnapshot = buildGameSnapshot {
    properties {
        tiles { change("c3" to TileType.QUEEN) }
    }
    round {
        curRole { Role.BLACK }
        resetPieces {
            white("c1")
            black("g7")
            black("d5")
        }
        winner { Role.BLACK }
    }
    val round2 = round {}
    setCurRound(round2)
}

val game = GameSession.restore(gameSnapshot)
```

## Internal Classes

### RuleEngine
The `RuleEngineImpl` internal class contains the actual implementation of the game logic.
It keeps track of all pieces on the board, records move history, and determines and updates the winner.

It implements the `RuleEngine` interface which defines the minimal set of methods needed to
interact with the game. The `RoundSession` depends on an implementation of `RuleEngine` 
and delegate most of its work to it.

### Board
The `Board` internal class represents a physical game board.
It contains a list of tiles and a list of pieces, and is used by the `RuleEngineImpl` 
to query and update the pieces positions on the board.
It uses the `RuleEngineCore` instance passed in its constructor to determine the validity of moves.

### RuleEngineCore
The `RuleEngineCore` interface contains only one method, `showValidMoves()`.
It is used to determine the valid moves of a piece on the board according to the KEIZAR rule book.
It is implemented in an adaptable and extensible way, so that it can quickly adapt to
any changes in the game rules, or be modified to support variant of the KEIZAR game.
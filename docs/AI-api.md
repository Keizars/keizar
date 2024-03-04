# AI api

## Introduction
The AI API is used to send the parameters to the AI model and get the next move from the model. The AI model is trained to predict the result based on the input parameters, which is trained using the historical data and the model is used to predict the future data. The current model is based on Q-table learning. In the future, it can be improved by DNN with reinforcement learning.

## API
### Get Board information
The parameters passed to the API `findBestMove` are `round: RoundSession` and `role: Role`, which records the current round state and which role the AI is. The return value is the best move for the current round. The `game: GameSession` is also used to show key information of the game.

The board information can be collected by the properties of the round session.
1. pieces positions of role on the board: `roundSession.getALlPiecesPos(role).first()`, where role can be `Role.BLACK` or `Role.WHITE`.
2. board seed: `game.properties.seed`
3. possible moves: 
    ```kotlin
            val moves = round.getAllPiecesPos(role).first().flatMap { source ->
                round.getAvailableTargets(source).first().map { dest ->
                    Move(source, dest, round.pieceAt(dest) != null)
                }
            }
    ```
4. tiles arrangement: `game.properties.tileArrangement`
5. keizar position: `game.properties.keizarPos`

### HTTP Request
The HTTP request is a POST request to the endpoint `/AI/black` or `/AI/white` based on the role of ai. The request body is a JSON object with the following fields:
We can set the body by using `buildJsonObject` with `put(${key}, ${value})` and `add(${value})` to build the JSON object.
Then we should get the move from the response body and parse to the format of `Pair<BoardPos, BoardPos>`.

## Examples
```kotlin
override suspend fun findBestMove(round: RoundSession, role: Role): Pair<BoardPos, BoardPos> {
    val moves = round.getAllPiecesPos(role).first().flatMap { source ->
        round.getAvailableTargets(source).first().map { dest ->
            Move(source, dest, round.pieceAt(dest) != null)
        }
    }

    val blackPiece = round.getAllPiecesPos(Role.BLACK).first()
    val whitePiece = round.getAllPiecesPos(Role.WHITE).first()
    val seed = game.properties.seed
    val resp = client.post(
        endpoint + "/AI/" + if (role == Role.BLACK) "black" else "white"
    ) {
        contentType(Application.Json)
        setBody(buildJsonObject {
            put("move", buildJsonArray {
                for (m in moves) {
                    add(buildJsonArray {
                        add(m.source.row)
                        add(m.source.col)
                        add(m.dest.row)
                        add(m.dest.col)
                        add(m.isCapture)
                    })
                }
            })
            put("black_pieces", buildJsonArray {
                for (p in blackPiece) {
                    add(buildJsonArray {
                        add(p.row)
                        add(p.col)
                    })
                }
            })
            put("white_pieces", buildJsonArray {
                for (p in whitePiece) {
                    add(buildJsonArray {
                        add(p.row)
                        add(p.col)
                    })
                }
            })
            put("seed", buildJsonArray { add(seed) })
        })
    }
    val move = resp.body<List<Int>>()
    return BoardPos(move[0], move[1]) to BoardPos(move[2], move[3])
}
```

## Conclusion
The AI API can give information of the current board state which can be used for future AI development. More parameters and information can be got from `game:GameSession` and `round:RoundSession` to improve the AI model, which can be improved by using DNN with reinforcement learning in the future.

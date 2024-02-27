# AI Algorithm Strategy for Keizar

## 1. Overview

The AI strategy for Keizar combines three key components to enhance single-player mode. First, it introduces the Distance Board to map potential moves, aiding AI strategy. Then, through strategy optimization, it analyzes game scenarios to adjust tactics. Finally, AI parameter tuning refines decision-making, balancing aggression with strategy. This integrated approach aims to create a challenging and strategic AI, enriching single-player experience with depth and engagement.
## 2. Distance Board Generation

### 2.1 Distance Board Setup

The distance board is structured as a 2D mutable list of TileNode objects. Each TileNode encompasses several key attributes: its position on the board, an occupancy status indicating whether it is occupied, the shortest distance to Keizar, and a list of parent nodes. This list also includes the distance to each parent node via the path from it. The TileNode is designed to support various implementations such as KeizarNode, DefaultNode, and NormalNode to accommodate different types of nodes within the game's architecture.
```kotlin
interface TileNode {
    val position: BoardPos
    var occupy: Role?
    var distance: Int
    val parents: MutableList<Pair<TileNode, Int>>
}
```
The setup begins with all board nodes marked as DefaultNodes and assigned a default distance of `Int.MAX_VALUE`, signifying a large, undefined distance from the Keizar tile, except for the Keizar's position at `BoardPos("d5")`, which starts at a distance of 0. This initial state is then updated through an iterative process using the distance board update algorithm, recalculating each tile's distance to the Keizar tile. The algorithm proceeds until reaching its termination, ensuring all tiles display the shortest path to the Keizar, thereby fully updating the board's distance metrics.
### 2.2 Distance Board Update Algorithm

#### 2.2.1 Breadth-First Search (BFS)

The algorithm leverages a Breadth-First Search (BFS) strategy, using a FIFO queue to explore nodes. 
1. It starts with the KeizarNode in the queue, then iteratively processes each node by removing it from the queue, identifying potential source nodes via a rule engine, and updating distances when applicable. 
2. If the target node's distance plus one is smaller than a source node's distance, the source node's distance is updated and added to the queue. 
3. Otherwise, only the parent-distance pair is recorded, without updating the distance or queue.
4. The algorithm concludes once all nodes are assessed, updating them with the shortest paths to the Keizar tile, except for unreachable or far-removed positions.

#### 2.2.2 Rule Engine
The rule engine validates movement legality, allowing moves to target tiles, occupied or not, barring any obstructing pieces on the path. It supports capture and normal forward moves, adhering to the game's complex rules, ensuring versatile gameplay adaptability.
### Using the Distance Board
To use the distance board effectively, the algorithm employs the shortest distance to the Keizar node for optimal pathfinding. When direct movement is blocked, it explores parent nodes for alternative routes, iterating until the best move is found, maximizing strategic advantage within game constraints.

## 3. Strategy Optimization
### 3.1 Forbidden Moves
The game's strategy includes specific forbidden moves, detailed in the rulebook and derived from gameplay experience. Moves from `BoardPos("c8")`, `BoardPos("e8")`, and `BoardPos("d7")` advancing two tiles forward on a plain tile are prohibited. Additionally, experience-based rules advise against moves to `BoardPos("d4")` for WHITE and `BoardPos("d6")` for BLACK on plain tiles, informed by strategic considerations and observed outcomes. These guidelines enhance the game's depth by steering players towards more thoughtful decisions.

### 3.2 Keizar Occupation Strategy
The Keizar Occupation Strategy is a dynamic approach that adapts to the state of the game, focusing on both offensive and defensive maneuvers to secure victory. It involves two key steps:
1. Capturing the Occupying Opponent: When the opponent controls Keizar, the strategy emphasizes aggressive tactics to recapture this central position. It involves identifying and executing the most effective moves to dislodge the opponent's piece from Keizar, viewing control of this tile as crucial for a comeback or to maintain the balance of power in the game.
2. Strategic Occupation of Surrounding Tiles: If Keizar is not under the opponent's control, the strategy shifts towards expanding territorial dominance by occupying tiles within a 1 to 3 distance range from Keizar. This not only solidifies one's presence around this critical area but also strategically positions the player to swiftly occupy Keizar when the timing is right
This strategic framework underscores the importance of adaptability, allowing players to seamlessly switch between direct aggression to recapture Keizar and a more subtle, territorial expansion to ensure strategic superiority. The ultimate goal is to balance these approaches, leveraging controlled aggression and strategic foresight to navigate the game's complexities and secure victory.

## 4. AI Parameter Tuning
The parameters mentioned are crucial components of the strategic framework designed to enhance gameplay and decision-making in the context of the Keizar Occupation Strategy:

```kotlin
class AIParameters(
    val keizarThreshold: Int = 0,         
    val possibleMovesThreshold: Int = 3,    
    val noveltyLevel: Double = 0.99,
    val allowCaptureKeizarThreshold: Double = 0.3
)  
```
1. keizarThreshold: Sets a limit for opponent's potential occupation of Keizar, influencing defensive or aggressive play.
2. possibleMovesThreshold: Determines the strategic capture range around Keizar, focusing on relevant moves for occupation strategy.
3. noveltyLevel: Balances the use of algorithm-generated strategic moves against random moves, adding unpredictability. 
4. allowCaptureKeizarThreshold: Filters candidate moves for capturing Keizar, optimizing strategy for defense or occupation.

Together, these parameters form a comprehensive system for guiding players through the strategic complexities of the game, enabling them to make informed decisions based on the current state of play, the behavior of their opponent, and the strategic objectives at hand.
## 5. Conclusion
In conclusion, the AI algorithm strategy for Keizar meticulously combines distance board generation, strategy optimization, and AI parameter tuning to enhance the single-player experience. This multifaceted approach results in an AI opponent that is not only challenging but also deeply strategic, capable of adapting to diverse gameplay scenarios. The development of this robust AI system significantly enriches the tactical depth of Keizar, offering players a more engaging and rewarding challenge.
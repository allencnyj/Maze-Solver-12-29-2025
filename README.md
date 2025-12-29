Maze Generator and Solver
Allen Chen


1. Project Overview:

This project implements a random maze generator and solver in Java.

The program:
- Treats each cell of a 2D grid as a node in a graph.
- Uses a simplified version of Prim’s Algorithm to generate a spanning tree.
- Uses Depth-First Search (DFS) to find a path from a start cell to a goal cell.
- Prints the maze as ASCII art and can highlight the solved path.

The design follows the specification:
- Graph representation using an adjacency list (Map<Cell, List<Cell>>).
- Maze generation with a simplified Prim’s algorithm.
- Maze solving with DFS.
- Java classes: Cell, Maze, MazeSolver, Visualization, and Main.



2. Maze Representation:

Each maze cell is represented by a Cell object with:
- row: int
- col: int

The maze is modeled as an undirected graph:
- Nodes: all Cell objects in the rows × cols grid.
- Edges: connections between adjacent cells where there is no wall.

Representation choice:
- Adjacency list:
    Map<Cell, List<Cell>> adjacencyList;

Why I chose adjacency list:
- The maze is sparse so adjacency list is efficient.
- It is easy to iterate over all neighbors of a given cell when running DFS.



3. Maze Generation (Simplified Prim’s Algorithm):

The maze generation algorithm builds a random spanning tree over the grid.

How it works?:
1) Start with a random cell and mark it as part of the maze.
2) Add all its neighbors (up, down, left, right) to a frontier list.
3) Repeatedly:
   - Choose a random cell from the frontier.
   - Among that cell’s neighbors that are already in the maze, pick one at random.
   - “Connect” the frontier cell to this visited neighbor (add an undirected edge in the adjacency list).
   - Mark the chosen frontier cell as being in the maze.
   - Add its unvisited neighbors into the frontier.
4) Stop when the frontier is empty.

This creates a spanning tree.

Implementation details:
- I keep a 2D boolean array inMaze[r][c] to track which cells are already in the maze.
- A List<Cell> frontier stores the current frontier cells. I select a random index from this list.
- The connect(Cell a, Cell b) method:
    - Adds b to adjacencyList.get(a)
    - Adds a to adjacencyList.get(b)
- Because each new cell is connected exactly once to the existing maze, the resulting graph is a tree (no cycles) and there is at least one path between any two cells.



4. Maze Solving (DFS):

To solve the maze, I use DFS on the adjacency list.

Goal:
- Find a path from the start cell (0, 0) to the goal cell (rows - 1, cols - 1).

Algorithm steps:
1) Start DFS at the start cell.
2) Maintain:
   - visited: Set<Cell> to avoid revisiting cells.
   - parent: Map<Cell, Cell> to remember how we reached each cell.
3) For each current cell:
   - If it is the goal, stop and return success.
   - Otherwise, recursively visit all unvisited neighbors from maze.getNeighbors(current).
4) When DFS finds the goal, I reconstruct the path by following the parent map backward:
   - Start from the goal, repeatedly look up parent[current], and push each cell into a list.
   - Reverse this list to obtain the path from start to goal.

This path is then passed to the Visualization class to mark the solution visually.



5. Visualization:

The maze is printed using ASCII characters.

Walls:
- Horizontal walls: "---"
- Vertical walls: "|"
- Corners: "+"

Cells:
- " S " – start cell
- " G " – goal cell
- " * " – cells that are part of the solution path (when solved)
- "   " – all other cells (open spaces)

Rules:
- If two horizontally adjacent cells are connected in the adjacency list, I do NOT draw a vertical wall between them.
- If two vertically adjacent cells are connected, I do NOT draw a horizontal wall between them.
- The outer border of the maze is always closed with walls.

The Visualization class simply calls:
- maze.printMaze() : to show the generated maze only
- maze.printMazeWithPath(path) : to show the maze with the solution path



6. How Each Class Works:

1) Cell
   - Fields: row, col (both int).
   - Methods:
     - Getters: getRow(), getCol().
     - equals() and hashCode() so that Cell works correctly as a key in HashMap/HashSet.
     - toString() for debugging.

2) Maze
   - Fields:
     - rows, cols
     - Cell[][] grid – stores all Cell objects.
     - Map<Cell, List<Cell>> adjacencyList – graph structure.
     - Random random – for generating random choices.
   - Responsibilities:
     - Initialize the grid and adjacency list.
     - Implement Prim’s algorithm (generate()).
     - provide methods: getCell(int row, int col), getNeighbors(Cell cell).
     - Check if two neighboring cells are connected: areConnected(Cell a, Cell b).
     - Build ASCII strings for displaying the maze (with or without a path).

3) MazeSolver
   - Field:
     - Maze maze
   - Responsibilities:
     - Public method solve(Cell start, Cell goal) that runs DFS and returns a List<Cell> path.
     - Private recursive helper dfs(...) that performs the actual depth-first traversal.

4) Visualization
   - Static helper methods:
     - showMaze(Maze maze)
     - showMazeWithPath(Maze maze, List<Cell> path)

5) Main
   - Responsibilities:
     - Interact with the user.
     - Read maze dimensions from input (e.g., "8, 8" or "8 8").
     - Create a Maze object and call maze.generate().
     - Print the generated maze.
     - Create a MazeSolver, solve from start to goal.
     - Print the maze with the solution path if one is found.
     - Handles invalid dimension input and falls back to a default size (8 × 8).


7. How to Compile/Run:

1) Compile:
   javac Main.java

2) Run:
   java Main

3) When prompted, enter the maze size, for example:
   8, 8



8. Design Choices and Tradeoffs

- Adjacency List vs Matrix:
  I chose an adjacency list because our maze graph is sparse and this makes DFS simpler and more efficient. An adjacency matrix would use more memory and require scanning entire rows to get neighbors.

- Randomized Prim’s Algorithm:
  Using a simplified Prim’s algorithm ensures:
  - The maze is fully connected (there is at least one path between start and goal).
  - There are no cycles (it is a tree), so DFS will not encounter loops.

- DFS for Solving:
  DFS is easy to implement recursively and is sufficient since the maze has no cycles. It will always find a path between S and G if one exists. In this tree, any found path is unique.

- Path Reconstruction via Parent Map:
  Instead of storing the full path during DFS, I keep a parent map from child to parent cell. Once the goal is found, this makes reconstructing the path straightforward and efficient.

- ASCII Visualization:
  I chose a simple text-based representation using +, -, and | characters. This fits the example provided in the word document to visualize the maze using simple ASCII symbols without needing any GUI libraries.

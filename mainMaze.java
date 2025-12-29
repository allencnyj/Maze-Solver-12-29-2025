import java.util.*;

/*
 CS 2336 Term Project - Maze Generator and Solver
 Maze represented as a graph
 Maze generated with a simplified Prim's Algorithm
 Maze solved with Depth-First Search (DFS)
 ASCII visualization with solved path
*/
public class mainMaze {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Maze Generator and Solver ===");
        System.out.print("Please enter the maze dimensions (rows, columns): ");

        String line = scanner.nextLine().trim();
        int rows;
        int cols;

        // Parse the input: for example - "8, 8" or "8 8"

        int[] dims = parseDimensions(line);
        rows = dims[0];
        cols = dims[1];

        Maze maze = new Maze(rows, cols);

        // Prim's algorithm

        maze.generate();

        System.out.println("\nGenerated Maze:\n");
        Visualization.showMaze(maze);

        // top-left

        Cell start = maze.getCell(0, 0);

        // bottom-right

        Cell goal  = maze.getCell(rows - 1, cols - 1);

        MazeSolver solver = new MazeSolver(maze);

        // solves maze using DFS

        List<Cell> path = solver.solve(start, goal);         

        if (path.isEmpty()) {
            System.out.println("No path found from start to goal.");
        } else {
            System.out.println("\nSolved Maze (S = start, G = goal, * = path):\n");
            Visualization.showMazeWithPath(maze, path);
        }

        scanner.close();
    }

    /*
    Parses the user's dimension input.
    Accepts formats like "8, 8" or "8 8".
    Defaults to 8x8 if parsing fails.
    */

    private static int[] parseDimensions(String line) {
        int rows = 8;
        int cols = 8;

        try {
            if (line.contains(",")) {
                String[] parts = line.split(",");
                rows = Integer.parseInt(parts[0].trim());
                cols = Integer.parseInt(parts[1].trim());
            } else {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    rows = Integer.parseInt(parts[0]);
                    cols = Integer.parseInt(parts[1]);
                } else {
                    System.out.println("Invalid input. Using default 8 x 8 maze.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Using default 8 x 8 maze.");
            rows = 8;
            cols = 8;
        }

        if (rows <= 0 || cols <= 0) {
            System.out.println("Dimensions must be positive. Using default 8 x 8 maze.");
            rows = 8;
            cols = 8;
        }

        return new int[]{rows, cols};
    }
}

/*
 Represents a single cell in the maze grid.
 Each cell represents a node on the graph.
*/

class Cell {
    private final int row;
    private final int col;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Cell)) return false;
        Cell other = (Cell) obj;
        return this.row == other.row && this.col == other.col;
    }

    @Override
    public int hashCode() {
        return 31 * row + col;
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}

/*
 Maze class:
 Stores the grid of cells
 Stores the graph as an adjacency list (Map<Cell, List<Cell>>)
 Generates a randomized maze using a simplified Prim's Algorithm
 Provides methods to visualize the maze as ASCII
*/

class Maze {
    private final int rows;
    private final int cols;
    private final Cell[][] grid;
    private final Map<Cell, List<Cell>> adjacencyList;
    private final Random random;

    public Maze(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Cell[rows][cols];
        this.adjacencyList = new HashMap<>();
        this.random = new Random();
        initializeGrid();
    }

    /*
     Initializes the grid of cell objects and sets up the adjacency list keys.
    */

    private void initializeGrid() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = new Cell(r, c);
                grid[r][c] = cell;
                adjacencyList.put(cell, new ArrayList<Cell>());
            }
        }
    }

    /*
     Returns the Cell location (which column/row)
    */

    public Cell getCell(int row, int col) {
        return grid[row][col];
    }

    /*
     Returns the neighbors of a cell in the graph.
    */

    public List<Cell> getNeighbors(Cell cell) {
        List<Cell> neighbors = adjacencyList.get(cell);
        if (neighbors == null) {
            return Collections.emptyList();
        }
        return neighbors;
    }

    /*
     Returns the up/down/left/right in the grid regardless of whether walls are opened.
    */

    private List<Cell> getGridNeighbors(Cell cell) {
        List<Cell> neighbors = new ArrayList<>();
        int r = cell.getRow();
        int c = cell.getCol();

        // Up

        if (r > 0) {
            neighbors.add(grid[r - 1][c]);
        }

        // Down

        if (r < rows - 1) {
            neighbors.add(grid[r + 1][c]);
        }

        // Left

        if (c > 0) {
            neighbors.add(grid[r][c - 1]);
        }

        // Right

        if (c < cols - 1) {
            neighbors.add(grid[r][c + 1]);
        }

        return neighbors;
    }

    /*
     Connects two cells in the graph (opens a wall between them).
    */

    private void connect(Cell a, Cell b) {
        adjacencyList.get(a).add(b);
        adjacencyList.get(b).add(a);
    }

    /*
     Checks if two cells are connected (if there is no wall between them).
    */

    public boolean areConnected(Cell a, Cell b) {
        List<Cell> neighbors = adjacencyList.get(a);
        return neighbors != null && neighbors.contains(b);
    }

    /*
     Generates a random maze using a simplified Prim's Algorithm to create a spanning tree over the grid.
    */

    public void generate() {
        boolean[][] inMaze = new boolean[rows][cols];
        List<Cell> frontier = new ArrayList<>();

        // 1. Start with a random cell

        int startRow = random.nextInt(rows);
        int startCol = random.nextInt(cols);
        Cell start = grid[startRow][startCol];
        inMaze[startRow][startCol] = true;

        // Add neighbors of the start cell to the frontier

        for (Cell neighbor : getGridNeighbors(start)) {
            if (!inMaze[neighbor.getRow()][neighbor.getCol()]) {
                if (!frontier.contains(neighbor)) {
                    frontier.add(neighbor);
                }
            }
        }

        // 2. While there are cells in the frontier:

        while (!frontier.isEmpty()) {

            // Choose a random cell from the frontier

            int index = random.nextInt(frontier.size());
            Cell current = frontier.remove(index);
            int r = current.getRow();
            int c = current.getCol();

            // Find neighbors of 'current' that are already in the maze

            List<Cell> visitedNeighbors = new ArrayList<>();
            for (Cell neighbor : getGridNeighbors(current)) {
                if (inMaze[neighbor.getRow()][neighbor.getCol()]) {
                    visitedNeighbors.add(neighbor);
                }
            }

            // Connect 'current' to one random visited neighbor

            if (!visitedNeighbors.isEmpty()) {
                Cell neighbor = visitedNeighbors.get(random.nextInt(visitedNeighbors.size()));
                connect(current, neighbor);
            }

            // Mark 'current' as part of the maze

            inMaze[r][c] = true;

            // Add its (unvisited) neighbors to the frontier

            for (Cell neighbor : getGridNeighbors(current)) {
                if (!inMaze[neighbor.getRow()][neighbor.getCol()] && !frontier.contains(neighbor)) {
                    frontier.add(neighbor);
                }
            }
        }
    }

    /*
     Returns a string representation of the maze with no path shown.
    */

    @Override
    public String toString() {
        return buildMazeString(null);
    }

    /*
     Prints the maze without a path.
    */

    public void printMaze() {
        System.out.print(toString());
    }

    /*
     Prints the maze with a given path highlighted.
    */

    public void printMazeWithPath(List<Cell> path) {
        System.out.print(buildMazeString(path));
    }

    /*
     Builds an ASCII representation of the maze.
     If path is not a null, cells on the path are marked with '*'.
     S = start cell (0,0), G = goal cell.
    */

    private String buildMazeString(List<Cell> path) {
        StringBuilder sb = new StringBuilder();

        Set<Cell> pathCells = new HashSet<>();
        if (path != null) {
            pathCells.addAll(path);
        }

        // Top border

        sb.append("+");
        for (int c = 0; c < cols; c++) {
            sb.append("---+");
        }
        sb.append("\n");

        for (int r = 0; r < rows; r++) {

            // Row of cells and vertical walls

            sb.append("|");
            for (int c = 0; c < cols; c++) {
                Cell cell = grid[r][c];

                boolean isStart = (r == 0 && c == 0);
                boolean isGoal = (r == rows - 1 && c == cols - 1);
                boolean onPath = pathCells.contains(cell);

                if (isStart) {
                    sb.append(" S ");
                } else if (isGoal) {
                    sb.append(" G ");
                } else if (onPath) {
                    sb.append(" * ");
                } else {
                    sb.append("   ");
                }

                // Right wall (or opening) between this cell and the cell to the right

                if (c < cols - 1) {
                    Cell east = grid[r][c + 1];
                    if (areConnected(cell, east)) {

                        // No wall

                        sb.append(" ");   
                    } else {

                        // Wall

                        sb.append("|");
                    }
                } else {
                    
                    // Far right border

                    sb.append("|"); 
                }
            }
            sb.append("\n");

            // Row of horizontal walls

            sb.append("+");
            for (int c = 0; c < cols; c++) {
                Cell cell = grid[r][c];
                if (r < rows - 1) {
                    Cell south = grid[r + 1][c];
                    if (areConnected(cell, south)) {

                        // No horizontal wall

                        sb.append("   ");
                    } else {

                        // Horizontal wall

                        sb.append("---");
                    }
                } else {

                    // Bottom border

                    sb.append("---");
                }
                sb.append("+");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}

/*
 MazeSolver:
 Uses DFS to find a path from start to goal.
 Returns the path as a List<Cell>.
*/

class MazeSolver {
    private final Maze maze;

    public MazeSolver(Maze maze) {
        this.maze = maze;
    }

    public List<Cell> solve(Cell start, Cell goal) {
        Set<Cell> visited = new HashSet<>();
        Map<Cell, Cell> parent = new HashMap<>();

        boolean found = dfs(start, goal, visited, parent);

        if (!found) {
            return Collections.emptyList();
        }

        // Reconstruct the path from goal back to start using the parent map

        List<Cell> path = new ArrayList<>();
        Cell current = goal;
        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }

        // Path is currently goal -> ... -> start, so reverse it

        Collections.reverse(path);
        return path;
    }

    /*
     Recursive DFS helper.
    */

    private boolean dfs(Cell current, Cell goal,
                        Set<Cell> visited,
                        Map<Cell, Cell> parent) {

        visited.add(current);

        if (current.equals(goal)) {
            return true;
        }

        for (Cell neighbor : maze.getNeighbors(current)) {
            if (!visited.contains(neighbor)) {
                parent.put(neighbor, current);
                if (dfs(neighbor, goal, visited, parent)) {
                    return true;
                }
            }
        }

        return false;
    }
}

/*
 Visualization helper class.
 Delegates to Maze's ASCII printing methods, but having a separate class matches the suggested design.
*/

class Visualization {

    public static void showMaze(Maze maze) {
        maze.printMaze();
    }

    public static void showMazeWithPath(Maze maze, List<Cell> path) {
        maze.printMazeWithPath(path);
    }
}

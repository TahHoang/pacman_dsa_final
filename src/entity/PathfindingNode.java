package entity;

/**
 * Represents a node in the BFS pathfinding algorithm.
 * Each node stores its position and a reference to its parent for backtracking.
 */
public class PathfindingNode {
    public int x;
    public int y;
    public PathfindingNode parent;

    public PathfindingNode(int x, int y, PathfindingNode parent) {
        this.x = x;
        this.y = y;
        this.parent = parent;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PathfindingNode node = (PathfindingNode) obj;
        return x == node.x && y == node.y;
    }

    @Override
    public int hashCode() {
        return x * 1000 + y;
    }
}

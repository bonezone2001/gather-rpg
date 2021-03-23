// My implementation of the A star algorithm

import java.util.Collections; 

class Node {
  boolean collidable = false;
  boolean visited = false;
  float globalGoal;
  float localGoal;
  float x;
  float y;
  ArrayList<Node> neighbours;
  Node parent;

  // WHYYYYY come onnnn, C++ I'd just copy the memory or pass as value...
  // Create deep copy
  @Override
  Node clone() {
    Node copy = new Node();
    copy.collidable = collidable;
    copy.visited = visited;
    copy.globalGoal = globalGoal;
    copy.localGoal = localGoal;
    copy.x = x;
    copy.y = y;
    copy.neighbours = neighbours;
    copy.parent = parent;
    return copy;
  }
}

// Created this so we can reuse the path finding instance
// but with different elements passed in
class NodeInfo {
  Node nodes[][];     // All the nodes to path find to
  Node startNode;     // Start
  Node endNode;       // Destination
}

class PathFinder {
  LevelManager lvls;  // Reference to level manager

  // Sort for nodes
  Comparator sortToTest = new Comparator<Node>() {
    @Override
    public int compare(Node n1, Node n2) {
      // Flip conditions for fun
      if (n1.globalGoal < n2.globalGoal) return -1;
      if (n1.globalGoal > n2.globalGoal) return 1;
      return 0;
    }
  };

  // Left over from debugging, could remove, too lazy
  PathFinder(LevelManager lvls_) {
    lvls = lvls_;
  }

  // Allow for init nodes outside class
  Node[][] initNodes() {
    Level current = lvls.current;
    Node[][] nodes_ = new Node[(int)current.size.y][(int)current.size.x];
    PVector start = new PVector(-(current.totalSize.x / 2) + (current.tileSize / 2), -(current.totalSize.y / 2) + (current.tileSize / 2));
    for (int y = 0; y < nodes_.length; y++)
      for (int x = 0; x < nodes_[0].length; x++) {
        Node node = new Node();
        node.x = start.x + current.tileSize * x;
        node.y = start.y + current.tileSize * y;
        node.neighbours = new ArrayList<Node>();
        node.collidable = false;
        node.globalGoal = Float.POSITIVE_INFINITY;
        node.localGoal = Float.POSITIVE_INFINITY;
        node.parent = null;
        node.visited = false;
        nodes_[y][x] = node;
      }

    // After init, connect neighbours
    for (int y = 0; y < nodes_.length; y++)
      for (int x = 0; x < nodes_[0].length; x++) {
        Node node = nodes_[y][x];
        
        // Connect y-axis
        if (y > 0) node.neighbours.add(nodes_[y-1][x]);
        if (y < nodes_.length-1) node.neighbours.add(nodes_[y+1][x]);

        // Connect x-axis
        if (x > 0) node.neighbours.add(nodes_[y][x-1]);
        if (x < nodes_[0].length-1) node.neighbours.add(nodes_[y][x+1]);
      }

    // Get current collidables as of creation
    updateCollidables(nodes_);

    return nodes_;
  }

  // Update all the collidable objects in the scene
  void updateCollidables(Node[][] nodes) {
    for (int y = 0; y < nodes.length; y++)
      for (int x = 0; x < nodes[0].length; x++) {
        int tileGridVal = lvls.current.tileGrid[y][x];
        int objGridVal = lvls.current.objectGrid[y][x];
        if (tileGridVal >= 0 && tileGridVal < lvls.current.tiles.length)
          nodes[y][x].collidable = lvls.current.tiles[tileGridVal].hasCollision;
        else nodes[y][x].collidable = true;

        if (objGridVal >= 0 && objGridVal < lvls.current.tiles.length)
          nodes[y][x].collidable = lvls.current.tiles[objGridVal].hasCollision;
      }
  }

  // Make my life a little easier instead of typing it out manually
  float nodeDistance(Node a, Node b) {
    return game.utils.distanceTo(a.x, a.y, b.x, b.y);
  }

  // Solve without trying all neighbours, fastest in terms of performance,
  // might not be the fastest path
  void solve(NodeInfo nodeInfo) {
    solve(nodeInfo, false);
  }

  // Solve path between points using A-Star
  // Probably implemented incorrect but whatevs lawl it works
  void solve(NodeInfo nodeInfo, boolean inDepth) {
    for (int y = 0; y < nodeInfo.nodes.length; y++)
      for (int x = 0; x < nodeInfo.nodes[0].length; x++) {
        Node node = nodeInfo.nodes[y][x];
        node.visited = false;
        node.parent = null;
        node.globalGoal = Float.POSITIVE_INFINITY;
        node.localGoal = Float.POSITIVE_INFINITY;
      }

    // Check we even have nodes
    if (nodeInfo.startNode == null) return;
    if (nodeInfo.endNode == null) return;

    Node current = nodeInfo.startNode;
    nodeInfo.startNode.localGoal = 0;
    nodeInfo.startNode.globalGoal = nodeDistance(nodeInfo.startNode, nodeInfo.endNode);

    // Store all the nodes we want to test
    ArrayList<Node> toTest = new ArrayList<Node>();
    toTest.add(nodeInfo.startNode);

    // Algorithm timeeee
    while (!toTest.isEmpty() && (!inDepth ? current != nodeInfo.endNode : true)) {
      // Sort the collection by global goals of the nodes
      toTest.sort(sortToTest);

      // Remove nodes that have already been visited
      while (!toTest.isEmpty() && toTest.get(0).visited)
        toTest.remove(0);

      // Double check we have things in list
      if (toTest.isEmpty()) break;

      // Set current to the front change visted status
      current = toTest.get(0);
      current.visited = true;

      // Go through neighbours
      for (Node neighbour : current.neighbours) {
        // Add none collidable, not visited elements to list
        if (!neighbour.visited && !neighbour.collidable)
          toTest.add(neighbour);
        
        // Neighbours lowest parent distance
        float maybeLowerGoal = current.localGoal + nodeDistance(current, neighbour);

        // If the goal is lower than the neighbour's local goal,
        // update their goal and set the parent as the current node
        if (maybeLowerGoal < neighbour.localGoal) {
          neighbour.parent = current;
          neighbour.localGoal = maybeLowerGoal;

          // Oh yeah, and update the global goal (since local goal has changed)
          neighbour.globalGoal = neighbour.localGoal + nodeDistance(neighbour, nodeInfo.endNode);
        }
      }
    }
  }

  // Get the path from the solved node information
  ArrayList<Node> getPath(NodeInfo nodeInfo) {
    if (nodeInfo.endNode != null) {
      ArrayList<Node> path = new ArrayList<Node>();      
      
      Node node = nodeInfo.endNode;
      while (node != null) {
        // Add current node to path (unless it's the start)
        if (node.parent != null)
          path.add(node);

        // Change nodes to the parent of this
        node = node.parent;
      }

      // Return the path reversed (since we want the start to end, not end to start)
      Collections.reverse(path);
      return path;
    }
    return null;
  }

  // Draw the algorithm for debugging purposes
  void draw(NodeInfo nodeInfo) {
    // Draw visited nodes using alpha modified col
    fill(0,0,0, 100);
    noStroke();
    for (int y = 0; y < nodeInfo.nodes.length; y++)
      for (int x = 0; x < nodeInfo.nodes[0].length; x++) {
        Node node = nodeInfo.nodes[y][x];
        if (!node.visited) continue;
        rect(node.x-(lvls.current.tileSize/2), node.y-(lvls.current.tileSize/2), lvls.current.tileSize, lvls.current.tileSize);
      }

    // Draw Nodes
    stroke(255);
    for (int y = 0; y < nodeInfo.nodes.length; y++)
      for (int x = 0; x < nodeInfo.nodes[0].length; x++) {
        Node node = nodeInfo.nodes[y][x];
        
        // Draw start/end node boxes
        if (node == nodeInfo.startNode) {
          fill(0, 255, 0);
          rect(node.x-15, node.y-15, 30, 30);
        }
        if (node == nodeInfo.endNode) {
          fill(0, 255, 255);
          rect(node.x-15, node.y-15, 30, 30);
        }

        // Draw node
        if (node.collidable)
          fill(0, 255, 0);
      }

    // Draw path
    stroke(255, 255, 0);
    if (nodeInfo.endNode != null) {
      Node node = nodeInfo.endNode;
      while (node != null) {
        // Draw line between here and the parent
        if (node.parent != null)
          line(node.x, node.y, node.parent.x, node.parent.y);

        // Change nodes to the parent of this
        node = node.parent;
      }
    }
    stroke(0);
    fill(255);
  }
}
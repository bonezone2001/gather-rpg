// An enemy that incorporates A* path finding

class Enemy extends BaseEntity {
  // Can be changed, otherwise they'd be constants
  float speedD = 150;
  float health = 100;
  float damage = 10;

  boolean isDead = false;

  // Path finding
  private NodeInfo nodeInfo;
  private ArrayList<Node> path;
  private PVector prevGridPos;
  private int pathIdx;

  // Spawn player at center of world, looking down by default
  Enemy() {
    super(null, "enemies/crab", true);
    super.setAnim("idle");
    super.setAnimFps(6);
    super.setScale(1);

    path = null;
    nodeInfo = new NodeInfo();
    nodeInfo.nodes = game.gameplay.pathfinder.initNodes();
    prevGridPos = new PVector(0, 0);

    // Add all nodes that do not have collision to an arraylist
    ArrayList<Node> canSpawn = new ArrayList<Node>();
    for (int y = 0; y < nodeInfo.nodes.length; y++)
      for (int x = 0; x < nodeInfo.nodes[0].length; x++) {
        if (!nodeInfo.nodes[y][x].collidable)
          canSpawn.add(nodeInfo.nodes[y][x]);
      }

    // Get random element from arraylist and spawn enemy at that position
    if (canSpawn.size() > 0) {
      Node spawnAt = canSpawn.get((int)random(canSpawn.size()));
      pos.set(spawnAt.x, spawnAt.y);
    }
    // Couldn't spawn, output error
    else
      println("Couldn't spawn enemy! No none collidable spaces");
  }
  
  // I know if you don't switch direction, it won't reset timeCount
  // if you hold opposing directions it'll also animate in place
  void charMoved(String dir) {
    if (moved) return;
    moved = true;
    super.setAnim(dir);
  }

  void drop() {
    // Just drop all in current position
    for (int i = (int)random(1, 5); i >= 0; i--) {
      Collectable coll = new Collectable();
      coll.pos.set(pos);
      game.gameplay.requiredCollectables.add(coll);
    }
  }
  
  // Handle movement animation
  void movement() {
    // Prevent moving along a path that doesn't exist
    if (path == null || pathIdx >= path.size() || path.size() == 0) return;

    // Current node
    Node target = path.get(pathIdx);

    // Get direction as vector
    PVector dir = new PVector(target.x - pos.x, target.y - pos.y);
    dir.normalize();

    // Set velocity
    vel.set(dir.x * speedD * (float)game.deltaTime, dir.y * speedD * (float)game.deltaTime);

    // Change animation direction based on priority
    if (vel.x != 0 || vel.y != 0)
      charMoved("move");
    // if (abs(vel.y) >= abs(vel.x)) {
    //   if (vel.y < 0)
    //     charMoved("up");
    //   if (vel.y > 0)
    //     charMoved("down");
    // }
    // else {
    //   if (vel.x < 0) 
    //     charMoved("left");
    //   if (vel.x > 0)
    //     charMoved("right");
    // }
  }

  void damage(float amount) {
    if (this.isDead) return;
    this.health -= amount;
    if (this.health <= 0)
      this.isDead = true;
  }

  void draw() {
    // Update path finder nodes when level changes
    if (game.gameplay.lvls.levelHasChanged)
      game.gameplay.pathfinder.updateCollidables(nodeInfo.nodes);

    // Get current position in node/world grid
    int gridX = floor((pos.x + (game.gameplay.lvls.current.totalSize.x / 2)) / game.gameplay.lvls.current.tileSize);
    int gridY = floor((pos.y + (game.gameplay.lvls.current.totalSize.y / 2)) / game.gameplay.lvls.current.tileSize);

    if (game.gameplay.player.moved) {
      // Reset the path index
      pathIdx = 0;

      // Get target position in node/world grid
      int playerGridX = floor((game.gameplay.player.pos.x + (game.gameplay.lvls.current.totalSize.x / 2)) / game.gameplay.lvls.current.tileSize);
      int playerGridY = floor((game.gameplay.player.pos.y + (game.gameplay.lvls.current.totalSize.y / 2)) / game.gameplay.lvls.current.tileSize);

      // Set the start and end nodes
      nodeInfo.startNode = nodeInfo.nodes[gridY][gridX];
      nodeInfo.endNode = nodeInfo.nodes[playerGridY][playerGridX];

      // Solve and get path
      game.gameplay.pathfinder.solve(nodeInfo);
      path = game.gameplay.pathfinder.getPath(nodeInfo);
    }

    // Check if differing grid from last frame, if so, update path idx
    // I mean this is done inside the player has moved branch so :( not 
    // really as responsive as I would want but it's more performant
    if ((prevGridPos.x != gridX || prevGridPos.y != gridY) && path != null && (pathIdx < path.size() || path.size() != 0)) {
      Node target = path.get(pathIdx);
      if (game.utils.distanceTo(pos.x, pos.y, target.x, target.y) < (2 / game.deltaTime)) {
        pathIdx++;
        prevGridPos.set(gridX, gridY);
      }
    }

    // Walk along path until close to player
    if (game.utils.distanceTo(pos.x, pos.y, game.gameplay.player.pos.x, game.gameplay.player.pos.y) > 75)
      movement();
    // Attack the player
    else {
      // Change attack direction based upon y-axis position of me (enemy) and player
      if (game.gameplay.player.pos.y > pos.y) 
        super.setAnim("attackDown");
      else
        super.setAnim("attackUp");
      
      // Deal damage
      if (this.animFrame == 1 && game.gameplay.player.health > 0)
        game.gameplay.player.damage(this.damage);
    }

    // Draw path in debug mode
    if (game.DEBUG && nodeInfo != null)
      game.gameplay.pathfinder.draw(nodeInfo);

    // Animate
    super.animate();

    // Update
    pos.add(vel);
    vel.set(0, 0);
    moved = false;
  }
}
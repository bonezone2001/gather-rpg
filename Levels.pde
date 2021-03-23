// Had to seperate out some things in order to make serializable
abstract class Level extends LevelRaw {
  PVector totalSize;
  Tile[] tiles;
  int tileSize = 50;
  PVector size;

  Level(String path) {
    Sprite[] sprites = game.utils.loadImages(path, true);
    tiles = new Tile[sprites.length];
    
    // Loading of tiles
    // The file name actually determines if the tile is collidable or not (the integer at the end of the file name)
    for (int i = 0; i < sprites.length; i++) {
      tiles[i] = new Tile(sprites[i]);
      if (Integer.parseInt(tiles[i].name.substring(tiles[i].name.length() - 1)) == 1) {
        tiles[i].hasCollision = true;
      }
      tiles[i].img.resize(tileSize, tileSize);
    }

    // Init
    size = new PVector(27, 19);
    tileGrid = new int[0][0];
    objectGrid = new int[0][0];
    totalSize = new PVector(0, 0);
  }

  Level(String path, LevelRaw lvl) {
    this(path);
    type = lvl.type;
    tileGrid = lvl.tileGrid;
    objectGrid = lvl.objectGrid;
    size = new PVector(tileGrid[0].length, tileGrid.length);
  }

  // Initialization of the size and spawn collectables
  void init() {
    totalSize = new PVector(
      tileSize * tileGrid[0].length,
      tileSize * tileGrid.length
    );
  }

  void spawnCollectable(ArrayList<PVector> canSpawnCoords, int type, int number) {
    for (int i = 0; i < number; i++) {
      if (canSpawnCoords.size() == 0) return;
      int idx = (int)random(0, canSpawnCoords.size()-1);
      Collectable coll = new Collectable();
      if (type == 1) coll = new DamagePickup();
      if (type == 2) coll = new HealthPickup();
      coll.pos.set(canSpawnCoords.get(idx));
      if (type == 0)
        game.gameplay.requiredCollectables.add(coll);
      else
        game.gameplay.collectables.add(coll);
      canSpawnCoords.remove(idx);
    }
  }

  ArrayList<PVector> getAvailableSpawns() {
    ArrayList<PVector> canSpawnCoords = new ArrayList<PVector>();
    for (int i = 0; i < tileGrid.length; i++) 
      for (int j = 0; j < tileGrid[0].length; j++)
      {
        // If tile grid is none existant, might as well just restart
        if (tileGrid[i][j] < 0 || tileGrid[i][j] >= tiles.length) continue;

        // If both the object and tile grid are none collidable, add to canSpawn
        if (!tiles[tileGrid[i][j]].hasCollision) {
          if (objectGrid[i][j] >= 0 && objectGrid[i][j] < tiles.length-1)
            if (tiles[objectGrid[i][j]].hasCollision) continue;
          canSpawnCoords.add(new PVector((-(totalSize.x / 2) + (tileSize / 2)) + (j * tileSize), (-(totalSize.y / 2) + (tileSize / 2)) + (i * tileSize)));
        }
      }
    return canSpawnCoords;
  }
  
  void spawnItems() {
    // Get positions at which objects can be placed!
    ArrayList<PVector> canSpawnCoords = getAvailableSpawns();
    
    // Spawn 3 collectables as per the specification
    spawnCollectable(canSpawnCoords, 0, 3);
    spawnCollectable(canSpawnCoords, 1, (int)random(0, 3));
    spawnCollectable(canSpawnCoords, 2, (int)random(0, 3));
  }
  
  void spawnEnemies(int number) {
    // Spawn enemies in locations
    for (int i = 0; i < number; i++) {
      game.gameplay.enemies.add(new Enemy());
    }
  }

  void reset() {
    game.gameplay.timer.reset();
    game.gameplay.player.pos.set(0, 0);
    spawnItems();
    spawnEnemies((int)random(0, 3 + (game.gameplay.score / 1000)));
  }

  // Original state of the map if not loaded
  void defineArr(int def) {
    for (int i = 0; i < tileGrid.length; i++)
      for (int j = 0; j < tileGrid[0].length; j++)
        tileGrid[i][j] = def;
    for (int i = 0; i < objectGrid.length; i++)
      for (int j = 0; j < objectGrid[0].length; j++)
        objectGrid[i][j] = -1;
    init();
  }
}

// Class for desert levels
class DesertLevel extends Level {
  DesertLevel() {
    super("tilesets/desert");
    type = LevelType.DESERT;
    tileGrid = new int[(int)size.y][(int)size.x];
    objectGrid = new int[(int)size.y][(int)size.x];
    super.defineArr(27);
  }

  DesertLevel(LevelRaw lvl) {
    super("tilesets/desert", lvl);
    super.init();
  }
}

// Class for grass levels
class GrassLevel extends Level {
  GrassLevel() {
    super("tilesets/grass");
    type = LevelType.GRASS;
    tileGrid = new int[(int)size.y][(int)size.x];
    objectGrid = new int[(int)size.y][(int)size.x];
    super.defineArr(1);
  }

  GrassLevel(LevelRaw lvl) {
    super("tilesets/grass", lvl);
    super.init();
  }
}

// Class for dungeon levels
class DungeonLevel extends Level {
  DungeonLevel() {
    super("tilesets/dungeon");
    type = LevelType.DUNGEON;
    tileGrid = new int[(int)size.y][(int)size.x];
    objectGrid = new int[(int)size.y][(int)size.x];
    super.defineArr(4);
  }

  DungeonLevel(LevelRaw lvl) {
    super("tilesets/dungeon", lvl);
    super.init();
  }
}
// All the current levels in the game as well as the base level class and manager
// Could very well use a tile sheet instead of multiple images using img.get(x, y, w, h)
// Probably gonna make a layering system eventually int[][][] using +/- in map editor

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Arrays;

class LevelManager {
  Level[] levels;
  Level current;
  boolean placingObjects = false;
  boolean levelHasChanged = false;
  ArrayList<Rect> collidables;

  LevelManager() {
    collidables = new ArrayList<Rect>();
    loadLevels();
  }

  // Load all the levels
  void loadLevels() {
    String[] lvlNames = game.utils.filesInFolder("levels");
    levels = new Level[lvlNames.length];
    for (int i = 0; i < levels.length; i++) {
      LevelRaw lvlRaw = (LevelRaw)game.utils.loadObject("levels/" + lvlNames[i]);
      switch (lvlRaw.type) {
        case DESERT:
          levels[i] = new DesertLevel(lvlRaw);
          break;
        case GRASS:
          levels[i] = new GrassLevel(lvlRaw);
          break;
        case DUNGEON:
          levels[i] = new DungeonLevel(lvlRaw);
          break;
      }
    }
    if (levels.length == 0)
      levels = new Level[] { new DesertLevel() };
    current = levels[(int)random(0, levels.length-1)];
  }

  void randomLevel() {
    game.gameplay.requiredCollectables.clear();
    game.gameplay.collectables.clear();
    game.gameplay.enemies.clear();
    current = levels[(int)random(0, levels.length)];
    current.reset();
  }

  // What a complete mess lol
  void drawGrid(int[][] grid, Tile[] tiles) {
    float x = -(current.totalSize.x / 2), y = -(current.totalSize.y / 2);
    for (int i = 0; i < grid.length; i++) {
      x = -(current.totalSize.x / 2);
      for (int j = 0; j < grid[0].length; j++) {
        int tileIdx = grid[i][j];

        // Don't render tiles that don't exist or are off screen
        // With this small optimisation I can have both grids at 1500x1500 (4.5 million total tiles)
        // and still maintain 45 fps on my machine
        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
        if (tileIdx >= 0 && tileIdx < tiles.length && game.utils.onScreen(x, y, current.tileSize, current.tileSize)) {
          Tile curTile = tiles[tileIdx];
          if (curTile.hasCollision)
            collidables.add(new Rect(new PVector(x, y), new PVector(current.tileSize, current.tileSize)));
          image(curTile.img, x, y);
        }
        // Add collision to blank tiles in tileGrid
        else if (Arrays.equals(current.tileGrid, grid) && (tileIdx < 0 || tileIdx >= tiles.length) && game.utils.onScreen(x, y, current.tileSize, current.tileSize))
          collidables.add(new Rect(new PVector(x, y), new PVector(current.tileSize, current.tileSize)));
        x += current.tileSize;
      }
      y += current.tileSize;
    }

    // Add collision around bounds
    if (Arrays.equals(current.tileGrid, grid)) {
      float mattersX = (current.totalSize.x / 2);
      float mattersY = (current.totalSize.y / 2);

      collidables.add(new Rect(-mattersX-10, -mattersY-10, current.totalSize.x+20, 10));        // TOP
      collidables.add(new Rect(-mattersX-10, -mattersY, 10, current.totalSize.y+10));           // LEFT
      collidables.add(new Rect(-mattersX, mattersY, current.totalSize.x+10, 10));               // BOTTOM
      collidables.add(new Rect(mattersX, -mattersY, 10, current.totalSize.y-20));               // RIGHT
    }
  }

  void draw() {
    levelHasChanged = false;
    if (collidables.size() > 0) {
      collidables.clear();
    }
    
    // Enable map editor if in debug
    if (game.DEBUG)
      if (!placingObjects)
        mapEditor(current.tileGrid, current.tiles);
      else
        mapEditor(current.objectGrid, current.tiles);

    // Render tiles in grid
    drawGrid(current.tileGrid, current.tiles);
    drawGrid(current.objectGrid, current.tiles);
  }

  // Map editor made to make my life easier while creating the levels
  void mapEditor(int[][] grid, Tile[] tiles) {
    // Get grid indexes
    int gridX = floor((game.gameplay.worldMouse.x + (current.totalSize.x / 2)) / current.tileSize);
    int gridY = floor((game.gameplay.worldMouse.y + (current.totalSize.y / 2)) / current.tileSize);
    
    // Check within bounds
    if (gridX >= grid[0].length || gridY >= grid.length || gridX < 0 || gridY < 0) return;
    
    // Check mouse and update grid
    int value = grid[gridY][gridX];
    if (game.input.getMouse(LEFT).framePressed) {
      grid[gridY][gridX] = (value+1) % (tiles.length+1);
      levelHasChanged = true;
    } else if (game.input.getMouse(RIGHT).framePressed) {
      grid[gridY][gridX]--;
      if (grid[gridY][gridX] < -1)
        grid[gridY][gridX] = tiles.length-1;
      levelHasChanged = true;
    }
    
    // Change between placing objects and changing tiles
    if (game.input.getMouse(CENTER).framePressed)
      placingObjects = !placingObjects;

    // New level
    if (game.input.getKey('n').framePressed) {
      current = new DesertLevel();
      levelHasChanged = true;
    }
    
    // New level (biome)
    if (game.input.getKey('b').framePressed) {
      if (current.type == LevelType.DUNGEON)
        current = new DesertLevel();
      else if(current.type == LevelType.DESERT)
        current = new GrassLevel();
      else
        current = new DungeonLevel();
    }

    // Save level
    if (game.input.getKey(ENTER).framePressed) {
      LevelRaw raw = new LevelRaw(current.type, current.tileGrid, current.objectGrid);
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMmmss");
      game.utils.saveObject(raw, "levels/" + current.type.name() + "-" + dtf.format(LocalDateTime.now()) + ".level");
    }
  }
}
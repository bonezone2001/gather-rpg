// For some odd reason if I don't seperate this out into it's own JAVA
// file then it errors out saying that main (the project name) cannot
// be serialized even though it is never referred to

// NOTE: I realise I could have just made a 3D array and had many different layers instead of just the object and tile layers.
// However, I've already serialized a few levels and making a converter for such a small change seems redundant for this
// game. An example of what I mean int[][][] grid (meaning an array (layer) of arrays (columns) of an array (rows))

// Level types
enum LevelType {
  DESERT,
  GRASS,
  DUNGEON
}

// Serializable object, the stuff that actually matters to store it
class LevelRaw implements java.io.Serializable {
  LevelType type;
  int[][] tileGrid;
  int[][] objectGrid;

  LevelRaw() {
    type = LevelType.DESERT;
    tileGrid = new int[0][0];
    objectGrid = new int[0][0];
  }

  LevelRaw(LevelType type_, int[][] tileGrid_, int[][] objectGrid_) {
    type = type_;
    tileGrid = tileGrid_;
    objectGrid = objectGrid_;
  }
}

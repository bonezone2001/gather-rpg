// Container for sprite images since I need the file name too (possibly other things later on)
class Sprite {
  String name;  // File name
  PVector size; // Original size
  PImage img;   // Image

  Sprite(String path) {
    int extPos = path.lastIndexOf(".");
    int slashPos = path.lastIndexOf("/");
    name = path.substring(slashPos > 0 ? slashPos+1 : 0, extPos);
    img = loadImage(path);
    size = new PVector(img.width, img.height);
  }

  Sprite(String name_, PImage img_) {
    name = name_;
    img = img_;
    size = new PVector(img.width, img.height);
  }

  Sprite(Sprite replace) {
    name = replace.name;
    size = replace.size;
    img = replace.img;
  }
}

// Extended sprite that also handles VERY collision
class Tile extends Sprite {
  boolean hasCollision;

  Tile(String path) {
    super(path);
  }

  Tile(Sprite convert) {
    super(convert);       
  }
}
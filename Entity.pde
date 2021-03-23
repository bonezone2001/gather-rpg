import java.io.FilenameFilter;
import javafx.util.Pair;
import java.util.Comparator;

// Class for all entities in the game world
// holds sprite/animation, velocity and position
// animation still needs work (especially animation speed to make it smoother with slower computers)
abstract class BaseEntity {
  boolean moved;          // Has entity moved
  PVector pos;            // Position
  PVector size;           // Current size
  PVector scale;          // Scale
  PVector vel;            // Velocity
  
  // Sprites/animation
  // TODO: implement ping pong anims
  boolean animOnce = false;   // Animation is run once?
  boolean reverse = false;    // Animation is run in reverse (could just reverse animCur but oh well)
  String setAnim = "";        // Hold the name of the current animation to prevent setting it again
  Sprite[] sprites;           // Sprites / Images
  Sprite[] animCur;           // Current list of sprites for current animation - they share memory
  float animSpeed = 0;        // Speed of animation (in seconds)
  float timeCount = 0;        // Count up each animate() call for animation speed (in seconds using deltaTime)
  int animFrame = 0;          // Current frame in animation
  
  // Collision order
  ArrayList<Pair<Integer, Float>> collidOrder;


  // Constructors and initialization
  // --------------------------------
  // Had to move this out of the constructor because I couldn't call the other constructor half way through for some reason
  private void initialize(PVector pos_, Sprite[] sprites_) {
    // Null check for variable input (simply pass null)
    if (pos_ == null)
      pos_ = new PVector(0, 0);
    if (sprites_ == null)
      sprites_ = new Sprite[0];

    pos = pos_;
    vel = new PVector(0, 0);
    sprites = sprites_;
    size = new PVector(100, 100);
    scale = new PVector(1, 1);
    animCur = sprites.clone();  // Shallow copy (just references)
  }

  BaseEntity() {
    pos = new PVector(0, 0);
    vel = new PVector(0, 0);
    size = new PVector(100, 100);
    scale = new PVector(1, 1);
    sprites = new Sprite[0];
    animCur = sprites.clone();
  }

  // Folder from assets passed for sprites (load them)
  BaseEntity(PVector pos_, String sprites_, boolean isAnimation) {
    // Null check for variable input (simply pass null)
    if (sprites_ == null || sprites_ == "")
      initialize(pos_, null);
    if (pos_ == null)
      pos_ = new PVector(0, 0);
    
    // Load images
    pos = pos_;
    vel = new PVector(0, 0);
    size = new PVector(100, 100);
    scale = new PVector(1, 1);
    if (sprites_ != null)
      loadImages(sprites_, isAnimation);
  }

  // Sprites and typical passed (can be null becos too lazy to make multiple overloads)
  BaseEntity(PVector pos_, Sprite[] sprites_) {
    initialize(pos_, sprites_);
  }

  // Methods
  // --------------------------------
  // Load images into both arrays
  public void loadImages(String fileOrFolder, boolean isFolder) {
    sprites = game.utils.loadImages(fileOrFolder, isFolder);
    animCur = sprites.clone();
  }

  // Changes velocity based upon collision
  CollisionResult resolveCollision(boolean changeVel) {
    CollisionResult res = null;

    // Check against all collidables on screen
    // Maybe if they're going faster than the screen it'll pose an issue but whatever
    
    // Sort collidables based on distance (since order matters with collision)
    if (collidOrder == null)
      collidOrder = new ArrayList<Pair<Integer, Float>>();
    else
      collidOrder.clear();

    // Add elements and their distances
    for (int i = game.gameplay.lvls.collidables.size()-1; i >= 0; i--) {
      res = game.utils.RayCollisionRect(pos, size, vel, game.gameplay.lvls.collidables.get(i));
      if (res == null) continue;
      collidOrder.add(new Pair<Integer, Float>(i, res.timeHit));
    }

    // I miss lambda expressions... WHERE IS JAVA 8?!
    // Sort array by right hand element
    Comparator compare = new Comparator<Pair<Integer, Float>>() {
      @Override
      public int compare(Pair<Integer, Float> p1, Pair<Integer, Float> p2) {
        return p1.getValue().compareTo(p2.getValue());
      }
    };
    collidOrder.sort(compare);

    for (Pair<Integer, Float> ordered : collidOrder) {
      if (changeVel)
        res = game.utils.RayCollisionRectRes(pos, size, vel, game.gameplay.lvls.collidables.get(ordered.getKey()));
      else
        res = game.utils.RayCollisionRect(pos, size, vel, game.gameplay.lvls.collidables.get(ordered.getKey()));
      if (res != null) {
        // Debug display
        if (game.DEBUG) {
          ellipse(res.pointOfContact.x, res.pointOfContact.y, 25, 25);
          line(res.pointOfContact.x, res.pointOfContact.y, res.pointOfContact.x+(res.contactNormal.x*15), res.pointOfContact.y+(res.contactNormal.y*15));
          fill(0); text(res.timeHit, res.pointOfContact.x, res.pointOfContact.y); fill(255);
        }
      }
    }
    return res;
  }

  // Handle collision (resolve collision)
  // I return the collision result so we can inspect the block we collided with just by nudging backwards along the normal
  // and checking the block index
  CollisionResult handleCollision() {
    return resolveCollision(true);
  }

  // Check collision with the current world
  boolean checkCollision() {
    return resolveCollision(false) != null;
  }

  // Draw the specified frame index from animCur
  void drawImage(int idx) {
    if (animCur.length == 0) return;
    if (idx >= animCur.length) throw new Error(String.format("Image outside bounds! %i %i", idx, animCur.length));
    PImage img = animCur[idx].img;
    size.set(img.width, img.height);
    image(img, pos.x-(img.width/2), pos.y-(img.width/2));
  }

  // Advance the animation and draw (depending upon the animation speed)
  void animate() {
    if (animCur.length == 0) return;
      
    // If frame counter exceeds total and set to only run once, stop
    if (animOnce && animFrame >= animCur.length)
      return;

    // Frame independant timing system, so all computers end up on the same
    // frame regardless of frame rate
    timeCount += game.deltaTime;
    if (timeCount > animSpeed) {
      // Division (to handle much slower systems with many missed frames)
      int frames = floor(timeCount / animSpeed);
      timeCount -= animSpeed * frames;
      animFrame = (animFrame + (reverse ? -frames : frames));
      
      // If frame counter exceeds total and set to only run once, stop
      if (animOnce && animFrame >= animCur.length)
        return;
      
      // Modulus to avoid out of bounds and looping
      if (reverse && animFrame < 0)
        animFrame = animCur.length - (abs(animFrame) % animCur.length);
      else
        animFrame = animFrame % animCur.length;
    }

    // Draw the image and update size if changed
    if (animFrame >= 0 && animFrame < animCur.length)
      drawImage(animFrame);
  }

  // Attach to another entity, just pos for now
  void setParent(BaseEntity parent) {
    pos = parent.pos;
  }
  
  // Resize uniformly
  void setScale(float size) {
    setScale(size, size);
  }

  // Reize all sprites along x and y independantly
  void setScale(float x, float y) {
    scale.x = x;
    scale.y = y;
    for (Sprite spr : sprites)
      spr.img.resize(floor(spr.size.x*x), floor(spr.size.y*y));
  }

  PVector getSize()
  {
    Sprite frame = this.animCur[animFrame];
    return new PVector(frame.img.width, frame.img.height); 
  }

  // Shallow copy some references over to current anim list
  void setAnim(String anim) {
    if (setAnim == anim) return;
    timeCount = 0;
    animFrame = 0;
    setAnim = anim;
    ArrayList<Sprite> newAnims = new ArrayList<Sprite>();
    for (Sprite spr : sprites)
      if (spr.name.startsWith(anim))
        newAnims.add(spr);
    animCur = newAnims.toArray(new Sprite[newAnims.size()]);
  }

  // MS in between frame switches
  void setAnimSpeed(int speedInMs) {
    setAnimSpeed(speedInMs, true);
  }

  // Seconds or MS in between frames
  void setAnimSpeed(float speedInMs, boolean isMS) {
    if (isMS)
      speedInMs /= 1000;
    animSpeed = speedInMs;
  }

  // Specify fps instead of speed
  void setAnimFps(int fps) {
    animSpeed = 1.0f / fps;
  }

  // Should reverse animation
  void setAnimReverse(boolean rev) {
    reverse = rev;
  }

  // Set the current positon (frame) we are at in animation
  void setAnimPos(int pos) {
    animFrame = pos;
  }

  void setAnimOnce(boolean state) {
    animOnce = state;
  }
}
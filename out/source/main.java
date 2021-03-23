import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.io.FilenameFilter; 
import javafx.util.Pair; 
import java.util.Comparator; 
import java.util.Map; 
import java.nio.charset.StandardCharsets; 
import javax.swing.KeyStroke; 
import java.time.format.DateTimeFormatter; 
import java.time.LocalDateTime; 
import java.util.Arrays; 
import java.util.Collections; 
import java.io.File; 
import javax.sound.sampled.Clip; 
import javax.sound.sampled.AudioSystem; 
import javax.sound.sampled.AudioInputStream; 
import javax.sound.sampled.FloatControl; 
import java.io.*; 
import java.util.Arrays; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class main extends PApplet {

// Main hooks, they're here becauseI like the 
// encapsulation and having thing's in classes so
// I just called the game object's methods from here

// Main game object
Game game;

public void setup()
{
  // This is here because I like playing at 144 hz, it should be 60 for you!
  frameRate(60);

  // Window title and size
  surface.setTitle("Really cool epic game");
  

  // Loading text (since loading audio takes some time)
  textSize(32);
  clear();
  textAlign(CENTER);
  text("GAME LOADING!", width/2, height/2);
  
  // Load game
  game = new Game();
  game.init();
}

public void draw()
{
  // For checking game performance
  if (game.DEBUG)
    surface.setTitle("FPS: " + (int)frameRate);

  game.deltaTime = 1 / frameRate; // Delta time (for frame rate independant tasks)
  game.draw();                    // Call the game class's draw
  game.input.resetFrame();        // Reset the pressed / released in frame
}


// Input states
// This is done to allow smoother input
public void keyPressed() { game.input.keyPress(); }
public void keyReleased() { game.input.keyRelease(); }
public void mousePressed() { game.input.mousePress(); }
public void mouseReleased() { game.input.mouseRelease(); }
public void mouseWheel(MouseEvent e) { game.input.mouseScroll(e); }
// Not my proudest class (it needs serious refactoring) but processing is also a tad limited/annoying
// should probably inherit from base entity for consistency...
// the font will not center correctly, it's not my fault :(

// Note: If this wasn't for an assignment I'd just use G4P


class Button {
  String text;
  int fontSize;
  PVector pos, size;
  PFont font;
  int textColor, baseColor, highlightColor;
  boolean wasHovered;
  boolean justHovered;

  // PVector uses float so I assume it supports sub pixel
  Button(float x, float y, float w, float h, String text_, int textSize) {
    // I don't know if it get's cached, I shouldn't assume and cache it myself but it'll do for current game
    font = createFont("Nexa-L.ttf", 32);

    wasHovered = false;
    justHovered = false;
    
    pos = new PVector(x, y);
    fontSize = textSize;
    size = new PVector(w, h);
    text = text_;
    setupColors(color(255,255,255), color(25,25,25), color(100,100,100));
  }
  
  // PVector uses float so I assume it supports sub pixel
  Button(float x, float y, float w, float h, String text, int textSize, int textCol, int baseCol, int highlightCol) {
    this(x, y, w, h, text, textSize);
    setupColors(textCol, baseCol, highlightCol);
  }

  // Allow for changing the colours later
  public void setupColors(int text, int base, int highlight) {
    textColor = text;
    baseColor = base;
    highlightColor = highlight;
  }

  public boolean isHovered() {
    return game.utils.hoveredRect(pos.x-(size.x/2), pos.y-(size.y/2), size.x, size.y);
  }

  public boolean justHovered() {
    return justHovered;
  }

  public boolean isPressed() {
    return isHovered() && game.input.getMouse(LEFT).framePressed;
  }

  // I hate the fact that all the changes to fontSize and fill, etc
  // they all effect globally...
  public void draw() {
    textFont(font);
    textSize(fontSize);
    textAlign(CENTER, CENTER);
    
    // Set frame hover state
    boolean hovered = isHovered();
    if (!wasHovered && hovered)
    {
      wasHovered = true;
      justHovered = true;
    }
    else if (wasHovered && !hovered)
      wasHovered = false;
    else
      justHovered = false;

    // Render background (change based on hover)
    strokeWeight(2);
    stroke(hovered ? baseColor : highlightColor);
    fill(hovered ? highlightColor : baseColor);
    rect(pos.x-(size.x/2), pos.y-(size.y/2), size.x, size.y);

    // Current don't allow changing of the button text colour on hover
    fill(textColor);

    // fontSize/4 to get it closer to center (processing's fault...)
    text(text, pos.x, pos.y-(fontSize/4));
  }
}
// This is it's own class so I can extend camera functionality
// at some point, right now it just follows an entity provided

// Extends from base entity because camera MAY move independant
class Camera extends BaseEntity {
  BaseEntity follow;

  Camera(BaseEntity toFollow) {
    setFollow(toFollow);
  }

  // This is seperated out for future expansion
  public void setFollow(BaseEntity toFollow){
    if (toFollow == null) return;
    follow = toFollow;
  }

  public void doTranslate() {
    translate(-PApplet.parseInt(follow.pos.x-game.center.x), -PApplet.parseInt(follow.pos.y-game.center.y));
  }

  public void undoTranslate() {
    translate(PApplet.parseInt(follow.pos.x-game.center.x), PApplet.parseInt(follow.pos.y-game.center.y));
  }
}
class Collectable extends BaseEntity {
  float value = 0;    // Value determines how much it adds to the score for retrieving
  
  Collectable() {
    super(null, "tilesets/collectables/good", true);
    super.setAnimFps(5);
    super.setScale(1.5f);
    // Randomize the value when created
    value = random(0, 50);
  }

  Collectable(String path) {
    super(null, path, true);
    super.setAnimFps(5);
    super.setScale(1.5f);
    // Randomize the value when created
    value = random(0, 50);
  }

  public void pickup() {
    game.gameplay.score += value;
    game.sound.collectGood.play();
  }
}

class DamagePickup extends Collectable {
  float damage = 0;   // Damage to do to player if picked up

  DamagePickup() {
    super("tilesets/collectables/bad");
    damage = random(0, 20);
  }

  public void pickup() {
    game.gameplay.player.damage(damage);
  }
}

class HealthPickup extends Collectable {
  float hp = 0;   // Damage to do to player if picked up

  HealthPickup() {
    super("tilesets/collectables/heal");
    hp = random(0, 50);
  }

  public void pickup() {
    game.gameplay.player.heal(hp);
  }
}
class DeathScreen {
  PImage bg;
  Button[] buttons;
  int buttonSize;

  DeathScreen() {
    bg = loadImage("mainMenu.png");
    bg.resize(width, height);

    int textSize = 65;
    buttonSize = 85;
    
    // Could be done programatically but whatever, just see if it even works
    buttons = new Button[] {
      new Button(game.center.x, game.center.y-(buttonSize/2)-20, 500, buttonSize, "Continue", textSize),
      new Button(game.center.x, game.center.y+(buttonSize/2)+20, 500, buttonSize, "Exit", textSize)
    };
  }

  // I'd love to pass lambda functions or function pointers to my Button class instead
  // but java 8 doesn't allow that..........
  public void pressedBtn(int idx) {
    strokeWeight(1);
    switch (idx) {
      case 0:
        game.sound.changeMusic(game.sound.gameplay);
        game.gameplay.revive();
        game.gameplay.state = GameplayState.PLAYING;
        break;
      case 1:
        exit();
        break;
    }
  }

  public void draw() {
    if (game.input.getKey(ESC).framePressed || game.input.getKey('p').framePressed)
      game.gameplay.state = GameplayState.PLAYING;

    if (game.gameplay.lives <= 0 && buttons[0].text != "No more lives!")
      buttons[0].text = "No more lives!";

    if (game.gameplay.timer.hasFinished() && buttons[0].text != "Ran out of time!")
      buttons[0].text = "Ran out of time!";

    // Draw background
    image(bg, 0, 0);

    // Draw and handle buttons
    int idx = 0;
    for (Button btn : buttons) {
      btn.draw();
      if (idx == 0 && (game.gameplay.lives <= 0 || game.gameplay.timer.hasFinished())) {
        idx++;
        continue;
      }
      if (btn.justHovered())
        game.sound.menuHover.play();
      if (btn.isPressed())
        pressedBtn(idx);
      idx++;
    }

    // Draw score and lives
    textSize(32);
    fill(0);
    text("Score: " + game.gameplay.score, game.center.x, game.center.y-(buttonSize/2)-130);
    text("Lives: " + game.gameplay.lives, game.center.x, game.center.y-(buttonSize/2)-100);
    fill(255);
  }
}
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
  public void charMoved(String dir) {
    if (moved) return;
    moved = true;
    super.setAnim(dir);
  }

  public void drop() {
    // Just drop all in current position
    for (int i = (int)random(1, 5); i >= 0; i--) {
      Collectable coll = new Collectable();
      coll.pos.set(pos);
      game.gameplay.requiredCollectables.add(coll);
    }
  }
  
  // Handle movement animation
  public void movement() {
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

  public void damage(float amount) {
    if (this.isDead) return;
    this.health -= amount;
    if (this.health <= 0)
      this.isDead = true;
  }

  public void draw() {
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
  public CollisionResult resolveCollision(boolean changeVel) {
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
  public CollisionResult handleCollision() {
    return resolveCollision(true);
  }

  // Check collision with the current world
  public boolean checkCollision() {
    return resolveCollision(false) != null;
  }

  // Draw the specified frame index from animCur
  public void drawImage(int idx) {
    if (animCur.length == 0) return;
    if (idx >= animCur.length) throw new Error(String.format("Image outside bounds! %i %i", idx, animCur.length));
    PImage img = animCur[idx].img;
    size.set(img.width, img.height);
    image(img, pos.x-(img.width/2), pos.y-(img.width/2));
  }

  // Advance the animation and draw (depending upon the animation speed)
  public void animate() {
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
  public void setParent(BaseEntity parent) {
    pos = parent.pos;
  }
  
  // Resize uniformly
  public void setScale(float size) {
    setScale(size, size);
  }

  // Reize all sprites along x and y independantly
  public void setScale(float x, float y) {
    scale.x = x;
    scale.y = y;
    for (Sprite spr : sprites)
      spr.img.resize(floor(spr.size.x*x), floor(spr.size.y*y));
  }

  public PVector getSize()
  {
    Sprite frame = this.animCur[animFrame];
    return new PVector(frame.img.width, frame.img.height); 
  }

  // Shallow copy some references over to current anim list
  public void setAnim(String anim) {
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
  public void setAnimSpeed(int speedInMs) {
    setAnimSpeed(speedInMs, true);
  }

  // Seconds or MS in between frames
  public void setAnimSpeed(float speedInMs, boolean isMS) {
    if (isMS)
      speedInMs /= 1000;
    animSpeed = speedInMs;
  }

  // Specify fps instead of speed
  public void setAnimFps(int fps) {
    animSpeed = 1.0f / fps;
  }

  // Should reverse animation
  public void setAnimReverse(boolean rev) {
    reverse = rev;
  }

  // Set the current positon (frame) we are at in animation
  public void setAnimPos(int pos) {
    animFrame = pos;
  }

  public void setAnimOnce(boolean state) {
    animOnce = state;
  }
}
enum GameState {
  MENU,
  OPTIONS,
  GAMEPLAY,
  SPLASH
}

class Game {
  PVector center;       // Center of screen x,y
  double deltaTime;     // Delta between last frame ending and the start of current frame
  SoundManager sound;   // Sound manager
  Input input;          // Extended input functionality
  Utils utils;          // Utility functions

  // Debug mode (allow for map editing among other things) (WAS FINAL/CONST BUT CHANGED FOR OPTIONS MENU)
  boolean DEBUG = false;
  
  // Game state var and the states themselves
  GameState state;
  Gameplay gameplay;
  MainMenu mainMenu;
  OptionsMenu optionsMenu;
  SplashScreen splashScreen;

  // Was originally in constructor but I need game obj initialized in some of the
  // other class constructors now so I have to call this after constructed
  public void init() {
    center = new PVector(width/2, height/2);
    input = new Input();
    utils = new Utils();
    state = GameState.SPLASH;
    gameplay = new Gameplay();
    gameplay.init();
    mainMenu = new MainMenu();
    optionsMenu = new OptionsMenu();
    splashScreen = new SplashScreen();
    
    sound = new SoundManager();
  }
  
  public void draw() {
    clear();
    switch (state) {
      case MENU:
        mainMenu.draw();
        break;
      case OPTIONS:
        optionsMenu.draw();
        break;
      case GAMEPLAY:
        gameplay.draw();
        break;
      case SPLASH:
        splashScreen.draw();
        break;
    }
    sound.update();
  }
}
enum GameplayState {
  PLAYING,
  DEAD,
  TIMER_OUT,
  PAUSED
}

// Class that handles all the gameplay
class Gameplay {
  PVector worldMouse;       // Mouse in world coordinates
  Player player;            // The main game player
  Camera camera;            // Basic camera for now
  LevelManager lvls;        // All game levels
  PathFinder pathfinder;    // A star path finder
  Hud hud;                  // Ingame HUD
  Timer timer;              // Timer for when the game should stop
  
  ArrayList<Enemy> enemies;             // List of all enemies in the current world
  ArrayList<Collectable> collectables;  // List of optional collectables  in the current world
  
  // List of required collectables  in the current world
  // didn't have to split these but I just felt it was easier
  ArrayList<Collectable> requiredCollectables;

  // Current state / states and score
  GameplayState state;
  PauseScreen pauseScreen;
  DeathScreen deathScreen;

  int score = 0;
  int lives = 3;

  Gameplay() {
    state = GameplayState.PLAYING;
    lvls = new LevelManager();
    pathfinder = new PathFinder(lvls);
    timer = new Timer();
  }

  // Somethings need the path finder to be intialized
  public void init() {
    player = new Player();
    camera = new Camera(player);
    pauseScreen = new PauseScreen();
    deathScreen = new DeathScreen();
    hud = new Hud();
    
    // Setup enemies and items
    enemies = new ArrayList<Enemy>();
    collectables = new ArrayList<Collectable>();
    requiredCollectables = new ArrayList<Collectable>();

    // Spawn items and enemies
    lvls.current.reset();

    // For interacting with the world using mouse
    worldMouse = game.utils.translateMouse(player.pos);
  }

  public void draw() {
    switch (state) {
      case PLAYING:
        playing();
        break;
      case TIMER_OUT:
      case DEAD:
        deathScreen.draw();
        break;
      case PAUSED:
        pauseScreen.draw();
        break;
    }
  }

  // For when the player chooses to continue and use a life
  public void revive() {
    player.health = 100;
    enemies.clear();
  }

  // Render actual gameplay
  private void playing() {
    if (game.input.getKey(ESC).framePressed || game.input.getKey('p').framePressed)
      state = GameplayState.PAUSED;

    // If we have everything in the level load a new one
    if (requiredCollectables.size() == 0 && enemies.size() == 0)
      lvls.randomLevel();
    
    pushMatrix();
    // Change where 0,0 is based upon the character position
    player.controls();
    camera.doTranslate();

    // Update world mouse (could check if player and mouse moved using mouseMoved() for performance but lazy)
    // should be moved into camera
    game.utils.translateMouse(player.pos, worldMouse);

    // Render world
    lvls.draw();

    // Render optional collectables
    for (int i = collectables.size()-1; i >= 0; i--) {
      Collectable coll = collectables.get(i);
      coll.animate();
      if (game.utils.isInRect(player.pos, PVector.sub(coll.pos, coll.size), PVector.mult(coll.size, 2))) {
        coll.pickup();
        collectables.remove(coll);
      }
    }

    // Render required collectables
    for (int i = requiredCollectables.size()-1; i >= 0; i--) {
      Collectable coll = requiredCollectables.get(i);
      coll.animate();
      if (game.utils.isInRect(player.pos, PVector.sub(coll.pos, coll.size), PVector.mult(coll.size, 2))) {
        coll.pickup();
        requiredCollectables.remove(coll);
      }
    }

    // Render enemies
    for (int i = enemies.size()-1; i >= 0; i--) {
      final Enemy enemy = enemies.get(i);
      if (!enemy.isDead)
        enemy.draw();
      // Handle drops and removing of enemies if they're dead
      else {
        enemy.drop();
        enemies.remove(i);
      }
    }

    // Render and update player
    player.draw();
    player.update();

    // Draw HUD (we pop matrix in here, scummy way of doing things)
    hud.draw();

    // Update timer
    if (!timer.hasFinished())
      timer.update();
    else
      state = GameplayState.TIMER_OUT;

    // Player death (remove life and change state)
    if (player.health <= 0) {
      lives--;
      state = GameplayState.DEAD;
    }
  }
}
// The in game hud for displaying information
// I appologise for this mess of a class, it was made when I was a little drunk
// on new years so everything is a bit all over the place (like popping the camera matrix inside of here)

class Hud {
  public void draw() {
    if (game.DEBUG)
      drawDebug();
    else
      drawGame();
  }

  // Draw the hud used for debugging (map editor)
  public void drawDebug() {
    // Also draw the game HUD (well, to debug the HUD)
    drawGame();

    textAlign(LEFT, BOTTOM);
    textSize(16);
    text("DEBUG MODE - " + (game.gameplay.lvls.placingObjects ? "OBJECTS" : "TILES"), 0, 16);
    text("lrMouse tile change", 0, 32);
    text("mMouse switch t/o", 0, 48);
    text("B to switch BIOME", 0, 64);
    text("enter save", 0, 80);
  }

  // Draw the hud for gameplay
  public void drawGame() {
    strokeWeight(1);
    stroke(0);
    
    // ENEMIES HUD
    // --------------------------
    // Just because I'm lazy, I'll be inconsistent and use CENTER rectMode
    rectMode(CENTER);
    fill(255, 0, 0);
    for (Enemy enemy : game.gameplay.enemies) {
      final PVector size = enemy.getSize();
      rect(enemy.pos.x, enemy.pos.y-(size.y/2)-10, enemy.health, 5);
    }
    rectMode(CORNER);
    fill(255);
    popMatrix();

    // PLAYER HUD
    // --------------------------
    // Draw HUD outline
    fill(0, 0, 0, 100);
    rect(0, height-80, 135, 80);

    // Draw timer
    textAlign(LEFT, BOTTOM);
    textSize(15);
    fill(255);
    text("Timer: " + (int)game.gameplay.timer.getRemaining(), 10, height-58);

    // Draw score
    textSize(15);
    fill(255);
    text("Score: " + game.gameplay.score, 10, height-38);

    // Draw stamina
    textSize(10);
    fill(255);
    text("ST", 10, height-24);
    fill(0, 255, 0);
    rect(30, height-35, game.gameplay.player.stamina, 10);

    // Draw health
    textSize(10);
    fill(255);
    text("HP", 10, height-9);
    fill(255, 0, 0);
    rect(30, height-20, game.gameplay.player.health, 10);

    fill(255);
  }
}
// Input handler since the built in one is not as reliable or smooth as I'd like
// it's essentially a sort of loose replica of the one I have implemented in my C++ game engine
// it's okay, not production level code but it does it's job (it also doesn't clean up)





public class InputState
{
  public boolean framePressed;  // Was pressed this frame
  public boolean frameReleased; // Was released this frame
  public boolean held;          // Currently being held / pressed
  
  public InputState()
  { setState(false, false, false); }

  public InputState(boolean pressed)
  { setState(pressed, pressed, false); }

  public void setState(boolean _pressed, boolean _held, boolean _released)
  {
    framePressed = _pressed;
    held = _held;
    frameReleased = _released;
  }
}

class Input
{
  // All input states
  private int mouseScroll = 0;
  private HashMap<Integer, InputState> _keyStates = new HashMap<Integer, InputState>();
  private HashMap<Integer, InputState> _mouseStates = new HashMap<Integer, InputState>();
  private ArrayList<InputState> releasePressed = new ArrayList<InputState>();

  // For optimisation purposes
  boolean keyChanged = false;
  boolean mouseChanged = false;

  // Getters
  // --------------------------------
  // Get key state or add if doesn't exist
  public InputState getKey(int keyCode_)
  {
    InputState state = _keyStates.get(keyCode_);
    if (state == null) {
      state = new InputState();
      _keyStates.put(keyCode_, new InputState());
    }
    return state;
  }

  // Wrapper for using characters
  public InputState getKey(char key_)
  {
    KeyStroke keyStroke = KeyStroke.getKeyStroke(Character.toUpperCase(key_), 0);
    InputState state = getKey(keyStroke.getKeyCode());
    return state;
  }

  // Wrapper for using characters
  public InputState[] getKeys(char[] keys_)
  {
    if (keys_.length == 0) return null;
    InputState[] states = new InputState[keys_.length];
    for (int i = 0; i < keys_.length; i++)
      states[i] = getKey(keys_[i]);
    return states;
  }

  // Get key state or add if doesn't exist
  public InputState getMouse(int mouseNum_)
  {
    InputState state = _mouseStates.get(mouseNum_);
    if (state == null) {
      state = new InputState();
      _mouseStates.put(mouseNum_, new InputState());
    }
    return state;
  }

  public int getScroll() {
    return mouseScroll;
  }

  
  // Events
  // --------------------------------
  // Key pressed current frame
  public void keyPress()
  {
    InputState current = getKey(keyCode);
    if (current.held) return;
    keyChanged = true;
    current.setState(true, true, false);
    releasePressed.add(current);
    
    // Prevent escape key from closing game
    if (key == ESC) {
      key = 0;
    }
  }
  
  // Key released current frame
  public void keyRelease()
  {
    keyChanged = true;
    InputState current = getKey(keyCode);
    current.setState(false, false, true);
    releasePressed.add(current);
  }
  
  // Mouse pressed current frame
  public void mousePress()
  {
    InputState current = getMouse(mouseButton);
    if (current.held) return;
    mouseChanged = true;
    current.setState(true, true, false);
    releasePressed.add(current);
  }
  
  // Mouse released current frame
  public void mouseRelease()
  {
    mouseChanged = true;
    InputState current = getMouse(mouseButton);
    current.setState(false, false, true);
    releasePressed.add(current);
  }

  public void mouseScroll(MouseEvent event) {
    mouseChanged = true;
    mouseScroll = event.getCount();
  }

  // Reset back to empty state
  public void resetFrame()
  {
    keyChanged = false;
    mouseChanged = false;
    if (mouseScroll != 0) mouseScroll = 0;
    if (releasePressed.size() == 0) return;
    for (int i = 0; i < releasePressed.size(); i++) {
      InputState state = releasePressed.remove(0);
      state.setState(false, state.held, false);
    }
  }
}
// All the current levels in the game as well as the base level class and manager
// Could very well use a tile sheet instead of multiple images using img.get(x, y, w, h)
// Probably gonna make a layering system eventually int[][][] using +/- in map editor





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
  public void loadLevels() {
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

  public void randomLevel() {
    game.gameplay.requiredCollectables.clear();
    game.gameplay.collectables.clear();
    game.gameplay.enemies.clear();
    current = levels[(int)random(0, levels.length)];
    current.reset();
  }

  // What a complete mess lol
  public void drawGrid(int[][] grid, Tile[] tiles) {
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

  public void draw() {
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
  public void mapEditor(int[][] grid, Tile[] tiles) {
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
  public void init() {
    totalSize = new PVector(
      tileSize * tileGrid[0].length,
      tileSize * tileGrid.length
    );
  }

  public void spawnCollectable(ArrayList<PVector> canSpawnCoords, int type, int number) {
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

  public ArrayList<PVector> getAvailableSpawns() {
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
  
  public void spawnItems() {
    // Get positions at which objects can be placed!
    ArrayList<PVector> canSpawnCoords = getAvailableSpawns();
    
    // Spawn 3 collectables as per the specification
    spawnCollectable(canSpawnCoords, 0, 3);
    spawnCollectable(canSpawnCoords, 1, (int)random(0, 3));
    spawnCollectable(canSpawnCoords, 2, (int)random(0, 3));
  }
  
  public void spawnEnemies(int number) {
    // Spawn enemies in locations
    for (int i = 0; i < number; i++) {
      game.gameplay.enemies.add(new Enemy());
    }
  }

  public void reset() {
    game.gameplay.timer.reset();
    game.gameplay.player.pos.set(0, 0);
    spawnItems();
    spawnEnemies((int)random(0, 3 + (game.gameplay.score / 1000)));
  }

  // Original state of the map if not loaded
  public void defineArr(int def) {
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
class MainMenu {
  PImage bg;
  Button[] buttons;
  ParticleSystem particleManager;

  MainMenu() {
    bg = loadImage("mainMenu.png");
    bg.resize(width, height);
    particleManager = new ParticleSystem(0, 0, "petal.png");

    int textSize = 65;
    int buttonSize = 85;
    float centerY = height/2;
    
    // Could be done programatically but whatever, just see if it even works
    buttons = new Button[] {
      new Button(game.center.x, (centerY-buttonSize)-(buttonSize/2), 500, buttonSize, "Start Game", textSize),
      new Button(game.center.x, centerY, 500, buttonSize, "Options", textSize),
      new Button(game.center.x, (centerY+buttonSize)+(buttonSize/2), 500, buttonSize, "Exit", textSize)
    };
  }

  // I'd love to pass lambda functions or function pointers to my Button class instead
  // but java 8 doesn't allow that..........
  public void pressedBtn(int idx) {
    particleManager.destroy();
    strokeWeight(1);
    switch (idx) {
      case 0:
        game.sound.changeMusic(game.sound.gameplay);
        game.state = GameState.GAMEPLAY;
        break;
      case 1:
        game.state = GameState.OPTIONS;
        break;
      case 2:
        exit();
        break;
    }
  }

  public void draw() {
    // Draw background
    image(bg, 0, 0);

    // Draw particles
    particleManager.draw();

    // Draw and handle buttons
    int idx = 0;
    for (Button btn : buttons) {
      btn.draw();
      if (btn.justHovered())
        game.sound.menuHover.play();
      if (btn.isPressed())
        pressedBtn(idx);
      idx++;
    }
  }
}
// Doesn't need another class but I'm too lazy to switch the UI element arrays so here we are

class OptionsMenu {
  PImage bg;
  Button[] buttons;
  ParticleSystem particleManager;

  OptionsMenu() {
    bg = loadImage("mainMenu.png");
    bg.resize(width, height);
    particleManager = new ParticleSystem(0, 0, "petal.png");

    int textSize = 65;
    int buttonSize = 85;
    float centerY = height/2;
    
    // Could be done programatically but whatever, just see if it even works
    buttons = new Button[] {
      new Button(game.center.x, (centerY-buttonSize)-(buttonSize/2), 500, buttonSize, "Debug: " + (game.DEBUG ? "On" : "Off"), textSize),
      new Button(game.center.x, centerY, 500, buttonSize, "Placeholder", textSize),
      new Button(game.center.x, (centerY+buttonSize)+(buttonSize/2), 500, buttonSize, "Back", textSize)
    };
  }

  // I'd love to pass lambda functions or function pointers to my Button class instead
  // but java 8 doesn't allow that..........
  public void pressedBtn(int idx) {
    switch (idx) {
      case 0:
        game.DEBUG = !game.DEBUG;
        buttons[0].text = "Debug: " + (game.DEBUG ? "On" : "Off");
        break;
      case 1:
        game.state = GameState.OPTIONS;
        break;
      case 2:
        particleManager.destroy();
        strokeWeight(1);
        game.state = GameState.MENU;
        break;	
    }
  }

  public void draw() {
    // Draw background
    image(bg, 0, 0);

    // Draw particles
    particleManager.draw();

    // Draw and handle buttons
    int idx = 0;
    for (Button btn : buttons) {
      btn.draw();
      if (btn.justHovered())
        game.sound.menuHover.play();
      if (btn.isPressed())
        pressedBtn(idx);
      idx++;
    }
  }
}
// I could probably repurpose this for other things,
// simply used for a nice looking starting menu 4 now

class ParticleSystem {
  PImage img;
  PVector pos;
  int particlesPerTic = 1;
  int volatileAmount = 1;
  int volatileChance = 20;
  float counter = 0;
  ArrayList<Particle> particles;

  ParticleSystem(PVector pos_) {
    pos = pos_;
    particles = new ArrayList<Particle>();
  }

  ParticleSystem(PVector pos_, String img_) {
    this(pos_);
    img = game.utils.loadImages(img_, false)[0].img;
    img.resize(35, 35);
  }

  ParticleSystem(float x, float y) {
    this(new PVector(x, y));
  }

  ParticleSystem(float x, float y, String img_) {
    this(new PVector(x, y), img_);
  }

  public void addParticle() {
    particles.add(new Particle(this.pos, random(3, 7), img));
  }

  public void draw() {
    counter += game.deltaTime;
    if (counter >= particlesPerTic) {
      addParticle();
      counter = 0;

      // Percentage chance to spawn multiple
      if (random(0, 100) <= volatileChance) {
        for (int i = 0; i < volatileAmount; i++)
          addParticle();
      }
    }

    for (int i = particles.size()-1; i > 0; i--) {
      Particle particle = particles.get(i);
      // Remove "dead" particles
      if (particle.life <= 0) {
        particles.remove(i);
        return;
      }

      // Draw the particles
      particle.draw();
    }    
  }

  public void destroy() {
    particles.clear();
  }

  // Set particles per second
  public void setPPS(int num) {
    particlesPerTic = 1 / num;
  }
}

class Particle extends BaseEntity {
  PImage img;
  float life;
  float totalLife;
  float rotation = 90;
  boolean rotateReverse;
  
  Particle(PVector pos, float lifeSpan_) {
    life = lifeSpan_;
    totalLife = lifeSpan_;
    rotateReverse = (random(0, 1) == 1);
    vel = new PVector(random(0.5f, 4), random(1, 2));
  }
  
  Particle(PVector pos, float lifeSpan_, PImage img_) {
    this(pos, lifeSpan_);
    if (img_ != null)
      img = img_;
  }

  // Update position and life, then draw
  public void draw() {
    // Update position based upon velocity
    this.pos.add(this.vel);

    // Draw particle
    if (img == null) {
      fill(255,255,255, floor(255 * (life / totalLife)));
      ellipse(this.pos.x, this.pos.y, 10, 10);
    }
    else {
      // Why is rotation so annoying?
      pushMatrix();
      translate(pos.x, pos.y);
      rotate(radians(rotation));
      tint(255, floor(255 * (life / totalLife)));
      image(img, img.width/2, img.height/2);
      popMatrix();
      noTint();

      if (rotation >= 120)
        rotateReverse = true;
      if (rotation <= 60)
        rotateReverse = false;
        
      double amount = 60 * game.deltaTime;
      rotation += rotateReverse ? -amount : amount;
    }

    // Update life
    life -= game.deltaTime;
  }
}
// My implementation of the A star algorithm

 

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
  public @Override
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
  public Node[][] initNodes() {
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
  public void updateCollidables(Node[][] nodes) {
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
  public float nodeDistance(Node a, Node b) {
    return game.utils.distanceTo(a.x, a.y, b.x, b.y);
  }

  // Solve without trying all neighbours, fastest in terms of performance,
  // might not be the fastest path
  public void solve(NodeInfo nodeInfo) {
    solve(nodeInfo, false);
  }

  // Solve path between points using A-Star
  // Probably implemented incorrect but whatevs lawl it works
  public void solve(NodeInfo nodeInfo, boolean inDepth) {
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
  public ArrayList<Node> getPath(NodeInfo nodeInfo) {
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
  public void draw(NodeInfo nodeInfo) {
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
class PauseScreen {
  PImage bg;
  Button[] buttons;
  int buttonSize;

  PauseScreen() {
    bg = loadImage("mainMenu.png");
    bg.resize(width, height);

    int textSize = 65;
    buttonSize = 85;
    
    // Could be done programatically but whatever, just see if it even works
    buttons = new Button[] {
      new Button(game.center.x, game.center.y-(buttonSize/2)-20, 500, buttonSize, "Resume", textSize),
      new Button(game.center.x, game.center.y+(buttonSize/2)+20, 500, buttonSize, "Exit", textSize)
    };
  }

  // I'd love to pass lambda functions or function pointers to my Button class instead
  // but java 8 doesn't allow that..........
  public void pressedBtn(int idx) {
    strokeWeight(1);
    switch (idx) {
      case 0:
        game.sound.changeMusic(game.sound.gameplay);
        game.gameplay.state = GameplayState.PLAYING;
        break;
      case 1:
        exit();
        break;
    }
  }

  public void draw() {
    if (game.input.getKey(ESC).framePressed || game.input.getKey('p').framePressed)
      game.gameplay.state = GameplayState.PLAYING;

    // Draw background
    image(bg, 0, 0);

    // Draw and handle buttons
    int idx = 0;
    for (Button btn : buttons) {
      btn.draw();
      if (btn.justHovered())
        game.sound.menuHover.play();
      if (btn.isPressed())
        pressedBtn(idx);
      idx++;
    }

    // Draw score and lives
    textSize(32);
    fill(0);
    text("Score: " + game.gameplay.score, game.center.x, game.center.y-(buttonSize/2)-130);
    text("Lives: " + game.gameplay.lives, game.center.x, game.center.y-(buttonSize/2)-100);
    fill(255);
  }
}
class Slash extends BaseEntity {
  Slash() {
    super(null, "player/slash", true);
    super.setAnimOnce(true);
    super.setAnimFps(35);
    super.setScale(0.5f);
    super.setAnimPos(19);
  }
}

class Player extends BaseEntity {
  float damageCooldown = 0;
  Slash slashEffect;
  float speed = 300;
  float health = 100;
  float stamina = 100;
  float damage = 50;

  // Collected collectables


  // Spawn player at center of world, looking down by default
  Player() {
    super(null, "player/char", true);
    super.setAnim("down");
    super.setAnimFps(6);
    super.setScale(1.5f);

    slashEffect = new Slash();
  }
  
  // I know if you don't switch direction, it won't reset timeCount
  // if you hold opposing directions it'll also animate in place
  public void charMoved(String dir) {
    if (moved) return;
    moved = true;
    super.setAnim(dir);
  }
  
  // Handle movement, move 300 pixels per second
  // and attacking 
  public void controls() {
    // Movement
    float speedD = (float)(speed * game.deltaTime);
    if (game.input.getKey('w').held || game.input.getKey(UP).held) {
      vel.y += -speedD;
      charMoved("up");
    }
    if (game.input.getKey('s').held || game.input.getKey(DOWN).held) {
      vel.y += speedD;
      charMoved("down");
    }
    if (game.input.getKey('a').held || game.input.getKey(LEFT).held) {
      vel.x += -speedD;
      charMoved("left");
    }
    if (game.input.getKey('d').held || game.input.getKey(RIGHT).held) {
      vel.x += speedD;
      charMoved("right");
    }

    // Attacking
    if (game.input.getKey(' ').framePressed) {
      if (slashEffect.animFrame >= 19 && stamina >= 30) {
        slashEffect.setAnimPos(0);

        // Play attack sound
        game.sound.attack.play();

        // Play sound effect and lower stamina
        stamina -= 30;

        // Do damage to enemies in range
        for (Enemy enemy : game.gameplay.enemies) {
          final float distance = game.utils.distanceTo(pos.x, pos.y, enemy.pos.x, enemy.pos.y);
          if (distance <= 100) {
            enemy.damage(this.damage);
          }
        }
      }
    }
  }

  public void damage(float dmg) {
    if (this.damageCooldown > 0) return;
    if (this.health <= 0) return;
    game.sound.hurt.play();
    this.damageCooldown = 0.5f;
    this.health -= dmg;
  }

  public void heal(float hp) {
    this.health += hp;
    game.sound.heal.play();
    if (this.health > 100)
      this.health = 100;
  }

  // Check collisions and move
  public void update() {
    // Set velocity based upon collision
    super.handleCollision();

    // Update position based on velocity and reset
    pos.add(vel);
    vel.set(0, 0);
    moved = false;
  }

  public void draw() {
    // Draw player
    if (!moved || (vel.x == 0 && vel.y == 0))
      super.drawImage(0);
    else
      super.animate();

    // Reset damage cooldown
    if (damageCooldown > 0)
      damageCooldown -= game.deltaTime;

    // Check stamina and regen
    if (stamina > 100)
      stamina = 100;
    else if (stamina < 100)
      stamina += 20 * game.deltaTime;

    // Draw slash (I was originally going to make this rotate with the player but translations in processing are a PAIN
    // so like I ain't doing that)
    if (slashEffect.animFrame < 19 || !slashEffect.animOnce) {
      // Animate/draw the attack
      slashEffect.pos = this.pos.copy();
      slashEffect.pos.y -= 20;
      slashEffect.animate();
    }
  }
}
// Not used currently

static class Settings {
  static boolean isFullscreen = false;
  static int leftKey, rightKey, downKey, upKey;
}







// My sound player. Origianlly used the processing sound library but then figured it's better ifI didn't use
// a library and instead created my own since it's not that hard in Java. I'm just using wav files.
// Uses C style erroring. If you wish to get an error from the system, use the getLastError() method.
// Volume control is a bit annoying because it doesn't affect the current buffer and it's a pain to change
// the buffer size to account for the time taken to exhaust the buffer. It's a bit botched but it's alright

// States of sound class
enum SoundState {
    PAUSED,
    STOPPED,
    PLAYING,
    LOOPING
}

class Sound {
    long curPos;                    // Current frame in sound - for pausing
    Clip clip;                      // Audio clip
    String filePath;                // Path to file
    SoundState state;               // Current state of the player
    AudioInputStream audioStream;   // Current audio stream
    
    // Some nice things to have
    float volume = 1.0f;                   // Current volume
    float desiredVolume = 1.0f;            // Volume to fade to

    // C style erroring because I'm lazy
    Exception lastError;            // Last error from the system
  
    // Setup a sound instead with reference to a file
    public Sound(String file) {
        // Attempt to create audio stream and setup the clip
        try {
            state = SoundState.STOPPED;
            filePath = dataPath(file);      // Set path
            clip = AudioSystem.getClip();   // Create a clip
            createStream();                 // Create stream and open clip
        }
        // Requirement since AudioSystem throws
        catch (Exception e) {
            lastError = e;
        }
    }

    // If we're paused, run this code before playing or looping
    private boolean preResume() {
        try {
            if (state == SoundState.PAUSED) {
                clip.close();
                createStream();
                clip.setMicrosecondPosition(curPos);
            }
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }
    
    // Attemp to play the audio without looping (or resume)
    public boolean play() {
        try {
            // If we're currently playing, seek back to the start
            if (state == SoundState.PLAYING)
                clip.setFramePosition(0);

            preResume();
            clip.start();
            state = SoundState.PLAYING;
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }

    // Attemp to loop audio (or resume)
    public boolean loop() {
        if (state == SoundState.LOOPING) return false;

        try {
            preResume();
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            state = SoundState.LOOPING;
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }

    // Method to pause the audio 
    public boolean pause() {
        if (state == SoundState.PAUSED) return false;

        try {
            curPos = clip.getMicrosecondPosition();
            clip.stop();
            state = SoundState.PAUSED;
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }

    // Reset without autoplaying
    public boolean reset() {
        // Stop clip and close stream
        if (!stop())
            return false;

        // Recreate the stream
        return createStream();
    }

    // Reset with autoplaying 
    public boolean restart() {
        try {
            // Reset everything
            reset();

            // Start playing again
            this.play();
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }
      
    // Method to stop the audio 
    public boolean stop() {
        try {
            state = SoundState.STOPPED;
            curPos = 0;
            clip.stop();
            clip.close();
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }

    // Set the gain of the audio c
    public boolean setVol(float gain) {
        try {
            // The gain controller for the clip
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            
            // Convert the percentage to dB (I square the percentage to make it more "logarithmic")
            // If you asked me how this conversion works though, I have no idea
            float dB = (float) (Math.log(Math.pow(gain, 2)) / Math.log(10.0f) * 20.0f);
            
            // Update the volume
            volume = gain;
            gainControl.setValue(dB);
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }

    // Set desired volume for fading
    public void setDesiredVol(float gain) {
        desiredVolume = gain;
    }

    // Check if we're playing
    public boolean isPlaying() {
        if (state == SoundState.PAUSED || state == SoundState.STOPPED)
            return false;
        return true;
    }

    // Method to jump over a specific part 
    public boolean seek(long pos) {
        try {
            if (pos < 0 || pos > clip.getMicrosecondLength()) 
                throw new Exception("position out of bounds");

            curPos = pos;
            clip.setMicrosecondPosition(pos);
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }

    // Method to create audio stream
    public boolean createStream()
    {
        try {
            audioStream = AudioSystem.getAudioInputStream(new File(filePath));
            clip.open(audioStream);
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }
}
// I did originally use mp3 files but they caused buzzing at extended play
// could make this infinitly better, like providing file names in as input
// loading it into a cache and then just grabbing from the cache instead
// of manually loading all these effects explicitly in the manager but I'll do that later

class SoundManager {
  // Sound files
  Sound mainMenu;
  Sound gameplay;

  // Menu sounds
  Sound menuHover;
  Sound collectGood;
  Sound attack;
  Sound hurt;
  Sound heal;

  // Current playing bg music (used for fading)
  Sound curBgMusic;
  Sound changeToBgMusic;

  SoundManager() {
    // Load sound effects
    menuHover = new Sound("sounds/hover.wav");
    collectGood = new Sound("sounds/collectgood.wav");
    attack = new Sound("sounds/attack.wav");
    hurt = new Sound("sounds/hurt.wav");
    heal = new Sound("sounds/heal.wav");

    // Load music
    mainMenu = new Sound("sounds/mainmenu.wav");
    gameplay = new Sound("sounds/gameplay.wav");
    mainMenu.setDesiredVol(0.75f);
    gameplay.setDesiredVol(0.75f);
    changeToBgMusic = null;
    curBgMusic = mainMenu;

    // Automatically start playing main menu music
    mainMenu.setVol(0.85f);
    mainMenu.loop();
  }

  // Switch between music
  public void changeMusic(Sound changeTo) {
    // Don't need to change
    if (changeTo == curBgMusic) return;

    // Set music to change to
    changeToBgMusic = changeTo;
  }

  // Simply used to fade between sounds
  boolean test = false;
  public void update() {
    // Change volumes to fade between the two
    if (changeToBgMusic != null) {
      // Set volume and start playing new track
      if (!changeToBgMusic.isPlaying()) {
        changeToBgMusic.setVol(0.05f);
        changeToBgMusic.loop();
      }

      // Change volumes
      boolean hasFinished = true;
      if (curBgMusic.volume > 0) {
        curBgMusic.setVol(curBgMusic.volume - (0.2f * (float)game.deltaTime));
        hasFinished = false;
      }
      if (changeToBgMusic.volume < changeToBgMusic.desiredVolume) {
        changeToBgMusic.setVol(changeToBgMusic.volume + (0.2f * (float)game.deltaTime));
        hasFinished = false;
      }
      
      // If lower frame rates overshot, correct
      if (changeToBgMusic.volume > (changeToBgMusic.desiredVolume + 0.1f))
        changeToBgMusic.setVol(changeToBgMusic.volume);

      // If finished, update current playing
      if (hasFinished) {
        curBgMusic.stop();
        curBgMusic.setVol(curBgMusic.desiredVolume);
        curBgMusic = changeToBgMusic;
        changeToBgMusic = null;
      }
    }
  }
}
// This was done last lol, I just wanted it to be over with

class SplashScreen {
  float timer = 0;

  public void draw() {
    // Increment timer
    timer += game.deltaTime;

    // Draw score and lives
    textSize(28);
    fill(255);
    textAlign(CENTER, CENTER);
    text("Information and controls in the README!\n HOWEVER here are the essentials:\n\nWASD / ARROW KEYS - Movement\nSpacebar - Attack\n\nGame created by Kyle Pelham (ID: 20050825)", game.center.x, game.center.y);

    // If we're over time, just change state
    if (timer > 10)
      game.state = GameState.MENU;
  }
}
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
// Since I already kinda have timing with the implementation of delta time
// I figured I'd just make a very simple timer that increments with deltaTime

class Timer {
  // Time in seconds
  private float ellapsed = 0;   // Past
  private float limit = 60;     // Stop at ( 60 seconds )

  // Things that actually matter
  public void update() {
    increment((float)game.deltaTime);
  }

  public void reset() {
    setEllapsed(0);
  }


  // Getters
  public float getEllapsed() {
    return this.ellapsed;
  }

  public float getRemaining() {
    return limit - ellapsed;
  }

  public boolean hasFinished() {
    return ellapsed >= limit;
  }


  // Setters / mutation
  public void increment(float amount) {
    ellapsed += amount;
  }

  public void decrement(float amount) {
    ellapsed -= amount;
  }

  public void setEllapsed(float time) {
    ellapsed = time;
  }

  public void setLimit(float time) {
    limit = time;
  }
}
// Made static originally but found many limitations with that approach




// Convience class for storing rectangles
class Rect {
  PVector pos;
  PVector size;

  Rect() {
    pos = new PVector(0, 0);
    size = new PVector(0, 0);
  }

  Rect(float x, float y, float w, float h) {
    pos = new PVector(x, y);
    size = new PVector(w, h);
  }

  Rect(PVector pos_, PVector size_) {
    pos = pos_;
    size = size_;
  }
}

// Class for storing the result of projection collision
class CollisionResult {
  PVector pointOfContact;
  PVector contactNormal;
  float timeHit;
}

class Utils {
  public boolean hoveredRect(float x, float y, float width_, float height_)  {
    if (mouseX >= x && mouseX <= x + width_ && mouseY >= y && mouseY <= y + height_)
      return true;
    return false;
  }

  public boolean hoveredRect(PVector pos, float width_, float height_)  {
    return hoveredRect(pos.x, pos.y, width_, height_);
  }

  public boolean hoveredCircle(float x, float y, float diameter) {
    float disX = x - mouseX;
    float disY = y - mouseY;
    if (sqrt(sq(disX) + sq(disY)) < diameter/2 ) 
      return true;
    return false;
  }

  public boolean hoveredRect(PVector pos, float diameter)  {
    return hoveredCircle(pos.x, pos.y, diameter);
  }

  // Load and filter folder contents
  public String[] filesInFolder(String folderStr, FilenameFilter filter) {
    File folder = new File(dataPath(folderStr));
    String[] files;
    if (filter != null)
      files = folder.list(filter);
    else
      files = folder.list();
    
    // Return sorted files, needs to be done for linux (tested on laptop)
    // .list() returns differently on linux
    return sort(files);
  }

  // Load folder contents without a filter
  public String[] filesInFolder(String folderStr) {
    return filesInFolder(folderStr, null);
  }

  // Load sprite or spriteS based upon the if its folder and the filter
  public Sprite[] loadImages(FilenameFilter filter, String fileOrFolder, boolean isFolder) {
    // Load images
    if (isFolder) {
      // Check if user provided "path/" or "path" and adapt path accordingly
      String addForwardSlash = fileOrFolder.substring(fileOrFolder.length() - 1) == "/" ? "" : "/";

      // Load and filter folder contents
      if (filter == null)
        filter = new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".png");
          }
        };
      String[] files = filesInFolder(fileOrFolder, filter);
      
      // Load sprites
      Sprite[] sprites = new Sprite[files.length];
      for (int i = 0; i < files.length; i++)
        sprites[i] = new Sprite(fileOrFolder + addForwardSlash + files[i]);
      return sprites;
    }
    // Load image (singular)
    return new Sprite[] { new Sprite(fileOrFolder) };
  }
  
  // Just so I don't have to change my existing code
  public Sprite[] loadImages(String fileOrFolder, boolean isFolder) {
    return loadImages(null, fileOrFolder, isFolder);
  }

  // Allow for specifying prefix filter (useful to have, however not used)
  public Sprite[] loadImages(String fileOrFolder, boolean isFolder, final String prefix) {
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        String lowerName = name.toLowerCase();
        return lowerName.endsWith(".png") && lowerName.startsWith(prefix);
      }
    };
    return loadImages(filter, fileOrFolder, isFolder);
  }

  // Convert mouse position to world coordinates
  public PVector translateMouse(PVector pos) {
    float newMouseX = mouseX - pos.x-game.center.x;
    float newMouseY = mouseY - pos.y-game.center.y;
    return new PVector(newMouseX, newMouseY);
  }

  // Convert mouse position to world coordinates (more efficent for multiple calls)
  public void translateMouse(PVector pos, PVector out) {
    out.x = mouseX + pos.x-game.center.x;
    out.y = mouseY + pos.y-game.center.y;
  }

  // Check if the screen rectangle overlaps point
  public boolean onScreen(float x, float y, float w, float h) {
    PVector cameraPos = game.gameplay.camera.follow.pos;
    float screenX = cameraPos.x - game.center.x;
    float screenY = cameraPos.y - game.center.y;

    if (x < (screenX + width) && (x+w) > screenX && (y+h) > screenY && y < (screenY+height))
      return true;
    return false;
  }
  
  public boolean isInRect(PVector playerPos, PVector pos, PVector size)  {
    if (playerPos.x >= pos.x && playerPos.x <= pos.x + size.x && playerPos.y >= pos.y && playerPos.y <= pos.y + size.y)
      return true;
    return false;
  }

  // Serialize (really ghetto but processing keeps saying that the <project name> is not serializable)
  // whenever I try and use an actual custom made class...
  public boolean saveObject(Object obj, String path) {
    try {
      FileOutputStream outStream = new FileOutputStream(dataPath(path));
      ObjectOutputStream out = new ObjectOutputStream(outStream);
      out.writeObject(obj);
      out.close();
      outStream.close();
    } catch (Exception ex) {
      println(ex);
      return false;
    }
    return true;
  }

  public Object loadObject(String path) {
    try {
      FileInputStream fileIn = new FileInputStream(dataPath(path));
      ObjectInputStream objectIn = new ObjectInputStream(fileIn);
      Object obj = objectIn.readObject();
      objectIn.close();
      return obj;
    } catch (Exception ex) {
      println(ex);
      return null;
    }
  }

  // Projection/ray based collision (most accurate and robust with a cost to performance)
  public CollisionResult RayCollisionPoint(PVector rayOrigin, PVector rayDir, Rect target) {
    // Invert becos faster
    PVector inverseDir = new PVector(1, 1);
    inverseDir.x /= rayDir.x;
    inverseDir.y /= rayDir.y;

    // Intersections
    PVector near = PVector.sub(target.pos, rayOrigin);
    near.x *= inverseDir.x;
    near.y *= inverseDir.y;
    PVector far = PVector.sub(PVector.add(target.pos, target.size), rayOrigin);
    far.x *= inverseDir.x;
    far.y *= inverseDir.y;


    // Check for NaN
    if (Float.isNaN(near.x) || Float.isNaN(near.y) || Float.isNaN(far.x) || Float.isNaN(far.y))
      return null;

    // Fix distances
    if (near.x > far.x) {
      float newFar = near.x;
      near.x = far.x;
      far.x = newFar;
    }
    if (near.y > far.y) {
      float newFar = near.y;
      near.y = far.y;
      far.y = newFar;
    }

    // Early call
    if (near.x > far.y || near.y > far.x)
      return null;

    // Furthest intersection time
    float tHitFar = Math.min(far.x, far.y);

    // Check direction correct
    if (tHitFar < 0)
      return null;

    // Result obj
    CollisionResult result = new CollisionResult();
    result.timeHit = Math.max(near.x, near.y);    // Closest intersection time
    result.pointOfContact = PVector.add(          // Contact point
      rayOrigin,
      PVector.mult(rayDir, result.timeHit)
    );

    // Get normals
    result.contactNormal = new PVector(0, 0);
    if (near.x > near.y)
      if (inverseDir.x < 0)
        result.contactNormal.set(1, 0);
      else
        result.contactNormal.set(-1, 0); 
    else
      if (inverseDir.y < 0)
        result.contactNormal.set(0, 1);
      else
        result.contactNormal.set(0, -1);

    // Contact point of collision from parametric line equation
    return result;
  }

  public CollisionResult RayCollisionRect(PVector rayOrigin, PVector size, PVector rayDir, Rect target) {
    // Check we're even moving
    if (rayDir.x == 0 && rayDir.y == 0)
      return null;

    // Expand target by src
    Rect expanded = new Rect();
    expanded.pos = PVector.sub(target.pos, PVector.div(size, 2));
    expanded.size = PVector.add(target.size, size);

    // Check if collision, return result
    CollisionResult result = RayCollisionPoint(rayOrigin, rayDir, expanded);
    if (result != null && result.timeHit >= 0 && result.timeHit < 1.0f)
      return result;
    return null;
  }

  // Also resolve the collision, don't just check
  public CollisionResult RayCollisionRectRes(PVector rayOrigin, PVector size, PVector rayDir, Rect target) {
    CollisionResult result = RayCollisionRect(rayOrigin, size, rayDir, target);

    // Attempt to resolve the collision by changing rayDir (vel)
    if (result != null) {
      PVector newVel = PVector.mult(new PVector(abs(rayDir.x), abs(rayDir.y)), (1-result.timeHit));
      newVel.x *= result.contactNormal.x;
      newVel.y *= result.contactNormal.y;
      rayDir.add(newVel);
    }
    return result;
  }

  // Get distance between two points using pythag
  public float distanceTo(float aX, float aY, float bX, float bY) {
    return (float)Math.sqrt((aY - bY) * (aY - bY) + (aX - bX) * (aX - bX));
  }
  
  public float distanceTo(PVector a, PVector b) {
    return distanceTo(a.x, a.y, b.x, b.y);
  }
}
  public void settings() {  size(1280, 720); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "main" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

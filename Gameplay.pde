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
  void init() {
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
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
  void init() {
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
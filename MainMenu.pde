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
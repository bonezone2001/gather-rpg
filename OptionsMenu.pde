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
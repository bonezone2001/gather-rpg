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
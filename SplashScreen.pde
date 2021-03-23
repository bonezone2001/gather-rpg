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
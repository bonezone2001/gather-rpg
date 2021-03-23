// The in game hud for displaying information
// I appologise for this mess of a class, it was made when I was a little drunk
// on new years so everything is a bit all over the place (like popping the camera matrix inside of here)

class Hud {
  void draw() {
    if (game.DEBUG)
      drawDebug();
    else
      drawGame();
  }

  // Draw the hud used for debugging (map editor)
  void drawDebug() {
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
  void drawGame() {
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
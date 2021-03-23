// Main hooks, they're here becauseI like the 
// encapsulation and having thing's in classes so
// I just called the game object's methods from here

// Main game object
Game game;

void setup()
{
  // This is here because I like playing at 144 hz, it should be 60 for you!
  frameRate(60);

  // Window title and size
  surface.setTitle("Really cool epic game");
  size(1280, 720);

  // Loading text (since loading audio takes some time)
  textSize(32);
  clear();
  textAlign(CENTER);
  text("GAME LOADING!", width/2, height/2);
  
  // Load game
  game = new Game();
  game.init();
}

void draw()
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
void keyPressed() { game.input.keyPress(); }
void keyReleased() { game.input.keyRelease(); }
void mousePressed() { game.input.mousePress(); }
void mouseReleased() { game.input.mouseRelease(); }
void mouseWheel(MouseEvent e) { game.input.mouseScroll(e); }
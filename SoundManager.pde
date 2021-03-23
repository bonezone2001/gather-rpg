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
    mainMenu.setDesiredVol(0.75);
    gameplay.setDesiredVol(0.75);
    changeToBgMusic = null;
    curBgMusic = mainMenu;

    // Automatically start playing main menu music
    mainMenu.setVol(0.85);
    mainMenu.loop();
  }

  // Switch between music
  void changeMusic(Sound changeTo) {
    // Don't need to change
    if (changeTo == curBgMusic) return;

    // Set music to change to
    changeToBgMusic = changeTo;
  }

  // Simply used to fade between sounds
  boolean test = false;
  void update() {
    // Change volumes to fade between the two
    if (changeToBgMusic != null) {
      // Set volume and start playing new track
      if (!changeToBgMusic.isPlaying()) {
        changeToBgMusic.setVol(0.05);
        changeToBgMusic.loop();
      }

      // Change volumes
      boolean hasFinished = true;
      if (curBgMusic.volume > 0) {
        curBgMusic.setVol(curBgMusic.volume - (0.2 * (float)game.deltaTime));
        hasFinished = false;
      }
      if (changeToBgMusic.volume < changeToBgMusic.desiredVolume) {
        changeToBgMusic.setVol(changeToBgMusic.volume + (0.2 * (float)game.deltaTime));
        hasFinished = false;
      }
      
      // If lower frame rates overshot, correct
      if (changeToBgMusic.volume > (changeToBgMusic.desiredVolume + 0.1))
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
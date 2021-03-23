class Slash extends BaseEntity {
  Slash() {
    super(null, "player/slash", true);
    super.setAnimOnce(true);
    super.setAnimFps(35);
    super.setScale(0.5);
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
    super.setScale(1.5);

    slashEffect = new Slash();
  }
  
  // I know if you don't switch direction, it won't reset timeCount
  // if you hold opposing directions it'll also animate in place
  void charMoved(String dir) {
    if (moved) return;
    moved = true;
    super.setAnim(dir);
  }
  
  // Handle movement, move 300 pixels per second
  // and attacking 
  void controls() {
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

  void damage(float dmg) {
    if (this.damageCooldown > 0) return;
    if (this.health <= 0) return;
    game.sound.hurt.play();
    this.damageCooldown = 0.5;
    this.health -= dmg;
  }

  void heal(float hp) {
    this.health += hp;
    game.sound.heal.play();
    if (this.health > 100)
      this.health = 100;
  }

  // Check collisions and move
  void update() {
    // Set velocity based upon collision
    super.handleCollision();

    // Update position based on velocity and reset
    pos.add(vel);
    vel.set(0, 0);
    moved = false;
  }

  void draw() {
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
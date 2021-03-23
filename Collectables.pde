class Collectable extends BaseEntity {
  float value = 0;    // Value determines how much it adds to the score for retrieving
  
  Collectable() {
    super(null, "tilesets/collectables/good", true);
    super.setAnimFps(5);
    super.setScale(1.5);
    // Randomize the value when created
    value = random(0, 50);
  }

  Collectable(String path) {
    super(null, path, true);
    super.setAnimFps(5);
    super.setScale(1.5);
    // Randomize the value when created
    value = random(0, 50);
  }

  void pickup() {
    game.gameplay.score += value;
    game.sound.collectGood.play();
  }
}

class DamagePickup extends Collectable {
  float damage = 0;   // Damage to do to player if picked up

  DamagePickup() {
    super("tilesets/collectables/bad");
    damage = random(0, 20);
  }

  void pickup() {
    game.gameplay.player.damage(damage);
  }
}

class HealthPickup extends Collectable {
  float hp = 0;   // Damage to do to player if picked up

  HealthPickup() {
    super("tilesets/collectables/heal");
    hp = random(0, 50);
  }

  void pickup() {
    game.gameplay.player.heal(hp);
  }
}
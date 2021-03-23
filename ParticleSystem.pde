// I could probably repurpose this for other things,
// simply used for a nice looking starting menu 4 now

class ParticleSystem {
  PImage img;
  PVector pos;
  int particlesPerTic = 1;
  int volatileAmount = 1;
  int volatileChance = 20;
  float counter = 0;
  ArrayList<Particle> particles;

  ParticleSystem(PVector pos_) {
    pos = pos_;
    particles = new ArrayList<Particle>();
  }

  ParticleSystem(PVector pos_, String img_) {
    this(pos_);
    img = game.utils.loadImages(img_, false)[0].img;
    img.resize(35, 35);
  }

  ParticleSystem(float x, float y) {
    this(new PVector(x, y));
  }

  ParticleSystem(float x, float y, String img_) {
    this(new PVector(x, y), img_);
  }

  void addParticle() {
    particles.add(new Particle(this.pos, random(3, 7), img));
  }

  void draw() {
    counter += game.deltaTime;
    if (counter >= particlesPerTic) {
      addParticle();
      counter = 0;

      // Percentage chance to spawn multiple
      if (random(0, 100) <= volatileChance) {
        for (int i = 0; i < volatileAmount; i++)
          addParticle();
      }
    }

    for (int i = particles.size()-1; i > 0; i--) {
      Particle particle = particles.get(i);
      // Remove "dead" particles
      if (particle.life <= 0) {
        particles.remove(i);
        return;
      }

      // Draw the particles
      particle.draw();
    }    
  }

  void destroy() {
    particles.clear();
  }

  // Set particles per second
  void setPPS(int num) {
    particlesPerTic = 1 / num;
  }
}

class Particle extends BaseEntity {
  PImage img;
  float life;
  float totalLife;
  float rotation = 90;
  boolean rotateReverse;
  
  Particle(PVector pos, float lifeSpan_) {
    life = lifeSpan_;
    totalLife = lifeSpan_;
    rotateReverse = (random(0, 1) == 1);
    vel = new PVector(random(0.5, 4), random(1, 2));
  }
  
  Particle(PVector pos, float lifeSpan_, PImage img_) {
    this(pos, lifeSpan_);
    if (img_ != null)
      img = img_;
  }

  // Update position and life, then draw
  void draw() {
    // Update position based upon velocity
    this.pos.add(this.vel);

    // Draw particle
    if (img == null) {
      fill(255,255,255, floor(255 * (life / totalLife)));
      ellipse(this.pos.x, this.pos.y, 10, 10);
    }
    else {
      // Why is rotation so annoying?
      pushMatrix();
      translate(pos.x, pos.y);
      rotate(radians(rotation));
      tint(255, floor(255 * (life / totalLife)));
      image(img, img.width/2, img.height/2);
      popMatrix();
      noTint();

      if (rotation >= 120)
        rotateReverse = true;
      if (rotation <= 60)
        rotateReverse = false;
        
      double amount = 60 * game.deltaTime;
      rotation += rotateReverse ? -amount : amount;
    }

    // Update life
    life -= game.deltaTime;
  }
}
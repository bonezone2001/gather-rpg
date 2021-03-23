// Since I already kinda have timing with the implementation of delta time
// I figured I'd just make a very simple timer that increments with deltaTime

class Timer {
  // Time in seconds
  private float ellapsed = 0;   // Past
  private float limit = 60;     // Stop at ( 60 seconds )

  // Things that actually matter
  void update() {
    increment((float)game.deltaTime);
  }

  void reset() {
    setEllapsed(0);
  }


  // Getters
  float getEllapsed() {
    return this.ellapsed;
  }

  float getRemaining() {
    return limit - ellapsed;
  }

  boolean hasFinished() {
    return ellapsed >= limit;
  }


  // Setters / mutation
  void increment(float amount) {
    ellapsed += amount;
  }

  void decrement(float amount) {
    ellapsed -= amount;
  }

  void setEllapsed(float time) {
    ellapsed = time;
  }

  void setLimit(float time) {
    limit = time;
  }
}
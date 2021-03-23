// This is it's own class so I can extend camera functionality
// at some point, right now it just follows an entity provided

// Extends from base entity because camera MAY move independant
class Camera extends BaseEntity {
  BaseEntity follow;

  Camera(BaseEntity toFollow) {
    setFollow(toFollow);
  }

  // This is seperated out for future expansion
  void setFollow(BaseEntity toFollow){
    if (toFollow == null) return;
    follow = toFollow;
  }

  public void doTranslate() {
    translate(-int(follow.pos.x-game.center.x), -int(follow.pos.y-game.center.y));
  }

  public void undoTranslate() {
    translate(int(follow.pos.x-game.center.x), int(follow.pos.y-game.center.y));
  }
}

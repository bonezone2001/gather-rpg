// Made static originally but found many limitations with that approach

import java.io.*;
import java.util.Arrays;

// Convience class for storing rectangles
class Rect {
  PVector pos;
  PVector size;

  Rect() {
    pos = new PVector(0, 0);
    size = new PVector(0, 0);
  }

  Rect(float x, float y, float w, float h) {
    pos = new PVector(x, y);
    size = new PVector(w, h);
  }

  Rect(PVector pos_, PVector size_) {
    pos = pos_;
    size = size_;
  }
}

// Class for storing the result of projection collision
class CollisionResult {
  PVector pointOfContact;
  PVector contactNormal;
  float timeHit;
}

class Utils {
  public boolean hoveredRect(float x, float y, float width_, float height_)  {
    if (mouseX >= x && mouseX <= x + width_ && mouseY >= y && mouseY <= y + height_)
      return true;
    return false;
  }

  public boolean hoveredRect(PVector pos, float width_, float height_)  {
    return hoveredRect(pos.x, pos.y, width_, height_);
  }

  public boolean hoveredCircle(float x, float y, float diameter) {
    float disX = x - mouseX;
    float disY = y - mouseY;
    if (sqrt(sq(disX) + sq(disY)) < diameter/2 ) 
      return true;
    return false;
  }

  public boolean hoveredRect(PVector pos, float diameter)  {
    return hoveredCircle(pos.x, pos.y, diameter);
  }

  // Load and filter folder contents
  public String[] filesInFolder(String folderStr, FilenameFilter filter) {
    File folder = new File(dataPath(folderStr));
    String[] files;
    if (filter != null)
      files = folder.list(filter);
    else
      files = folder.list();
    
    // Return sorted files, needs to be done for linux (tested on laptop)
    // .list() returns differently on linux
    return sort(files);
  }

  // Load folder contents without a filter
  public String[] filesInFolder(String folderStr) {
    return filesInFolder(folderStr, null);
  }

  // Load sprite or spriteS based upon the if its folder and the filter
  public Sprite[] loadImages(FilenameFilter filter, String fileOrFolder, boolean isFolder) {
    // Load images
    if (isFolder) {
      // Check if user provided "path/" or "path" and adapt path accordingly
      String addForwardSlash = fileOrFolder.substring(fileOrFolder.length() - 1) == "/" ? "" : "/";

      // Load and filter folder contents
      if (filter == null)
        filter = new FilenameFilter() {
          public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".png");
          }
        };
      String[] files = filesInFolder(fileOrFolder, filter);
      
      // Load sprites
      Sprite[] sprites = new Sprite[files.length];
      for (int i = 0; i < files.length; i++)
        sprites[i] = new Sprite(fileOrFolder + addForwardSlash + files[i]);
      return sprites;
    }
    // Load image (singular)
    return new Sprite[] { new Sprite(fileOrFolder) };
  }
  
  // Just so I don't have to change my existing code
  public Sprite[] loadImages(String fileOrFolder, boolean isFolder) {
    return loadImages(null, fileOrFolder, isFolder);
  }

  // Allow for specifying prefix filter (useful to have, however not used)
  public Sprite[] loadImages(String fileOrFolder, boolean isFolder, final String prefix) {
    FilenameFilter filter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        String lowerName = name.toLowerCase();
        return lowerName.endsWith(".png") && lowerName.startsWith(prefix);
      }
    };
    return loadImages(filter, fileOrFolder, isFolder);
  }

  // Convert mouse position to world coordinates
  public PVector translateMouse(PVector pos) {
    float newMouseX = mouseX - pos.x-game.center.x;
    float newMouseY = mouseY - pos.y-game.center.y;
    return new PVector(newMouseX, newMouseY);
  }

  // Convert mouse position to world coordinates (more efficent for multiple calls)
  public void translateMouse(PVector pos, PVector out) {
    out.x = mouseX + pos.x-game.center.x;
    out.y = mouseY + pos.y-game.center.y;
  }

  // Check if the screen rectangle overlaps point
  public boolean onScreen(float x, float y, float w, float h) {
    PVector cameraPos = game.gameplay.camera.follow.pos;
    float screenX = cameraPos.x - game.center.x;
    float screenY = cameraPos.y - game.center.y;

    if (x < (screenX + width) && (x+w) > screenX && (y+h) > screenY && y < (screenY+height))
      return true;
    return false;
  }
  
  public boolean isInRect(PVector playerPos, PVector pos, PVector size)  {
    if (playerPos.x >= pos.x && playerPos.x <= pos.x + size.x && playerPos.y >= pos.y && playerPos.y <= pos.y + size.y)
      return true;
    return false;
  }

  // Serialize (really ghetto but processing keeps saying that the <project name> is not serializable)
  // whenever I try and use an actual custom made class...
  public boolean saveObject(Object obj, String path) {
    try {
      FileOutputStream outStream = new FileOutputStream(dataPath(path));
      ObjectOutputStream out = new ObjectOutputStream(outStream);
      out.writeObject(obj);
      out.close();
      outStream.close();
    } catch (Exception ex) {
      println(ex);
      return false;
    }
    return true;
  }

  public Object loadObject(String path) {
    try {
      FileInputStream fileIn = new FileInputStream(dataPath(path));
      ObjectInputStream objectIn = new ObjectInputStream(fileIn);
      Object obj = objectIn.readObject();
      objectIn.close();
      return obj;
    } catch (Exception ex) {
      println(ex);
      return null;
    }
  }

  // Projection/ray based collision (most accurate and robust with a cost to performance)
  public CollisionResult RayCollisionPoint(PVector rayOrigin, PVector rayDir, Rect target) {
    // Invert becos faster
    PVector inverseDir = new PVector(1, 1);
    inverseDir.x /= rayDir.x;
    inverseDir.y /= rayDir.y;

    // Intersections
    PVector near = PVector.sub(target.pos, rayOrigin);
    near.x *= inverseDir.x;
    near.y *= inverseDir.y;
    PVector far = PVector.sub(PVector.add(target.pos, target.size), rayOrigin);
    far.x *= inverseDir.x;
    far.y *= inverseDir.y;


    // Check for NaN
    if (Float.isNaN(near.x) || Float.isNaN(near.y) || Float.isNaN(far.x) || Float.isNaN(far.y))
      return null;

    // Fix distances
    if (near.x > far.x) {
      float newFar = near.x;
      near.x = far.x;
      far.x = newFar;
    }
    if (near.y > far.y) {
      float newFar = near.y;
      near.y = far.y;
      far.y = newFar;
    }

    // Early call
    if (near.x > far.y || near.y > far.x)
      return null;

    // Furthest intersection time
    float tHitFar = Math.min(far.x, far.y);

    // Check direction correct
    if (tHitFar < 0)
      return null;

    // Result obj
    CollisionResult result = new CollisionResult();
    result.timeHit = Math.max(near.x, near.y);    // Closest intersection time
    result.pointOfContact = PVector.add(          // Contact point
      rayOrigin,
      PVector.mult(rayDir, result.timeHit)
    );

    // Get normals
    result.contactNormal = new PVector(0, 0);
    if (near.x > near.y)
      if (inverseDir.x < 0)
        result.contactNormal.set(1, 0);
      else
        result.contactNormal.set(-1, 0); 
    else
      if (inverseDir.y < 0)
        result.contactNormal.set(0, 1);
      else
        result.contactNormal.set(0, -1);

    // Contact point of collision from parametric line equation
    return result;
  }

  public CollisionResult RayCollisionRect(PVector rayOrigin, PVector size, PVector rayDir, Rect target) {
    // Check we're even moving
    if (rayDir.x == 0 && rayDir.y == 0)
      return null;

    // Expand target by src
    Rect expanded = new Rect();
    expanded.pos = PVector.sub(target.pos, PVector.div(size, 2));
    expanded.size = PVector.add(target.size, size);

    // Check if collision, return result
    CollisionResult result = RayCollisionPoint(rayOrigin, rayDir, expanded);
    if (result != null && result.timeHit >= 0 && result.timeHit < 1.0f)
      return result;
    return null;
  }

  // Also resolve the collision, don't just check
  public CollisionResult RayCollisionRectRes(PVector rayOrigin, PVector size, PVector rayDir, Rect target) {
    CollisionResult result = RayCollisionRect(rayOrigin, size, rayDir, target);

    // Attempt to resolve the collision by changing rayDir (vel)
    if (result != null) {
      PVector newVel = PVector.mult(new PVector(abs(rayDir.x), abs(rayDir.y)), (1-result.timeHit));
      newVel.x *= result.contactNormal.x;
      newVel.y *= result.contactNormal.y;
      rayDir.add(newVel);
    }
    return result;
  }

  // Get distance between two points using pythag
  public float distanceTo(float aX, float aY, float bX, float bY) {
    return (float)Math.sqrt((aY - bY) * (aY - bY) + (aX - bX) * (aX - bX));
  }
  
  public float distanceTo(PVector a, PVector b) {
    return distanceTo(a.x, a.y, b.x, b.y);
  }
}
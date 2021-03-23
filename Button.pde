// Not my proudest class (it needs serious refactoring) but processing is also a tad limited/annoying
// should probably inherit from base entity for consistency...
// the font will not center correctly, it's not my fault :(

// Note: If this wasn't for an assignment I'd just use G4P


class Button {
  String text;
  int fontSize;
  PVector pos, size;
  PFont font;
  color textColor, baseColor, highlightColor;
  boolean wasHovered;
  boolean justHovered;

  // PVector uses float so I assume it supports sub pixel
  Button(float x, float y, float w, float h, String text_, int textSize) {
    // I don't know if it get's cached, I shouldn't assume and cache it myself but it'll do for current game
    font = createFont("Nexa-L.ttf", 32);

    wasHovered = false;
    justHovered = false;
    
    pos = new PVector(x, y);
    fontSize = textSize;
    size = new PVector(w, h);
    text = text_;
    setupColors(color(255,255,255), color(25,25,25), color(100,100,100));
  }
  
  // PVector uses float so I assume it supports sub pixel
  Button(float x, float y, float w, float h, String text, int textSize, color textCol, color baseCol, color highlightCol) {
    this(x, y, w, h, text, textSize);
    setupColors(textCol, baseCol, highlightCol);
  }

  // Allow for changing the colours later
  void setupColors(color text, color base, color highlight) {
    textColor = text;
    baseColor = base;
    highlightColor = highlight;
  }

  boolean isHovered() {
    return game.utils.hoveredRect(pos.x-(size.x/2), pos.y-(size.y/2), size.x, size.y);
  }

  boolean justHovered() {
    return justHovered;
  }

  boolean isPressed() {
    return isHovered() && game.input.getMouse(LEFT).framePressed;
  }

  // I hate the fact that all the changes to fontSize and fill, etc
  // they all effect globally...
  void draw() {
    textFont(font);
    textSize(fontSize);
    textAlign(CENTER, CENTER);
    
    // Set frame hover state
    boolean hovered = isHovered();
    if (!wasHovered && hovered)
    {
      wasHovered = true;
      justHovered = true;
    }
    else if (wasHovered && !hovered)
      wasHovered = false;
    else
      justHovered = false;

    // Render background (change based on hover)
    strokeWeight(2);
    stroke(hovered ? baseColor : highlightColor);
    fill(hovered ? highlightColor : baseColor);
    rect(pos.x-(size.x/2), pos.y-(size.y/2), size.x, size.y);

    // Current don't allow changing of the button text colour on hover
    fill(textColor);

    // fontSize/4 to get it closer to center (processing's fault...)
    text(text, pos.x, pos.y-(fontSize/4));
  }
}
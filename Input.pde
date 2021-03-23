// Input handler since the built in one is not as reliable or smooth as I'd like
// it's essentially a sort of loose replica of the one I have implemented in my C++ game engine
// it's okay, not production level code but it does it's job (it also doesn't clean up)

import java.util.Map;
import java.nio.charset.StandardCharsets;
import javax.swing.KeyStroke;

public class InputState
{
  public boolean framePressed;  // Was pressed this frame
  public boolean frameReleased; // Was released this frame
  public boolean held;          // Currently being held / pressed
  
  public InputState()
  { setState(false, false, false); }

  public InputState(boolean pressed)
  { setState(pressed, pressed, false); }

  public void setState(boolean _pressed, boolean _held, boolean _released)
  {
    framePressed = _pressed;
    held = _held;
    frameReleased = _released;
  }
}

class Input
{
  // All input states
  private int mouseScroll = 0;
  private HashMap<Integer, InputState> _keyStates = new HashMap<Integer, InputState>();
  private HashMap<Integer, InputState> _mouseStates = new HashMap<Integer, InputState>();
  private ArrayList<InputState> releasePressed = new ArrayList<InputState>();

  // For optimisation purposes
  boolean keyChanged = false;
  boolean mouseChanged = false;

  // Getters
  // --------------------------------
  // Get key state or add if doesn't exist
  public InputState getKey(int keyCode_)
  {
    InputState state = _keyStates.get(keyCode_);
    if (state == null) {
      state = new InputState();
      _keyStates.put(keyCode_, new InputState());
    }
    return state;
  }

  // Wrapper for using characters
  public InputState getKey(char key_)
  {
    KeyStroke keyStroke = KeyStroke.getKeyStroke(Character.toUpperCase(key_), 0);
    InputState state = getKey(keyStroke.getKeyCode());
    return state;
  }

  // Wrapper for using characters
  public InputState[] getKeys(char[] keys_)
  {
    if (keys_.length == 0) return null;
    InputState[] states = new InputState[keys_.length];
    for (int i = 0; i < keys_.length; i++)
      states[i] = getKey(keys_[i]);
    return states;
  }

  // Get key state or add if doesn't exist
  public InputState getMouse(int mouseNum_)
  {
    InputState state = _mouseStates.get(mouseNum_);
    if (state == null) {
      state = new InputState();
      _mouseStates.put(mouseNum_, new InputState());
    }
    return state;
  }

  public int getScroll() {
    return mouseScroll;
  }

  
  // Events
  // --------------------------------
  // Key pressed current frame
  public void keyPress()
  {
    InputState current = getKey(keyCode);
    if (current.held) return;
    keyChanged = true;
    current.setState(true, true, false);
    releasePressed.add(current);
    
    // Prevent escape key from closing game
    if (key == ESC) {
      key = 0;
    }
  }
  
  // Key released current frame
  public void keyRelease()
  {
    keyChanged = true;
    InputState current = getKey(keyCode);
    current.setState(false, false, true);
    releasePressed.add(current);
  }
  
  // Mouse pressed current frame
  public void mousePress()
  {
    InputState current = getMouse(mouseButton);
    if (current.held) return;
    mouseChanged = true;
    current.setState(true, true, false);
    releasePressed.add(current);
  }
  
  // Mouse released current frame
  public void mouseRelease()
  {
    mouseChanged = true;
    InputState current = getMouse(mouseButton);
    current.setState(false, false, true);
    releasePressed.add(current);
  }

  public void mouseScroll(MouseEvent event) {
    mouseChanged = true;
    mouseScroll = event.getCount();
  }

  // Reset back to empty state
  public void resetFrame()
  {
    keyChanged = false;
    mouseChanged = false;
    if (mouseScroll != 0) mouseScroll = 0;
    if (releasePressed.size() == 0) return;
    for (int i = 0; i < releasePressed.size(); i++) {
      InputState state = releasePressed.remove(0);
      state.setState(false, state.held, false);
    }
  }
}
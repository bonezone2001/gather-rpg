import java.io.File;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.FloatControl;


// My sound player. Origianlly used the processing sound library but then figured it's better ifI didn't use
// a library and instead created my own since it's not that hard in Java. I'm just using wav files.
// Uses C style erroring. If you wish to get an error from the system, use the getLastError() method.
// Volume control is a bit annoying because it doesn't affect the current buffer and it's a pain to change
// the buffer size to account for the time taken to exhaust the buffer. It's a bit botched but it's alright

// States of sound class
enum SoundState {
    PAUSED,
    STOPPED,
    PLAYING,
    LOOPING
}

class Sound {
    long curPos;                    // Current frame in sound - for pausing
    Clip clip;                      // Audio clip
    String filePath;                // Path to file
    SoundState state;               // Current state of the player
    AudioInputStream audioStream;   // Current audio stream
    
    // Some nice things to have
    float volume = 1.0f;                   // Current volume
    float desiredVolume = 1.0f;            // Volume to fade to

    // C style erroring because I'm lazy
    Exception lastError;            // Last error from the system
  
    // Setup a sound instead with reference to a file
    public Sound(String file) {
        // Attempt to create audio stream and setup the clip
        try {
            state = SoundState.STOPPED;
            filePath = dataPath(file);      // Set path
            clip = AudioSystem.getClip();   // Create a clip
            createStream();                 // Create stream and open clip
        }
        // Requirement since AudioSystem throws
        catch (Exception e) {
            lastError = e;
        }
    }

    // If we're paused, run this code before playing or looping
    private boolean preResume() {
        try {
            if (state == SoundState.PAUSED) {
                clip.close();
                createStream();
                clip.setMicrosecondPosition(curPos);
            }
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }
    
    // Attemp to play the audio without looping (or resume)
    public boolean play() {
        try {
            // If we're currently playing, seek back to the start
            if (state == SoundState.PLAYING)
                clip.setFramePosition(0);

            preResume();
            clip.start();
            state = SoundState.PLAYING;
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }

    // Attemp to loop audio (or resume)
    public boolean loop() {
        if (state == SoundState.LOOPING) return false;

        try {
            preResume();
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            state = SoundState.LOOPING;
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }

    // Method to pause the audio 
    public boolean pause() {
        if (state == SoundState.PAUSED) return false;

        try {
            curPos = clip.getMicrosecondPosition();
            clip.stop();
            state = SoundState.PAUSED;
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }

    // Reset without autoplaying
    public boolean reset() {
        // Stop clip and close stream
        if (!stop())
            return false;

        // Recreate the stream
        return createStream();
    }

    // Reset with autoplaying 
    public boolean restart() {
        try {
            // Reset everything
            reset();

            // Start playing again
            this.play();
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }
      
    // Method to stop the audio 
    public boolean stop() {
        try {
            state = SoundState.STOPPED;
            curPos = 0;
            clip.stop();
            clip.close();
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }

    // Set the gain of the audio c
    boolean setVol(float gain) {
        try {
            // The gain controller for the clip
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            
            // Convert the percentage to dB (I square the percentage to make it more "logarithmic")
            // If you asked me how this conversion works though, I have no idea
            float dB = (float) (Math.log(Math.pow(gain, 2)) / Math.log(10.0) * 20.0);
            
            // Update the volume
            volume = gain;
            gainControl.setValue(dB);
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }

    // Set desired volume for fading
    void setDesiredVol(float gain) {
        desiredVolume = gain;
    }

    // Check if we're playing
    boolean isPlaying() {
        if (state == SoundState.PAUSED || state == SoundState.STOPPED)
            return false;
        return true;
    }

    // Method to jump over a specific part 
    public boolean seek(long pos) {
        try {
            if (pos < 0 || pos > clip.getMicrosecondLength()) 
                throw new Exception("position out of bounds");

            curPos = pos;
            clip.setMicrosecondPosition(pos);
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }

    // Method to create audio stream
    public boolean createStream()
    {
        try {
            audioStream = AudioSystem.getAudioInputStream(new File(filePath));
            clip.open(audioStream);
        } catch (Exception e) {
            lastError = e;
            return false;
        }
        return true;
    }
}
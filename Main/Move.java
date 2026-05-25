package Main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Move implements KeyListener {

    // Held keys (movement)
    public boolean up, down, left, right;

    // One-shot keys – reset by GamePanel after consuming
    public boolean interactPressed;   // E
    public boolean attackPressed;     // Space / F
    public boolean escPressed;        // ESC
    public boolean enterPressed;      // Enter
    public boolean restartPressed;    // R
    public boolean qPressed;          // Q  (pause → title)
    public boolean iPressed;          // I  (inventory toggle)
    public boolean mPressed;          // M  (minimap toggle)

    @Override public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W, KeyEvent.VK_UP    -> up    = true;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN  -> down  = true;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT  -> left  = true;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> right = true;
            case KeyEvent.VK_E                    -> interactPressed = true;
            case KeyEvent.VK_SPACE, KeyEvent.VK_F -> attackPressed   = true;
            case KeyEvent.VK_ESCAPE               -> escPressed      = true;
            case KeyEvent.VK_ENTER                -> enterPressed    = true;
            case KeyEvent.VK_R                    -> restartPressed  = true;
            case KeyEvent.VK_Q                    -> qPressed        = true;
            case KeyEvent.VK_I                    -> iPressed        = true;
            case KeyEvent.VK_M                    -> mPressed        = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W, KeyEvent.VK_UP    -> up    = false;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN  -> down  = false;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT  -> left  = false;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> right = false;
        }
    }
}

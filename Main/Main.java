package Main;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        JFrame widow = new JFrame();
        widow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        widow.setResizable(false);
        widow.setTitle("Game of NVTS");
        widow.setVisible(true);
        widow.setResizable(true); // <-- Cho phép resize cửa sổ
        GamePanel gamePanel = new GamePanel();
        widow.add(gamePanel);
        widow.pack();
        widow.setLocationRelativeTo(null);
        gamePanel.startGameThread();
        gamePanel.requestFocusInWindow();
    }
}

package tiles;

import Main.GamePanel;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TilesManager {
    GamePanel gamePanel;
    Tiles[] tiles;
    int mapTile[][];

    public TilesManager(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        mapTile = new int[gamePanel.maxWorldRow][gamePanel.maxWorldCol];
        tiles = new Tiles[38]; // Assuming a maximum of 10 different tiles
        getTileImage();
        loadMap("/map/test.txt"); // Load the map from a text file
    }

    public void loadMap(String path) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)));
            for (int row = 0; row < gamePanel.maxWorldRow; row++) {
                String line = br.readLine();
                String[] numbers = line.split(" ");
                for (int col = 0; col < gamePanel.maxWorldCol; col++) {
                    mapTile[row][col] = Integer.parseInt(numbers[col]);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getTileImage() {
        try {
            for (int i = 1; i <= 37; i++) {
                tiles[i] = new Tiles();
                tiles[i].image = ImageIO.read(getClass().getResourceAsStream("/tiles/" + String.format("%03d", i) + ".png"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {
        final int tile = gamePanel.tileSize;

        // Giới hạn tile hiển thị theo camera + màn hình
        int firstCol = Math.max(0, gamePanel.cameraX / tile);
        int firstRow = Math.max(0, gamePanel.cameraY / tile);

        int lastCol = Math.min(gamePanel.maxWorldCol - 1,
                (gamePanel.cameraX + gamePanel.width - 1) / tile);
        int lastRow = Math.min(gamePanel.maxWorldRow - 1,
                (gamePanel.cameraY + gamePanel.depth - 1) / tile);

        for (int row = firstRow; row <= lastRow; row++) {
            for (int col = firstCol; col <= lastCol; col++) {
                int tileIndex = mapTile[row][col];
                if (tileIndex < 0 || tileIndex >= tiles.length || tiles[tileIndex] == null)
                    continue;

                // WORLD -> SCREEN
                int worldX = col * tile;
                int worldY = row * tile;
                int screenX = worldX - gamePanel.cameraX;
                int screenY = worldY - gamePanel.cameraY;

                g2.drawImage(tiles[tileIndex].image, screenX, screenY, tile, tile, null);
            }
        }
    }

}

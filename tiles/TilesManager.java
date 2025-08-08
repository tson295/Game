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
        mapTile = new int[gamePanel.maxRowPixel][gamePanel.maxColPixel];
        tiles = new Tiles[38]; // Assuming a maximum of 10 different tiles
        getTileImage();
        loadMap("/map/test.txt"); // Load the map from a text file
    }

    public void loadMap(String path) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)));
            for (int row = 0; row < gamePanel.maxRowPixel; row++) {
                String line = br.readLine();
                String[] numbers = line.split(" ");
                for (int col = 0; col < gamePanel.maxColPixel; col++) {
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
        for (int row = 0; row < gamePanel.maxRowPixel; row++) {
            for (int col = 0; col < gamePanel.maxColPixel; col++) {
                int tileIndex = mapTile[row][col];
                int x = col * gamePanel.realPixel;
                int y = row * gamePanel.realPixel;

                if (tileIndex >= 0 && tileIndex < tiles.length) {
                    g2.drawImage(tiles[tileIndex].image, x, y, gamePanel.realPixel, gamePanel.realPixel, null);
                }
            }
        }
    }

}

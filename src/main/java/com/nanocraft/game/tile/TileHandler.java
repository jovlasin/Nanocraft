package com.nanocraft.game.tile;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;
import com.nanocraft.game.core.GameHandler;

public class TileHandler {
    private GameHandler gh;
    public Tile[] tile;
    public int tileNum[][];

    public TileHandler(GameHandler gh) {
        this.gh = gh;
        tile = new Tile[10];
        tileNum = new int[75][75]; // placeholder values
        getImage();
        loadMap("/map/mapv0.csv");
    }

    public void getImage() {
        setup(0, "000", true);
        setup(1, "001", false);
        setup(2, "002", false);
        setup(3, "003", false);
        setup(4, "004", false);
        setup(5, "005", true);
        setup(6, "006", true);
    }

    private void setup(int index, String name, boolean collision) {
        try {
            tile[index] = new Tile();
            tile[index].image = ImageIO.read(getClass().getResourceAsStream("/tile/" + name + ".png"));
            tile[index].image = scaleImage(tile[index].image, gh.tileSize, gh.tileSize); // placeholder tilesize values for now
            tile[index].collision = collision;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage scaleImage(BufferedImage original, int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width, height, original.getType());
        Graphics2D g2 = scaledImage.createGraphics();
        g2.drawImage(original, 0, 0, width, height, null);
        g2.dispose();
        
        return scaledImage;
    }

    private void loadMap(String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            for (int row = 0; row < 75; row++) { // 75 is a placeholder number of rows for now
                String line = br.readLine();
                String[] num = line.split(",");

                for (int col = 0; col < 75; col++) { // 75 is a placeholder number of cols for now
                    tileNum[col][row] = Integer.parseInt(num[col]);
                }
            }
            br.close();
        } catch (IOException e) {        
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {
        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < 75 && worldRow < 75) {
            int num = tileNum[worldCol][worldRow];
            int worldX = worldCol * 48;
            int worldY = worldRow * 48;
            int screenX = worldX - gh.player.worldX + gh.player.screenX;
            int screenY = worldY - gh.player.worldY + gh.player.screenY;

            if (worldX + 48 > gh.player.worldX - gh.player.screenX && 
                worldX - 48 < gh.player.worldX + gh.player.screenX && 
                worldY + 48 > gh.player.worldY - gh.player.screenY && 
                worldY - 48 < gh.player.worldY + gh.player.screenY) {

                g2.drawImage(tile[num].image, screenX, screenY, null);
            }
            worldCol++;

            if (worldCol == 75) {
                worldCol = 0;
                worldRow++;
            }
        }
    }
    
}

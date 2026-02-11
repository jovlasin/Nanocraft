package map;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import main.GameHandler;

public class TileHandler {
    private GameHandler gh;
    public Tile[] tile;
    public int tileNum[][];

    public TileHandler(GameHandler gh) {
        this.gh = gh;
        tile = new Tile[10];
        tileNum = new int[][];
        getImage();
    }

    public void getImage() {

    }

    private void setup(int index, String name, boolean collision) {
        try {
            tile[index] = new Tile();
            tile[index].image = ImageIO.read(getClass().getResourceAsStream("/tiles/" + name + ".png"));
            tile[index].image = scaleImage(tile[index].image, gh.tileSize, gh.tileSize);
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
    
}

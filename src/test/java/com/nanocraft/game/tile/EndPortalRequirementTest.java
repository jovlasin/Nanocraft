package com.nanocraft.game.tile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.nanocraft.game.core.GameHandler;
import com.nanocraft.game.object.EyeOfEnder;

public class EndPortalRequirementTest {
    @Test
    public void desertPortalRequiresEyeOfEnder() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/desert.tmj");

        MapTransition endTransition = findEndTransition();
        positionPlayerOnTransition(gh, endTransition);

        gh.th.checkMapTransition();

        assertEquals("/map/desert.tmj", gh.th.getCurrentMapPath());
        assertTrue(gh.ui.message.contains("Need Eye of Ender to enter the End!"));
    }

    @Test
    public void desertPortalAllowsEntryWhenEyeOfEnderIsInInventory() {
        GameHandler gh = new GameHandler();
        gh.th.loadMap("/map/desert.tmj");
        assertTrue(gh.player.addToInventory(new EyeOfEnder(gh)));

        MapTransition endTransition = findEndTransition();
        positionPlayerOnTransition(gh, endTransition);

        gh.th.checkMapTransition();

        assertEquals("/map/end.tmj", gh.th.getCurrentMapPath());
        assertFalse(gh.player.hasItem("eye_of_ender"));
    }

    private MapTransition findEndTransition() {
        MapLoader loader = new MapLoader(48);
        MapLoader.MapData mapData = loader.loadMap("/map/desert.tmj");

        for (int col = 0; col < mapData.mapWidth; col++) {
            for (int row = 0; row < mapData.mapHeight; row++) {
                for (int[][] layer : mapData.layers) {
                    int tileId = layer[col][row];
                    if (mapData.zeroMeansEmpty && tileId == 0) {
                        continue;
                    }

                    Tile tile = mapData.tileRegistry.get(tileId);
                    if (tile == null || "/map/end.tmj".equals(tile.targetMapPath) == false) {
                        continue;
                    }

                    return new MapTransition(
                        col,
                        row,
                        1,
                        1,
                        tile.targetMapPath,
                        tile.targetCol,
                        tile.targetRow,
                        tile.targetDirection == null ? "down" : tile.targetDirection
                    );
                }
            }
        }

        return null;
    }

    private void positionPlayerOnTransition(GameHandler gh, MapTransition transition) {
        assertNotNull(transition);

        gh.player.worldX = transition.sourceCol * gh.tileSize;
        gh.player.worldY = transition.sourceRow * gh.tileSize;
        gh.player.direction = "down";
    }
}

package com.nanocraft.game.tile;

import java.util.List;

import com.google.gson.JsonElement;

class TiledMapData {
    int width;
    int height;
    List<TiledLayerData> layers;
    List<TiledTilesetData> tilesets;
}

class TiledLayerData {
    String name;
    String type;
    JsonElement data;
    String encoding;
    String compression;
}

class TiledTilesetData {
    int firstgid;
    String source;
    String image;
    int imagewidth;
    int imageheight;
    int tilewidth;
    int tileheight;
    int tilecount;
    int columns;
    List<TiledTileData> tiles;
}

class TiledTileData {
    int id;
    String image;
    List<TiledPropertyData> properties;
}

class TiledPropertyData {
    String name;
    Object value;
}

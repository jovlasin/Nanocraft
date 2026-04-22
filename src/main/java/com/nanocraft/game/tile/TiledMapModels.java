package com.nanocraft.game.tile;

import java.util.List;

import com.google.gson.JsonElement;

class TiledMapData {
    int width;
    int height;
    int tilewidth;
    int tileheight;
    List<TiledLayerData> layers;
    List<TiledTilesetData> tilesets;
}

class TiledLayerData {
    String name;
    String type;
    JsonElement data;
    String encoding;
    String compression;
    List<TiledObjectData> objects;
}

class TiledTilesetData {
    int firstgid;
    String name;
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

class TiledObjectData {
    int id;
    String name;
    String type;
    Integer gid;
    double x;
    double y;
    double width;
    double height;
    List<TiledPropertyData> properties;
}

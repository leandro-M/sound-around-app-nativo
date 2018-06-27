package com.example.leandro.soundaroundapp;

public class AppConfig {
    private static String URL = "192.168.0.106";

    // Server user login url
    public static String URL_LOGIN = "http://" + URL + "/sound-around-api/login";

    // Server user register url
    public static String URL_REGISTER = "http://" + URL + "/sound-around-api/save/user";

    // Server user register url
    public static String URL_GETUSERINFO = "http://" + URL + "/sound-around-api/user/by/";

    // Server user albums url
    public static String URL_ALBUMS = "http://" + URL + "/sound-around-api/users/albums/";

    // Server user albums url edit
    public static String URL_ALBUMS_VIEW = "http://" + URL + "/sound-around-api/album/";

    // Server user albums url edit
    public static String URL_ALBUMS_SAVE = "http://" + URL + "/sound-around-api/save/album";

    // Server user albums url
    public static String URL_DELETE_ALBUM = "http://" + URL + "/sound-around-api/albums/deleteAlbum/?id=";

    // Server user albums url
    public static String URL_MUSICS = "http://" + URL + "/sound-around-api/music/by/";

    // Server user albums url
    public static String URL_DELETE_MUSIC = "http://" + URL + "/sound-around-api/sounds/deleteMusic/?id=";

    // Server user albums url edit
    public static String URL_MUSIC_VIEW = "http://" + URL + "/sound-around-api/music/";

    // Server user albums url edit
    public static String URL_MUSIC_SAVE = "http://" + URL + "/sound-around-api/save/music";
}

package com.example.leandro.soundaroundapp;

public class AppConfig {
    // Server user login url
    public static String URL_LOGIN = "http://192.168.0.106/sound-around-api/login";

    // Server user register url
    public static String URL_REGISTER = "http://192.168.0.106/sound-around-api/save/user";

    // Server user register url
    public static String URL_GETUSERINFO = "http://192.168.0.106/sound-around-api/user/by/";

    // Server user albums url
    public static String URL_ALBUMS = "http://192.168.0.106/sound-around-api/users/albums/";

    // Server user albums url edit
    public static String URL_ALBUMS_VIEW = "http://192.168.0.106/sound-around-api/album/";

    // Server user albums url
    public static String URL_DELETE_ALBUM = "http://192.168.0.106/sound-around-api/albums/deleteAlbum/?id=";

}

package com.example.aryamirshafii.nilereverb;

import android.content.ContentUris;
import android.net.Uri;

public class Song  {
    private long id;
    private String artist;
    private String title;
    private String album;
    private long duration;
    public Song(long id, String artist, String title, String album, long duration) {
        this.id = id;
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.duration = duration;
    }
    public long getId() {
        return id;
    }
    public String getArtist() {
        return artist;
    }
    public String getTitle() {
        return title;
    }
    public String getAlbum() {
        return album;
    }
    public long getDuration() {
        return duration;
    }
    public Uri getURI() {
        return ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
    }

    @Override
    public String toString(){
        return title;
    }
}


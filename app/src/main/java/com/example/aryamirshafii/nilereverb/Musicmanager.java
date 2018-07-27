package com.example.aryamirshafii.nilereverb;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.regex.Pattern;

public class Musicmanager implements AudioManager.OnAudioFocusChangeListener{
    final String TAG = "Musicmanager";
    private ContentResolver mContentResolver;

    private List<Song> mySongs = new ArrayList<Song>();

    private Context mContext;

    private stringSearch stringSearcher;
    private ArrayList<String> genres;
    private ArrayList<String> artists;
    private HashMap<String, Song[]> artistMap;
    private HashMap<String, Song[]> albumMap;

    private MediaPlayer mediaPlayer;

    private String currentPlayingSong;

    private DoublyLinkedList<Song> songLinkedList;

    private Boolean isShuffling = false;

    private AudioManager audioManager;








    private HashMap<String, Song[]> genreMap;
    public Musicmanager(ContentResolver cr, Context context) {
        this.mContentResolver = cr;
        this.mContext = context;
        stringSearcher = new stringSearch();
        genres = new ArrayList<>();
        artists = new ArrayList<>();
        artistMap = new HashMap<>();
        genreMap = new HashMap<>();
        mediaPlayer = new MediaPlayer();
        albumMap = new HashMap<>();
        songLinkedList = new DoublyLinkedList<>();
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        //Checking if music is already playing. If so the app will request focus.
        if(audioManager.isMusicActive()) {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);


        }
        audioManager.startBluetoothSco();



    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mediaPlayer.start();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                mediaPlayer.pause();// Pause your media player here
                break;
        }
    }




    /**
     * Loads music data. This method may take long, so be sure to call it asynchronously without
     * blocking the main thread.
     */
    public void prepare() {
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Log.i(TAG, "Querying media...");
        Log.i(TAG, "URI: " + uri.toString());
        // Perform a query on the content resolver. The URI we're passing specifies that we
        // want to query for all audio media on external storage (e.g. SD card)
        Cursor cur = mContentResolver.query(uri, null,
                MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);
        Log.i(TAG, "Query finished. " + (cur == null ? "Returned NULL." : "Returned a cursor."));
        if (cur == null) {
            // Query failed...
            Log.e(TAG, "Failed to retrieve music: cursor is null :-(");
            return;
        }
        if (!cur.moveToFirst()) {
            // Nothing to query. There is no music on the device. How boring.
            Log.e(TAG, "Failed to move cursor to first row (no query results).");
            return;
        }
        Log.i(TAG, "Listing...");
        // retrieve the indices of the columns where the ID, title, etc. of the song are
        int artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION);
        int idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID);

        int genreColumn = cur.getColumnIndex(MediaStore.Audio.Genres.NAME);

        //Log.i(TAG, "Title column index: " + String.valueOf(titleColumn));
        //Log.i(TAG, "ID column index: " + String.valueOf(titleColumn));
        // add each song to mItems


        String currentArtist = cur.getString(artistColumn);
        ArrayList<Song> artistSongs = new ArrayList<>();

        String currentAlbum = cur.getString(albumColumn);
        ArrayList<Song> albumSongs = new ArrayList<>();



        do {
            //Log.i(TAG, "ID: " + cur.getString(idColumn) + " Title: " + cur.getString(titleColumn));
            Song newSong = new Song(
                    cur.getLong(idColumn),
                    cur.getString(artistColumn),
                    cur.getString(titleColumn),
                    cur.getString(albumColumn),
                    cur.getLong(durationColumn));

            mySongs.add(newSong);







            if(!currentArtist.equals(cur.getString(artistColumn))){


                currentArtist = cur.getString(artistColumn);
                artistSongs.clear();
            }else{
                artistSongs.add(newSong);
            }


            if(!currentAlbum.equals(cur.getString(albumColumn))){


                currentAlbum = cur.getString(albumColumn);
                albumSongs.clear();
            }else{
                albumSongs.add(newSong);
            }




            artistMap.put(cur.getString(artistColumn).trim().toLowerCase().replaceAll("[^A-Za-z]+", "") ,
                    artistSongs.toArray(new Song[artistSongs.size()]));

            albumMap.put(cur.getString(albumColumn).trim().toLowerCase().replaceAll("[^A-Za-z]+", ""), albumSongs.toArray(new
                    Song[albumSongs.size()]));



        } while (cur.moveToNext());

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);



        Log.i(TAG, "Done querying media. MusicRetriever is ready.");
        System.out.println("The number of artists is: " + artistMap.size() + " But artists list size is" + artists.size());

    }

    public String playSong(String songName){
        songLinkedList.clear();
        System.out.println("the size of linked list is " + songLinkedList.size());
        System.out.println("The song name is :" + songName + ":");
        if(currentPlayingSong != null && currentPlayingSong.trim().equals(songName.trim())){
            return "This song is already playing";
        }
        songName = songName.replaceAll("[^A-Za-z]+", "").toLowerCase();

        System.out.println("The Song name is" + songName + ":");
        if (mySongs.size() <= 0){
            System.out.println("NO song found");
            return "No songs on device";
        }
        int maxLength = 0;
        Uri theUri = Uri.parse("");
        String displayMessage = "";
        for(Song aSong : mySongs){
            String songTitle = aSong.getTitle().replaceAll("[^A-Za-z]+", "").toLowerCase();

            if(songTitle.equals(songName)){

                System.out.println("it is equal");
                theUri = aSong.getURI();
                maxLength = 0;
                playFile((theUri));

                displayMessage = "Playing " + aSong.getTitle();
                currentPlayingSong = aSong.getTitle();
                songLinkedList.clear();
                songLinkedList.addToBack(aSong);
                System.out.println("Added1 " + aSong.getTitle());
                return displayMessage;


            }else if(stringSearcher.lcs(songTitle,songName).length() > maxLength && maxLength <= songName.length() ){
                songLinkedList.clear();
                songLinkedList.addToBack(aSong);
                System.out.println("Added2 " + aSong.getTitle());
                theUri = aSong.getURI();
                displayMessage = "Playing " + aSong.getTitle();

                currentPlayingSong = aSong.getTitle();
                maxLength = stringSearcher.lcs(songTitle,songName).length();
            }




        }




        if(!theUri.equals(Uri.parse(""))  && !displayMessage.equals("")){
            playFile((theUri));
            return displayMessage;
        }





        maxLength = 0;
        return "No song found";


    }

    private void playFile(Uri theUri){
        if(!theUri.equals(Uri.parse("")) ){
            mediaPlayer.pause();
            mediaPlayer.reset();
            //System.out.println("My URI is " + myUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);


            try{

                mediaPlayer.setDataSource(mContext, theUri);
                mediaPlayer.prepare();
                mediaPlayer.start();

            }catch (IOException e) {
                e.printStackTrace();
                Log.e("IO","IO"+e);

            }




            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Uri theURI = Uri.parse("");
                    if(isShuffling){
                        songLinkedList.shuffleCurrent();


                    }else {
                        songLinkedList.getNext();

                    }

                    theURI  =  songLinkedList.getCurrent().getURI();

                    //https://stackoverflow.com/questions/7816551/java-lang-illegalstateexception-what-does-it-mean
                    mp.reset();
                    try{

                        mp.setDataSource(mContext, theURI);
                        mp.prepare();

                    }catch (IOException e) {
                        e.printStackTrace();
                        Log.e("IO","IO"+e);

                    }

                    mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {


                        @Override public void onPrepared(MediaPlayer player) {
                            player.start();
                        }

                    });


                }
            });
        }
    }

    /**
     * A helper method to test that the songs are being correcctly added to the hashmap.
     * @param key the key to use .get() on the hashmap
     */
    public void printTest(String key){
        key = key.trim().toLowerCase().replaceAll("[^A-Za-z]+", "");
        System.out.println("My album name key is " + key);

        Song[] songs = albumMap.get(key);

        for(Song aSong: songs){
            System.out.println("SONG IS " + aSong.getTitle() + "By + " + key + "from the album " + aSong.getAlbum());
        }

    }




    public String playArtist(String artistName){
        String originalArtistName = artistName;

        artistName = artistName.replaceAll("[^A-Za-z]+", "").toLowerCase();


        /**
         * checking if the name of the group needs a the in front
         * For example nobody says "play some the beatles" or "play some the rolling stones"
         * Most would say "play some beatles" or "play some rolling stones"
         */
        if(!artistMap.containsKey(artistName)){
            System.out.println("The key doesnt exist. Appending the");

            if(!artistMap.containsKey("the" + artistName)){
                System.out.println("The key doesnt exist.");

                return "The artist cannot be found";
            }else {
                artistName = "the" +artistName;
                originalArtistName = "The" + originalArtistName;

            }


        }



        Song[] songs = artistMap.get(artistName);
        songLinkedList.clear();

        for(Song someSong: songs){
            songLinkedList.addToBack(someSong);
        }


        if(isShuffling){
           songLinkedList.shuffleCurrent();
        }


        try{
            playFile(songLinkedList.getCurrent().getURI());
        }catch (NoSuchElementException e){
            e.printStackTrace();
        }




        return "Playing music by " + songLinkedList.getCurrent().getArtist();

    }




    public void playAlbum(String albumName){

        albumName = albumName.replaceAll("[^A-Za-z]+", "").toLowerCase();

        if(!artistMap.containsKey(albumName)){
            System.out.println("The key doesnt exist");
            return;
        }
        Song[] songs = albumMap.get(albumName);


        for(Song someSong: songs){
            songLinkedList.addToBack(someSong);
        }


        if(isShuffling){
            songLinkedList.shuffleCurrent();
        }

        playFile(songLinkedList.getCurrent().getURI());

    }

    public String playBoth(String searchKey){
        searchKey =  searchKey.replaceAll("[^A-Za-z]+", "").toLowerCase();



        if(!artistMap.containsKey(searchKey)){
            System.out.println("The key doesnt exist");
            return "Artist not found";
        }
        Song[] songs = albumMap.get(searchKey);


        for(Song someSong: songs){
            songLinkedList.addToBack(someSong);
        }








        /**
         * checking if the name of the group needs a the in front
         * For example nobody says "play some the beatles" or "play some the rolling stones"
         * Most would say "play some beatles" or "play some rolling stones"
         */
        if(!artistMap.containsKey(searchKey)){
            System.out.println("The key doesnt exist. Appending the");

            if(!artistMap.containsKey("the" + searchKey)){
                System.out.println("The key doesnt exist.");

                return "The artist cannot be found";
            }else {
                searchKey = "the" + searchKey;


            }


        }



        songs = artistMap.get(searchKey);

        for(Song someSong: songs){
            songLinkedList.addToBack(someSong);
        }




        if(isShuffling){
            songLinkedList.shuffleCurrent();
        }

        playFile(songLinkedList.getCurrent().getURI());

        return "Playing " + songLinkedList.getCurrent().getTitle();
    }







    public void enableShuffling(){
        this.isShuffling = true;
    }


    public void disableShuffling(){
        this.isShuffling = false;
    }


    /**
     * plays the next song in the song linked list
     */
    public String playNext(){
        if(songLinkedList.size() > 1){
            songLinkedList.getNext();
            playFile(songLinkedList.getCurrent().getURI());
            return "Playing " + songLinkedList.getCurrent().getTitle();
        }

        return "What do you want me to play?";


    }


    /**
     * plays the previous song in the song linked list
     */
    public String playPrevious(){
        if(songLinkedList.size() > 1){
            songLinkedList.getPrevious();
            playFile(songLinkedList.getCurrent().getURI());
        }
        return "Playing " + songLinkedList.getCurrent().getTitle();
    }


    /**
     * Pauses the media player instance
     */
    public String pause(){
        mediaPlayer.pause();
        return "Player has been paused";
    }


    /**
     * Starts the media player instance
     */
    public String startPlaying(){

        //Accounting for case where songLinked list might be empty
        if(songLinkedList.isEmpty()){
            for(Song aSong : mySongs){
                songLinkedList.addToBack(aSong);

            }
            playFile(songLinkedList.getCurrent().getURI());
        } else {
            mediaPlayer.start();
        }
        

        return "Player has been started";
    }


    /**
     * A method to play all types of entities, albums, songs, artists ect
     * @param name
     */
    public void play(String name){
        songLinkedList.clear();

    }





    








}

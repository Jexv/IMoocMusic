package guessmusic.imooc.com.imoocmusic.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * 音乐播放类
 * Created by jexv on 15-11-29.
 */
public class MyPlayer {
    //歌曲播放
    private static MediaPlayer mMusicMediaPlayer;

    //音效索引
    public final static int INDEX_STONE_ENTER=0;
    public final static int INDEX_STONE_CANCEL=1;
    public final static int INDEX_STONE_COIN=2;

    //音效的文件名称
    private final static String[] SONG_NAMES={"enter.mp3","cancel.mp3","coin.mp3"};

    //音效数组
    private static MediaPlayer[] mToneMediaPlayer=new MediaPlayer[SONG_NAMES.length];


    /**
     * 播放音效
     * @param context
     * @param index
     */
    public static void playTone(Context context,int index){
        //加载声音
        AssetManager assetManager=context.getAssets();

        if (mToneMediaPlayer[index]==null){
            mToneMediaPlayer[index]= new MediaPlayer();
            try {
                AssetFileDescriptor fileDescriptor=assetManager.openFd(SONG_NAMES[index]);

                mToneMediaPlayer[index].setDataSource(fileDescriptor.getFileDescriptor(),
                        fileDescriptor.getStartOffset(),fileDescriptor.getLength());

                mToneMediaPlayer[index].prepare();


            } catch (IOException e) {
                e.printStackTrace();

            }
        }

        mToneMediaPlayer[index].start();

    }

    /**
     * 播放歌曲
     * @param context
     * @param fileName
     */
    public static void playSong(Context context,String fileName){

        if (mMusicMediaPlayer==null){
            mMusicMediaPlayer=new MediaPlayer();
        }

        //强制重置MediaPlayer
        mMusicMediaPlayer.reset();

        //加载声音
        AssetManager assetManager=context.getAssets(); //用AssetManager从Assets取得资源
        try {
            AssetFileDescriptor fileDescriptor=assetManager.openFd(fileName);
            mMusicMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(), fileDescriptor.getLength());

            //准备播放，调用后才能播放
            mMusicMediaPlayer.prepare();
            //播放
            mMusicMediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 停止播放
     * @param context
     */
    public static void stopTheSong(Context context){
        if (mMusicMediaPlayer==null){
            mMusicMediaPlayer.stop();
        }
    }
}

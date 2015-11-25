package guessmusic.imooc.com.imoocmusic.model;

/**
 * Created by jexv on 15-11-16.
 */
public class Song {
    //歌曲名称
    private  String mSongName;
    //歌曲文件名
    private String mSongFileName;
    //歌曲名字长度
    private int mNameLength;

    public char[] getNameCharacters(){
        return mSongName.toCharArray();
    }

    public String getSongName() {
        return mSongName;
    }

    public void setSongName(String SongName) {
        this.mSongName = SongName;
        this.mNameLength=SongName.length();
    }
    public String getSongFileName() {
        return mSongFileName;
    }

    public void setSongFileName(String SongFileName) {
        this.mSongFileName = SongFileName;
    }
    public int getNameLength() {
        return mNameLength;
    }



}

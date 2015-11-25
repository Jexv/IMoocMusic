package guessmusic.imooc.com.imoocmusic.model;

import android.widget.Button;

/**文字按钮
 * Created by jexv on 15-11-15.
 */
public class WordButton {

    public int mIndex;
    public boolean mIsVisiable;
    public String mWordString;

    public Button mViewButton;

    public WordButton(){
        mIsVisiable=true;
        mWordString="";


    }
}

package guessmusic.imooc.com.imoocmusic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by jexv on 15-11-29.
 */
public class AllPassView extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_pass_view);

        //隐藏右上角金币按钮
        FrameLayout view=(FrameLayout)findViewById(R.id.layout_bar_coin);
        view.setVisibility(View.INVISIBLE);
    }

}

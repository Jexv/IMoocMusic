package guessmusic.imooc.com.imoocmusic.model;

/**
 * Created by jexv on 15-11-15.
 */
public interface IWordButtonClickListener {

    void onWordButtonClick(WordButton wordButton);
    //通过接口在MyGridView中注册，在MainActivity实现，将两者连接。执行MainActivity中实现的方法中的语句----所谓的观察者模式
}

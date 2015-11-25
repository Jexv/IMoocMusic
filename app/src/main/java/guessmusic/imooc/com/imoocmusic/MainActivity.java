package guessmusic.imooc.com.imoocmusic;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import guessmusic.imooc.com.imoocmusic.data.Const;
import guessmusic.imooc.com.imoocmusic.model.IAlertDialogButtonListener;
import guessmusic.imooc.com.imoocmusic.model.IWordButtonClickListener;
import guessmusic.imooc.com.imoocmusic.model.Song;
import guessmusic.imooc.com.imoocmusic.model.WordButton;
import guessmusic.imooc.com.imoocmusic.myui.MyGridView;
import guessmusic.imooc.com.imoocmusic.util.MyLog;
import guessmusic.imooc.com.imoocmusic.util.MyPlayer;
import guessmusic.imooc.com.imoocmusic.util.Util;

public class MainActivity extends AppCompatActivity implements IWordButtonClickListener{

    public final static String TAG="MainActivity";//值一般为类名或者包名，用来指示位置 Log

    /**答案状态--正确*/
    public final static int STATUS_ANSWER_RIGHT=1;
    /**答案状态--错误*/
    public final static int STATUS_ANSWER_WRONG=2;
    /**答案状态--不完整*/
    public final static int STATUS_ANSWER_LACK=3;
    //闪烁次数
    public final static int SPASH_TIMES=6;

    public final static int ID_DIALOG_DELETE_WORD=1;

    public final static int ID_DIALOG_TIP_ANSWER=2;

    public final static int ID_DIALOG_LACK_COINS=3;

    //唱片相关动画
    private Animation mPanAnim;
    private LinearInterpolator mPanLin; //匀速线性动画
    //拨杆相关动画
    private Animation mBarInAnim;
    private LinearInterpolator mBarInLin;

    private Animation mBarOutAnim;
    private LinearInterpolator mBarOutLin;


    private ImageView mViewPan;

    private ImageView mViewPanBar;

    //当前关的索引（过关时）
    private TextView mCurrentStagePassView;

    private TextView mCurrentStageView;

    //当前歌曲名称
    private TextView mCurrentSongNamePassView;


    //按键事件
    private ImageButton mBtnPlayStart;

    //过关界面
    private View mPassView;

    //盘片动画是否在运行判断标志
    private boolean mIsRunning=false;

    //文字框容器
    private ArrayList<WordButton> mAllWords;

    private ArrayList<WordButton> mBtnSelectWords;

    //已选择文字容器
    private LinearLayout mViewWordsContainer;

    private MyGridView mMyGridView;

    //当前歌曲
    private Song mCurrentSong;

    //当前关的索引
    private int mCurrentStageIndex=-1;

    //当前金币数量初始化
    private int mCurrentCoins=Const.TOTAL_COINS;

    //金币View
    private TextView mViewCurrentCions;

    /**
     * 以下为自己添加修改的部分
     *
     */

    private ImageButton mDeleteWordButton;


    private ImageButton mTiopAnswerButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //读取上次保存的游戏数据
        int[] datas=Util.loadData(this);
        mCurrentStageIndex=datas[Const.INDEX_LOAD_DATA_STAGE];
        mCurrentCoins=datas[Const.INDEX_LOAD_DATA_COINS];

        //初始化控件
        mViewPan=(ImageView)findViewById(R.id.imageView1);
        mViewPanBar=(ImageView)findViewById(R.id.imageView2);

        mMyGridView=(MyGridView)findViewById(R.id.gridview);

        mViewCurrentCions= (TextView) findViewById(R.id.txt_bar_cions);
        mViewCurrentCions.setText(mCurrentCoins+"");

        //注册监听
        mMyGridView.registOnWordButtonClick(this);//通过接口与MyGriDVIew关联

        mViewWordsContainer=(LinearLayout)findViewById(R.id.word_select_container);



        mPanAnim= AnimationUtils.loadAnimation(this,R.anim.rotate);
        mPanLin=new LinearInterpolator();
        mPanAnim.setInterpolator(mPanLin);
        mPanAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                mViewPanBar.startAnimation(mBarOutAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mBarInAnim= AnimationUtils.loadAnimation(this,R.anim.rotate_45);
        mBarInLin=new LinearInterpolator();
        mBarInAnim.setFillAfter(true); //动画播放完后保持播放完时的状态，而不是复原
        mBarInAnim.setInterpolator(mBarInLin);
        mBarInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewPan.startAnimation(mPanAnim);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mBarOutAnim= AnimationUtils.loadAnimation(this,R.anim.rotate_d_45);
        mBarOutAnim.setFillAfter(true);
        mBarOutLin=new LinearInterpolator();
        mBarOutAnim.setInterpolator(mBarOutLin);
        mBarOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIsRunning = false;
                mBtnPlayStart.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        mBtnPlayStart=(ImageButton)findViewById(R.id.btn_play_start);
        mBtnPlayStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlePlayButton();
            }
        });

        initCurrentStageData();//初始化游戏数据

        //处理删除按键事件
        handleDeleteWords();

        //处理提示按钮事件
        handleTipAnswer();



    }

    @Override
    public void onWordButtonClick(WordButton wordButton) {
        //观察者模式的具体执行语句
        //Toast.makeText(this,"Hello",Toast.LENGTH_SHORT).show();
        //Toast.makeText(this,wordButton.mIndex+"",Toast.LENGTH_SHORT).show();
        setSelectWords(wordButton);

        //获得答案状态
        int checkResult=checkTheAnswer();

        //检查答案
        if (checkResult == STATUS_ANSWER_RIGHT) {
            //过关并获得奖励
            handlePassEvent();
        }else if (checkResult==STATUS_ANSWER_WRONG){
            //闪烁文字并提示用户
            sparkTheWords();
        }else if (checkResult==STATUS_ANSWER_LACK){
            //设置文字颜色为白色（normal）
            for (int i = 0; i < mBtnSelectWords.size(); i++) {
                mBtnSelectWords.get(i).mViewButton.setTextColor(Color.WHITE);
            }
        }
    }

    //处理过关界面以及事件

    /**
     * 重要，这里有一个Bug，即使显示了过关界面，半透明界面后的按钮却仍是可以点击的
     * 这样可能会误扣金币，所以应当设置背面的按钮为不可点击
     *
     */
    private void handlePassEvent(){
        mPassView=(LinearLayout)this.findViewById(R.id.pass_view);
        mPassView.setVisibility(View.VISIBLE);

        //停止未完成的动画
        mViewPan.clearAnimation();

        //停止正在播放的音乐
        MyPlayer.stopTheSong(MainActivity.this);

        //播放过关获得金币音效
        MyPlayer.playTone(MainActivity.this,MyPlayer.INDEX_STONE_COIN);


        //当前关的索引
        mCurrentStagePassView=(TextView)findViewById(R.id.text_current_stage_pass);
        if (mCurrentStagePassView!=null){
            mCurrentStagePassView.setText((mCurrentStageIndex+1)+"");
        }

        //显示歌曲名称
        mCurrentSongNamePassView=(TextView)findViewById(R.id.text_current_song_name_pass);
        mCurrentSongNamePassView.setText(mCurrentSong.getSongName());

        //下一关按键处理
        ImageButton btnPass=(ImageButton)findViewById(R.id.btn_next);
        btnPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (judgeAppPassed()){
                    //进入通关界面
                    Util.startActivity(MainActivity.this,AllPassView.class);
                }else {
                    //开始新一关
                    mPassView.setVisibility(View.GONE);

                    //加载关卡数据
                    initCurrentStageData();
                }
            }
        });

    }

    /**
     * 判断是否通关
     * @return
     */
    private boolean judgeAppPassed(){
        return (mCurrentStageIndex==Const.SONG_INFO.length-1);
    }


    //设置答案
    private void setSelectWords(WordButton wordButton){
        for (int i = 0; i <mBtnSelectWords.size() ; i++) {
            if (mBtnSelectWords.get(i).mWordString.length()==0){
                //设置答案文字框内容，以及可见性
                mBtnSelectWords.get(i).mViewButton.setText(wordButton.mWordString);
                mBtnSelectWords.get(i).mIsVisiable=true;
                mBtnSelectWords.get(i).mWordString=wordButton.mWordString;

                //记录索引
                mBtnSelectWords.get(i).mIndex=wordButton.mIndex;

                //Log....
                MyLog.d(TAG,mBtnSelectWords.get(i).mIndex+"");//第二个值是你想检查的内容


                //设置待选框可见性
                setButtonVisiable(wordButton, View.INVISIBLE);

                break;//条件成立循环一次就退出，防止再遍历，出现重复设置字符
            }
        }
    }

    //清除已选文字
    private void clearTheAnswer(WordButton wordButton){
        wordButton.mViewButton.setText("");
        wordButton.mWordString="";
        wordButton.mIsVisiable=false;

        //设置待选框的可见性
        setButtonVisiable(mAllWords.get(wordButton.mIndex),View.VISIBLE);//根据索引设置，防止把所有的都设置为可见

    }


    //设置待选文字框是否可见
    private void setButtonVisiable(WordButton button,int visiability){
        button.mViewButton.setVisibility(visiability);
        button.mIsVisiable=(visiability==View.VISIBLE)?true:false;

        //Log
        MyLog.d(TAG, button.mIsVisiable + "");
    }


//处理圆盘中间的播放按钮，就是开始播放音乐
    private void handlePlayButton(){
        if (mViewPanBar!=null){
            if (!mIsRunning) {
                mIsRunning = true;
                //开始拨杆进入动画
                mViewPanBar.startAnimation(mBarInAnim);
                mBtnPlayStart.setVisibility(View.INVISIBLE);

                //播放音乐
                MyPlayer.playSong(MainActivity.this,mCurrentSong.getSongFileName());
            }
        }

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause() {
        //保存游戏数据
        Util.saveData(MainActivity.this,mCurrentStageIndex-1,mCurrentCoins);

        mViewPan.clearAnimation(); //进入Pause状态，退出，进入后台的时候停止动画

        //暂停音乐
        MyPlayer.stopTheSong(MainActivity.this);
        super.onPause();
    }



    //导入歌曲信息
    private  Song loadStageSongInfo(int stageIndex){

        Song song=new Song();
        String[] stage= Const.SONG_INFO[stageIndex];
        song.setSongFileName(stage[Const.INDEX_FILENAME]);  //取得歌曲文件名
        song.setSongName(stage[Const.INDEX_SONGNAME]);  //取得歌曲名

        return song;
    }


    /**
     * 加载当前关的数据
     */
    public void initCurrentStageData(){
        //读取当前关的歌曲信息
        mCurrentSong=loadStageSongInfo(++mCurrentStageIndex);

        //初始化已选择框
        mBtnSelectWords=initWordSelect();
        LinearLayout.LayoutParams params= new LinearLayout.LayoutParams(140,140);

        //清空原来的答案
        mViewWordsContainer.removeAllViews();
        //增加新的答案框
        for (int i=0;i<mBtnSelectWords.size();i++){

            mViewWordsContainer.addView(mBtnSelectWords.get(i).mViewButton,params);
        }

        //显示当前关的索引
        mCurrentStageView=(TextView)findViewById(R.id.text_current_stage);
        if (mCurrentStageView!=null){
            mCurrentStageView.setText((mCurrentStageIndex+1)+"");
        }



        //获得数据
        mAllWords=initAllWord();
        //更新数据--MyGridView
        mMyGridView.updateData(mAllWords);

        //每关加载完数据时先播放一次音乐
        handlePlayButton();
    }


    //初始化待选文字框
    private ArrayList<WordButton> initAllWord(){
        ArrayList<WordButton> data=new ArrayList<WordButton>();
        //获得所有待选文字
        String[] words=generateWords();//取得generateWords()方法返回的待选的汉字 字符串数组


        for (int i=0;i< MyGridView.COUNTS_WORDS;i++){  //循环产生一组测试数据
            WordButton button=new WordButton();
            button.mWordString=words[i];
            data.add(button);
        }

        return data;

    }




    //初始化已选择文字框
    private ArrayList<WordButton> initWordSelect(){
        ArrayList<WordButton> data=new ArrayList<WordButton>();
        for (int i=0;i<mCurrentSong.getNameLength();i++){
            View convertView= Util.getView(MainActivity.this, R.layout.self_ui_gridview_item);

            final WordButton holder=new WordButton();
            holder.mViewButton= (Button)convertView.findViewById(R.id.item_btn);
            holder.mViewButton.setTextColor(Color.WHITE);
            holder.mViewButton.setText("");
            holder.mIsVisiable=false;

            holder.mViewButton.setBackgroundResource(R.mipmap.game_wordblank);

            holder.mViewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearTheAnswer(holder);
                }
            });
            data.add(holder);

        }

        return data;
    }



    //生成所有的待选文字,包括正确歌名
    private String[] generateWords(){
        Random random=new Random();

        String[] words=new String[MyGridView.COUNTS_WORDS];

        //存入歌名
        for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
            words[i]=mCurrentSong.getNameCharacters()[i]+"";//+""----加上空的字符串可以char自动转换成字符串
        }

        //获取随机文字并存入数组
        for (int i = mCurrentSong.getNameLength(); i <MyGridView.COUNTS_WORDS ; i++) {
            words[i]=getRandomChar()+"";
        }

        //打乱文字顺序:首先选一个元素与最后一个元素交换，然后从倒数第二个之后选一个元素与倒数第二个元素交换，以此类推...直到第一个元素

        for (int i = MyGridView.COUNTS_WORDS-1; i >=0 ; i--) {
            int index=random.nextInt(i + 1);
            
            String buf=words[index];
            words[index]=words[i];
            words[i]=buf;
        }


        return words;
    }

    //随机生成候选文字
    private char getRandomChar(){
        String str="";
        int hightPos;  //汉纸编码的高位字节
        int lowPos;    //汉子编码的低位字节
        Random random=new Random();

        hightPos=(176+Math.abs(random.nextInt(39)));
        lowPos=(161+Math.abs(random.nextInt(93)));

        byte[] b=new byte[2];

        b[0]=(Integer.valueOf(hightPos)).byteValue();
        b[1]=(Integer.valueOf(lowPos)).byteValue();

        try {
            str=new String(b,"GBK");  //将字节转换成GBK编码，生成汉字并保存到str中
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return str.charAt(0);

    }


    private int checkTheAnswer(){
        //先检查答案长度
        for (int i = 0; i < mBtnSelectWords.size(); i++) {
            //如果有空的，说明答案不完整
            if (mBtnSelectWords.get(i).mWordString.length() == 0) {
                return STATUS_ANSWER_LACK;
            }
        }

        //答案完整，继续检查正确性
        StringBuffer sb=new StringBuffer();
        for (int i = 0; i <mBtnSelectWords.size() ; i++) {
            sb.append(mBtnSelectWords.get(i).mWordString);
        }

        return (sb.toString().equals(mCurrentSong.getSongName()))?STATUS_ANSWER_RIGHT:STATUS_ANSWER_WRONG;
    }

    //文字闪烁
    private void sparkTheWords(){
        //定时器要执行的任务
        TimerTask timerTask=new TimerTask() {
            boolean mChange=false;
            int mSpardTime=0;//闪烁次数

            @Override
            public void run() {
                //UI更新需要在UI线程中完成，runOnUiThread是解决办法之一
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (++mSpardTime>SPASH_TIMES){
                            return;
                        }

                        //执行闪烁逻辑，交替显示红色和白色文字
                        for (int i = 0; i < mBtnSelectWords.size(); i++) {
                            mBtnSelectWords.get(i).mViewButton.setTextColor(
                                    mChange?Color.RED:Color.WHITE
                            );
                        }

                        mChange=!mChange;
                    }
                });
            }


        };
        //定时器
        Timer timer=new Timer();
        timer.schedule(timerTask,1,150);//给定时器添加任务，设定时间
    }

    //自动选择一个答案
    private void tipAnswer(){

        boolean tipWord=false;
        for (int i = 0; i <mBtnSelectWords.size() ; i++) {
            if (mBtnSelectWords.get(i).mWordString.length()==0){
                //根据当前答案框条件选择对应文字并填入
                onWordButtonClick(findIsAnswerWord(i));
                tipWord=true;

                //减少金币数量
                if (!handleCoins(-getTipCoins())){
                    //金币数量不够，显示对话框
                    showConfirmDialog(ID_DIALOG_LACK_COINS);
                    return;
                }
                break;
            }
        }

        //没有找到可以填充的答案（可能是格子已经被占用了），闪烁提示
        if (!tipWord){
            //闪烁文字提示
            sparkTheWords();
        }

    }


    /**
     * 找到一个答案文字
     *
     * @param index 当前要填入答案的索引
     * @return
     */
    private WordButton findIsAnswerWord(int index){
        WordButton buf=null;

        for (int i = 0; i < MyGridView.COUNTS_WORDS; i++) {
            buf=mAllWords.get(i);

            if (buf.mWordString.equals(""+mCurrentSong.getNameCharacters()[index])){
                return buf;
            }

        }
        return null;

    }


    //删除文字
    private void deleteOneWord(){
        //减少金币
        if (!handleCoins(-getDeleteWordCoins())){
            //金币不够，显示对话框
            return;
        }

        //将索引对应的WordButton设置为不可见
        setButtonVisiable(findNotAnswerWord(),View.INVISIBLE);

    }


    //找到一个不是答案的文件，并且当前是可见的
    private WordButton findNotAnswerWord(){
        Random random=new Random();
        WordButton buf=null;
        while (true){
            int index=random.nextInt(MyGridView.COUNTS_WORDS);

            buf=mAllWords.get(index);

            if (buf.mIsVisiable&&!isTheAnswerWord(buf)) {
                return buf;
            }
        }
    }


    //判断某个文字是否为答案
    private boolean isTheAnswerWord(WordButton wordButton){
        boolean result=false;

        for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
            if (wordButton.mWordString.equals(""+mCurrentSong.getNameCharacters()[i])) {
                result=true;
                break;
            }
        }
        return result;
    }

    //增加或者减少指定数量的金币@return true 增加/减少成功 false 增加/减少失败
    private boolean handleCoins(int data){
        //判断当前总的数量是否可被减少
        if (mCurrentCoins+data>=0){
            mCurrentCoins+=data;

            mViewCurrentCions.setText(mCurrentCoins+"");
        }else {
            //金币不够
            return false;
        }
        return true;
    }

    //从配置文件读取删除操作所要的金币
    private int getDeleteWordCoins(){

        return this.getResources().getInteger(R.integer.pay_delete_word);
    }

    //从配置文件去除一个错误答案操作所要的金币
    private int getTipCoins(){

        return this.getResources().getInteger(R.integer.pay_tip_answer);
    }

    //处理删除待选文字事件
    private void handleDeleteWords(){
        ImageButton button=(ImageButton)findViewById(R.id.btn_delete_word);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //    deleteOneWord();
                showConfirmDialog(ID_DIALOG_DELETE_WORD);

            }
        });
    }

    //处理提示事件
    private void handleTipAnswer(){
        ImageButton button=(ImageButton)findViewById(R.id.btn_tip_answer);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //    tipAnser();
                showConfirmDialog(ID_DIALOG_TIP_ANSWER);

            }
        });
    }

    //自定义AlertDialog事件响应
    //删除错误答案
    private IAlertDialogButtonListener mBtnOkDeleteWordListener=new IAlertDialogButtonListener() {
        @Override
        public void onClick() {
            deleteOneWord();
            //执行事件
        }
    };


    //答案提示
    private IAlertDialogButtonListener mBtnOkTipAnswerListener=new IAlertDialogButtonListener() {
        @Override
        public void onClick() {
            tipAnswer();
            //执行事件
        }
    };

    //金币不足
    private IAlertDialogButtonListener mBtnOkLackCoinsListener=new IAlertDialogButtonListener() {
        @Override
        public void onClick() {
            //执行事件
            //跳到商店
        }
    };

    /**
     * 显示对话框
     * @param id
     */
    private void showConfirmDialog(int id){
        switch (id){
            case ID_DIALOG_DELETE_WORD:
                Util.showDialog(MainActivity.this,"确认花掉"+getDeleteWordCoins()+
                        "个金币去掉一个错误答案？",mBtnOkDeleteWordListener);
                break;
            case ID_DIALOG_TIP_ANSWER:
                Util.showDialog(MainActivity.this,"确认花掉"+getTipCoins()+
                        "个金币获得一个文字提示？",mBtnOkTipAnswerListener);
                break;
            case ID_DIALOG_LACK_COINS:
                Util.showDialog(MainActivity.this,"金币不足，去商店补充",mBtnOkLackCoinsListener);
                break;
        }
    }

}

package guessmusic.imooc.com.imoocmusic.myui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import java.util.ArrayList;

import guessmusic.imooc.com.imoocmusic.R;
import guessmusic.imooc.com.imoocmusic.model.IWordButtonClickListener;
import guessmusic.imooc.com.imoocmusic.model.WordButton;
import guessmusic.imooc.com.imoocmusic.util.Util;

/**
 * Created by jexv on 15-11-15.
 */
public class MyGridView extends GridView{


    public final static int COUNTS_WORDS=24;
    private ArrayList<WordButton> mArrayList=new ArrayList<WordButton>();

    private MyGridAdapter myGridAdapter;
    private Context mContext;  //表示GridView本身

    private Animation mScaleAnimation;

    private IWordButtonClickListener mWordButtonListener;

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        myGridAdapter=new MyGridAdapter();
        this.setAdapter(myGridAdapter);

    }


    public void updateData(ArrayList<WordButton> list){
        mArrayList=list;
        setAdapter(myGridAdapter);//重新设置数据源
    }


    class MyGridAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return mArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final WordButton holder;
            if (convertView==null){   //判断GridView是否为空,为空则导入View和WordButton,并且关联mArrayList中的数据
                convertView= Util.getView(mContext, R.layout.self_ui_gridview_item);

                holder=mArrayList.get(position);

                //加载动画
                mScaleAnimation= AnimationUtils.loadAnimation(mContext,R.anim.scale);
                //设置动画延迟时间
                mScaleAnimation.setStartOffset(position*100);

                holder.mIndex=position;
                holder.mViewButton= (Button)convertView.findViewById(R.id.item_btn);
                holder.mViewButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mWordButtonListener.onWordButtonClick(holder);
                    }
                });
                convertView.setTag(holder);//将数据设置到View上
            }else {           //不为空则直接导入数据
                holder=(WordButton)convertView.getTag();
            }

            holder.mViewButton.setText(holder.mWordString);//将Tag时得到的文字数据显示到按钮上

            //播放动画
            convertView.startAnimation(mScaleAnimation);

            return convertView;
        }
    }

    //注册监听接口
    public void registOnWordButtonClick(IWordButtonClickListener listener){
        mWordButtonListener =listener;

    }
}

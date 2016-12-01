package io.github.b_lam.resplash.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.pixelcan.inkpageindicator.InkPageIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.b_lam.resplash.R;

public class IntroActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener{

    @BindView(R.id.activity_intro_btnClose) ImageButton btnClose;
    @BindView(R.id.activity_intro_button) Button btnNext;
    @BindView(R.id.activity_intro_viewPager) ViewPager mViewPager;
    @BindView(R.id.activity_intro_indicator) InkPageIndicator mIndicator;

    private IntroPagerAdapter mIntroPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        ButterKnife.bind(this);

        btnClose.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        mIntroPagerAdapter = new IntroPagerAdapter(this);
        mViewPager.setAdapter(mIntroPagerAdapter);
        mViewPager.addOnPageChangeListener(this);

        mIndicator.setViewPager(mViewPager);

    }

    @Override
    public void onClick(View view){
        switch (view.getId()) {
            case R.id.activity_intro_btnClose:
                finish();
                break;

            case R.id.activity_intro_button:
                if (mViewPager.getCurrentItem() == mIntroPagerAdapter.getCount() - 1) {
                    finish();
                } else {
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                }
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        changeNextButtonText(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private void changeNextButtonText(int page){
        if(page == mIntroPagerAdapter.getCount() - 1){
            btnNext.setText(getString(R.string.intro_done));
        }else {
            btnNext.setText(getString(R.string.intro_next));
        }
    }

    class IntroPagerAdapter extends PagerAdapter{

        Context mContext;
        LayoutInflater mInflater;

        public IntroPagerAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View itemView = mInflater.inflate(R.layout.item_intro_slide, container, false);
            ImageView imageView = (ImageView) itemView.findViewById(R.id.intro_image);
            TextView title_textView = (TextView) itemView.findViewById(R.id.intro_title);
            TextView subtitle_textView = (TextView) itemView.findViewById(R.id.intro_description);
            switch (position) {
                case 0:
                    imageView.setImageResource(R.drawable.intro_welcome_image);
                    title_textView.setText(getString(R.string.intro_welcome_title));
                    subtitle_textView.setText(getString(R.string.intro_welcome_description));
                    break;
                case 1:
                    imageView.setImageResource(R.drawable.intro_welcome_image);
                    title_textView.setText(getString(R.string.intro_download_title));
                    subtitle_textView.setText(getString(R.string.intro_download_description));
                    break;
            }
            container.addView(itemView);
            return itemView;
        }
    }
}

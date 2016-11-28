package com.dante.rxdemo;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dante.rxdemo.model.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PictureActivity extends AppCompatActivity {
    private static final String TAG = "PictureActivity";

    List<Image> data;
    List<Fragment> fragments = new ArrayList<>();
    @BindView(R.id.pager)
    ViewPager pager;
    int position;
    private DetailPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        ButterKnife.bind(this);
        supportPostponeEnterTransition();

        data = getIntent().getParcelableArrayListExtra("data");
        position = getIntent().getIntExtra("position", 0);

        for (int i = 0; i < data.size(); i++) {
            fragments.add(ViewerFragment.newInstance(data.get(i)));
        }
        adapter = new DetailPagerAdapter(getSupportFragmentManager(), fragments, data.size());
        pager.setAdapter(adapter);
        pager.setCurrentItem(position);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int current) {
//                setEnterSharedElement(current);
                if (position != current) {
                    PreferenceManager.getDefaultSharedPreferences(PictureActivity.this)
                            .edit().putInt("position", current)
                            .apply();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });
    }

    private void setEnterSharedElement(final int current) {
        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                if (position != current) {
                    View sharedView = getImageAtPosition(current);
                    names.clear();
                    sharedElements.clear();
                    names.add(sharedView.getTransitionName());
                    sharedElements.put(sharedView.getTransitionName(), sharedView);
                }
            }

            /**
             * Returns the image of interest to be used as the entering/returning
             * shared element during the activity transition.
             */
            private View getImageAtPosition(int position) {
                ImageView view = new ImageView(PictureActivity.this);
                Glide.with(PictureActivity.this).load(data.get(position).image).into(view);
                return view;
            }
        });
    }

    private class DetailPagerAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> fragments;
        private int size;

        DetailPagerAdapter(FragmentManager fm, List<Fragment> fragments, int dataSize) {
            super(fm);
            this.fragments = fragments;
            this.size = dataSize;
        }

        public void addFragment(Fragment fragment) {
            fragments.add(fragment);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return size;
        }

    }
}

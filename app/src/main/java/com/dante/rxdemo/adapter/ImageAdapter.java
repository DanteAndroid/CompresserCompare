package com.dante.rxdemo.adapter;

import android.support.v4.view.ViewCompat;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.dante.rxdemo.R;
import com.dante.rxdemo.model.Image;


/**
 * Created by yons on 16/11/25.
 */

public class ImageAdapter extends BaseQuickAdapter<Image> {

    public ImageAdapter() {
        super(R.layout.recyclerview_item, null);
    }
 

    @Override
    protected void convert(BaseViewHolder holder, Image image) {
         holder.setText(R.id.imageType, image.type)
                 .setText(R.id.size, image.size);
        ViewCompat.setTransitionName(holder.getView(R.id.image), image.type);

        Glide.with(mContext).load(image.image).diskCacheStrategy(DiskCacheStrategy.NONE).into((ImageView) holder.getView(R.id.image));
    }
}

package com.dante.rxdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.dante.rxdemo.libs.TouchImageView;
import com.dante.rxdemo.model.Image;

import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yons on 16/11/25.
 */
public class ViewerFragment extends Fragment {
    private static final String TAG = "ViewerFragment";
    @BindView(R.id.image)
    TouchImageView imageView;
    @BindView(R.id.type)
    TextView type;
    private Image image;

    public static ViewerFragment newInstance(Image image) {
        ViewerFragment fragment = new ViewerFragment();
        Bundle args = new Bundle();
        args.putParcelable("image", image);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_viewer, container, false);
        ButterKnife.bind(this, rootView);
        init();

        return rootView;
    }

    private void init() {
        image = getArguments().getParcelable("image");
        if (image == null) {
            return;
        }
        ViewCompat.setTransitionName(imageView, image.type);
        new LoadPictureTask().execute();

        type.setText(String.format("Type: %s", image.type));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void finish() {
        getActivity().setResult(Activity.RESULT_OK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().finishAfterTransition();
        } else {
            getActivity().finish();
        }
    }


    private class LoadPictureTask extends AsyncTask<Void, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Void... voids) {
            if (isCancelled()) {
                return null;
            }
            Bitmap bitmap = null;
            try {
                bitmap = Glide.with(ViewerFragment.this).load(image.image)
                        .asBitmap()
                        .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap picture) {
            if (isCancelled()) {
                return;
            }
            imageView.setImageBitmap(picture);
            if (getActivity()!=null)            getActivity().supportStartPostponedEnterTransition();

        }
    }
}

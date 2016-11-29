package com.dante.rxdemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.dante.rxdemo.adapter.ImageAdapter;
import com.dante.rxdemo.model.Image;
import com.dante.rxdemo.utils.Util;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.zelory.compressor.Compressor;
import id.zelory.compressor.FileUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import static android.R.attr.duration;
import static com.dante.rxdemo.App.context;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CROP_REQUEST = 2;
    private static final int REQUEST_TAKE_PHOTO = 3;
    private static final int REQUEST_VIEW = 4;
    @BindView(R.id.choose)
    Button choose;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.compress)
    Button compress;

    private File originalImage;
    private File image;
    private File compressdImage;
    private LinearLayoutManager layoutManager;
    private ImageAdapter adapter;
    /*
    Add new compress tool here,
    and add the method with same name(`compressImage` will find and execute it)
    */

    private String[] compressType = {"Original", "Compressor", "Luban"};


    private void load(File file, long time) {
        if (file == null || !file.exists()) {
            showToast("Can't load file.");
            return;
        }
        int dataSize = adapter.getData().size();
        //if compress type equals data size
        //no need to add data (need clear instead)
        if (compressType.length > dataSize) {
            String type = compressType[adapter.getData().size()];
            Log.i(TAG, "load: size " + file.length());
            String size = String.format("Size: %S", Util.getReadableSize(file.length()));
            String duration = String.format(Locale.getDefault(), "Duration: %d ms", time);
            if (time == 0) duration = "N/A";
            Image image = new Image(file, type, size, duration);
            if (dataSize == 0) adapter.notifyDataSetChanged();
            adapter.addData(image);
        } else {
            showToast("Compress finished.");
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        int position = sp.getInt("position", -1);
        if (position >= 0) {
            sp.edit().remove("position").apply();//Remove after read it
            recycler.smoothScrollToPosition(position);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initRecyclerView();

    }

    private void initRecyclerView() {
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(layoutManager);
        adapter = new ImageAdapter();
        adapter.openLoadAnimation(BaseQuickAdapter.SCALEIN);
        recycler.setAdapter(adapter);
        recycler.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void SimpleOnItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                startViewer(view.findViewById(R.id.image), i);
            }
        });
    }

    public void chooseImage(View v) {
        clearData();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void clearData() {
        adapter.getData().clear();
    }

    private void deleteCache() {
        File dir = getExternalCacheDir();
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String file : children) {
                boolean result = new File(dir, file).delete();
                if (!result) {
                    Log.w(TAG, "deleteCache: failed ");
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            Log.i(TAG, "onActivityResult: canceld");

        } else if (requestCode == PICK_IMAGE_REQUEST) {
            if (data == null) {
                showToast("Read picture failed.");
            } else {
                try {
                    originalImage = FileUtil.from(this, data.getData());
                    load(originalImage, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                cropPhoto(data.getData());

            }
        } else if (requestCode == CROP_REQUEST) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = null;
            if (extras != null) {
                Log.i(TAG, "onActivityResult: extras not null");
                bitmap = extras.getParcelable("data");
                if (bitmap != null) {
                    Log.i(TAG, "bitmap : " + bitmap.getWidth() + " * " + bitmap.getHeight());
                }
            }
            load(image, 0);
        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            load(originalImage, 0);
        }
    }


//    private void cropPhoto(Uri uri) {
//        if (image == null)
//            image = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "temp" + suffix);
//        if (!image.exists()) {
//            try {
//                boolean result = image.createNewFile();
//                Log.i(TAG, "cropPhoto: created " + result);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        Intent crop = new Intent("com.android.camera.action.CROP");
//        crop.setDataAndType(uri, "image/*");
//        crop.putExtra("crop", false);
////        crop.putExtra("scale", true);
////        crop.putExtra("outputX", size);
////        crop.putExtra("outputY", size);
////        crop.putExtra("return-data", false);
//        crop.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
//        startActivityForResult(crop, CROP_REQUEST);
//    }

    public void compressImage(View v) {
        if (adapter.getItemCount() <= 0) {
            chooseImage(null);
        } else if (adapter.getItemCount() == compressType.length) {
            return;
        }

        for (int i = 1; i < compressType.length; i++) {
            Method compress;
            try {
                compress = this.getClass().getDeclaredMethod(compressType[i]);
                Log.i(TAG, "compressImage: " + compressType[i]);
                compress.invoke(this);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteCache();
    }

    private void startViewer(View view, int position) {
        Intent intent = new Intent(context, PictureActivity.class);
        intent.putExtra("position", position);
        ArrayList<Image> data = (ArrayList<Image>) adapter.getData();
        intent.putParcelableArrayListExtra("data", data);
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, view, view.getTransitionName());
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }


    public void takePhoto(View view) {
        clearData();
        checkPermission();
    }

    private void take() {
        originalImage = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
        if (!originalImage.exists()) {
            try {
                boolean result = originalImage.createNewFile();
                if (!result) {
                    showToast("Unable to create file, please check permission.");
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(originalImage));
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    private void checkPermission() {
        new TedPermission(this)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        take();
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> arrayList) {
                        Toast.makeText(MainActivity.this, "Permission denied, taking photo will not work.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setDeniedMessage(R.string.permission_hint)
                .setPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }


    private void Luban() {
        final long start = System.currentTimeMillis();
        Luban.get(this).load(originalImage)
                .putGear(Luban.THIRD_GEAR)
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(File file) {
                        long duration = System.currentTimeMillis() - start;
                        load(file, duration);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                }).launch();
    }

    private void Compressor() {
        final long start = System.currentTimeMillis();
        Compressor.getDefault(this)
                .compressToFileAsObservable(originalImage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        long duration = System.currentTimeMillis() - start;
                        load(file, duration);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showToast(throwable.getMessage());
                    }
                });
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

}

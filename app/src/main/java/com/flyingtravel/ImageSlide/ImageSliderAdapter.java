package com.flyingtravel.ImageSlide;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.flyingtravel.R;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by wei on 2015/11/10.
 */
public class ImageSliderAdapter extends PagerAdapter {

    ImageLoader imageLoader = ImageLoader.getInstance();
    DisplayImageOptions options;
    private ImageLoadingListener listener;
    FragmentActivity fragmentActivity;
    List<Product> products;
    // HomeFragment=MainImageFragment
    MainImageFragment fragment;

    public ImageSliderAdapter(final FragmentActivity fragment_Activity, List<Product> I_products,
                              final MainImageFragment main_fragment) {
        this.fragmentActivity = fragment_Activity;
        this.products = I_products;
        this.fragment = main_fragment;

        options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.error)
                .showImageForEmptyUri(R.drawable.empty)
                .cacheInMemory().cacheOnDisk(true).showImageOnLoading(R.drawable.loading2)
                .cacheOnDisc().build();
        listener = new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
            }
        };
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        LayoutInflater inflater = (LayoutInflater) fragmentActivity
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.main_vp_image, container, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.main_image_display);
//        以下是應該可以省略的部分
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Bundle arguments = new Bundle();
//                Fragment fragment = null;
//                Log.d("position adapter", "" + position);
//                Product product = (Product) products.get(position);
//                arguments.putParcelable("singleProduct", product);
//
//                 Start a new fragment
//                fragment = new ProductDetailFragment();
//                fragment.setArguments(arguments);

//                FragmentTransaction transaction = activity
//                        .getSupportFragmentManager().beginTransaction();
//            }
//        });
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                fragmentActivity.getBaseContext()).denyCacheImageMultipleSizesInMemory()
                .build();
        ImageLoader.getInstance().init(config);
        imageLoader.displayImage(((Product) products.get(position)).getImageUrl()
                , imageView, options, listener);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    private static class ImageDisplayListener extends
            SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections
                .synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view,
                                      Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }

}

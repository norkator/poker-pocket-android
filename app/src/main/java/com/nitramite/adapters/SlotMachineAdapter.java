package com.nitramite.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.nitramite.wheel.adapters.AbstractWheelAdapter;
import com.nitramite.pokerpocket.R;
import android.widget.LinearLayout;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class SlotMachineAdapter extends AbstractWheelAdapter {

    // Slot adapter images
    private final int items[] = new int[] {
            R.mipmap.clubs_icon,
            R.mipmap.hearts_icon,
            R.mipmap.spades_icon,
            R.mipmap.diamonds_icon,
    };

    // Cached images
    private List<SoftReference<Bitmap>> images;

    // Layout inflater
    private Context context;

    // Size parameters
    private int imageWidth;
    private int imageHeight;

    // Layout params for image view
    private LinearLayout.LayoutParams params;

    // Constructor
    public SlotMachineAdapter(Context context, final int imageWidth, final int imageHeight) {
        this.context = context;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        params = new LinearLayout.LayoutParams(imageWidth, imageHeight);
        images = new ArrayList<SoftReference<Bitmap>>(items.length);
        for (int id : items) {
            images.add(new SoftReference<Bitmap>(loadImage(id)));
        }
    }


    /**
     * Loads image from resources
     */
    private Bitmap loadImage(int id) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, imageWidth, imageHeight, true);
        bitmap.recycle();
        return scaled;
    }

    @Override
    public int getItemsCount() {
        return items.length;
    }

    @Override
    public View getItem(int index, View cachedView, ViewGroup parent) {
        ImageView img;
        if (cachedView != null) {
            img = (ImageView) cachedView;
        } else {
            img = new ImageView(context);
        }
        params.gravity = Gravity.CENTER;
        img.setLayoutParams(params);
        SoftReference<Bitmap> bitmapRef = images.get(index);
        Bitmap bitmap = bitmapRef.get();
        if (bitmap == null) {
            bitmap = loadImage(items[index]);
            images.set(index, new SoftReference<Bitmap>(bitmap));
        }
        img.setImageBitmap(bitmap);

        return img;
    }

} // End of adapter
package com.example.send.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.send.R;
import com.squareup.picasso.Picasso;

public class ImageHelper {
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
    public static Bitmap fitWidthBitmap(Bitmap bitmap, int newWidth){
        int oldWidth = bitmap.getWidth();
        int oldHeight = bitmap.getHeight();
        int newHeight = oldHeight*newWidth/oldWidth;
        Log.w("image_helper", "newWidth:"+newWidth+" newHeight:"+newHeight);
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
    }

    public static Bitmap fitHeightBitmap(Bitmap bitmap, int newHeight){
        int oldWidth = bitmap.getWidth();
        int oldHeight = bitmap.getHeight();
        int newWidth = oldWidth*newHeight/oldHeight;
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
    }

    public static void setPictureInImageView(Bitmap bitmap, ImageView targetView, int availableSpace, Resources resources) {
        //bitmap gets resized to not take to much RAM
        int newWidth =  resources.getDimensionPixelSize(R.dimen.inner_content_width);
        bitmap = ImageHelper.fitWidthBitmap(bitmap, newWidth);

        if (availableSpace >= bitmap.getHeight()) {
            targetView.getLayoutParams().height = bitmap.getHeight();
        } else {
            targetView.getLayoutParams().height = availableSpace;
            bitmap = ImageHelper.fitHeightBitmap(bitmap, availableSpace);
        }
        //remove background otherwise there would be two frames for image
        targetView.setBackground(null);

        //round corners to fit UI style
        bitmap = ImageHelper.getRoundedCornerBitmap(bitmap, resources.getDimensionPixelSize(R.dimen.round_corners));

        LayerDrawable layerDrawable = (LayerDrawable) resources.getDrawable(R.drawable.image_view);/*drawable*/
        Drawable newDrawable = new BitmapDrawable(resources, bitmap);
        layerDrawable.setDrawableByLayerId(R.id.image_view_drawable, newDrawable);
        targetView.setImageDrawable(layerDrawable);
        Log.w("set_img", "height:" + bitmap.getHeight() + ", width:" + bitmap.getWidth() + ", bytes:" + bitmap.getByteCount());
    }

    public static int getAvailableSpace(View targetView, View viewUnderTarget, int minWhitespace){
        //checking if enough space on Screen available
        int[] selectButtonLocation = new int[2];
        int[] imageViewLocation = new int[2];
        viewUnderTarget.getLocationOnScreen(selectButtonLocation);
        targetView.getLocationOnScreen(imageViewLocation);

        int availableSpace = (selectButtonLocation[1] - imageViewLocation[1]) - minWhitespace;
        return availableSpace;
    }

    public static void setPictureWithPicasso(Uri uri, ImageView targetView, int availableSpace){
        Picasso.get().load(uri).into(targetView);
    }
}

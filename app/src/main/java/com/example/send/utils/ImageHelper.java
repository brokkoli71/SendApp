package com.example.send.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.send.R;

import java.io.IOException;

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

    public static Bitmap fitSizeBitmap(Bitmap bitmap, int maxHeight, int maxWidth){
        int targetWidth, targetHeight;
        double aspectRatio;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            targetWidth = maxWidth;
            aspectRatio = (double) bitmap.getHeight() / (double) bitmap.getWidth();
            targetHeight = (int) (targetWidth * aspectRatio);
        } else {
            targetHeight = maxHeight;
            aspectRatio = (double) bitmap.getWidth() / (double) bitmap.getHeight();
            targetWidth = (int) (targetHeight * aspectRatio);
        }
        Bitmap result = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);
        if (result != bitmap) {
            bitmap.recycle();
        }
        return result;
    }

    public static void setPictureInImageView(Bitmap bitmap, ImageView targetView, Resources resources) {
        //bitmap gets resized to not take to much RAM

        targetView.getLayoutParams().height = bitmap.getHeight();

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

    /*public static void setPictureWithPicasso(Uri uri, final ImageView targetView, final int availableSpace, final Resources resources){
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.w("picasso", "bitmap height"+bitmap.getHeight());

                targetView.setImageBitmap(bitmap);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                Log.e("picasso", "failed", e);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Log.w("picasso", "preparing");
                targetView.setImageDrawable(placeHolderDrawable);
            }
        };
        Picasso.get().load(uri)
                .transform(new Transformation() {
                    @Override
                    public Bitmap transform(Bitmap source) {
                        int maxWidth = resources.getDimensionPixelSize(R.dimen.inner_content_width);
                        Log.w("picasso",  "transform");
                        return fitSizeBitmap(source, availableSpace, maxWidth);
                    }

                    @Override
                    public String key() {
                        return "resized";
                    }
                })
            .into(target);
    }*/


    public static Bitmap modifyOrientation(Bitmap bitmap, String image_absolute_path) throws IOException {
        ExifInterface ei = new ExifInterface(image_absolute_path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}

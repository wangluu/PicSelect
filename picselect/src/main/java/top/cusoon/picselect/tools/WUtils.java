package top.cusoon.picselect.tools;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.graphics.drawable.DrawableCompat;

/**
 * Created by l.wang on 2016/6/29.
 */
public class WUtils
{
    /**
     * 4.4后期推荐使用{@link DocumentsContract},uri格式与以前不一样
     *
     * @param context
     * @param contentUri
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPathByUri(Context context, Uri contentUri)
    {
        String filePath = null;
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                    && DocumentsContract.isDocumentUri(context, contentUri))
            {
                String wholeID = DocumentsContract.getDocumentId(contentUri);
                String id = wholeID.split(":")[1];
                String[] column = { MediaStore.Images.Media.DATA };
                String sel = MediaStore.Images.Media._ID + "=?";
                Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        column, sel, new String[] { id }, null);
                int columnIndex = cursor.getColumnIndex(column[0]);
                if (cursor.moveToFirst())
                {
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
            } else
            {
                String[] projection = { MediaStore.Images.Media.DATA };
                Cursor cursor = context.getContentResolver().query(contentUri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                filePath = cursor.getString(column_index);
            }
        } catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        return filePath;
    }


    /**
     * tint 着色
     * @param drawable
     * @param colors
     * @return
     */
    public static Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, colors);
        return wrappedDrawable;
    }

}

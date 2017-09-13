package top.cusoon.picselect.tools;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import top.cusoon.picselect.album.ImageBucket;
import top.cusoon.picselect.album.ImageItem;

/**
 * Created by l.wang on 2016/6/28.
 */
public class AlbumTools
{

    public static List<ImageBucket> buildImagesBuckets(ContentResolver cr) {
        long startTime = System.currentTimeMillis();
        List<ImageBucket> buckets = new ArrayList<>();
        HashMap<String,ImageBucket> bucketMap = new HashMap<>();
        // 构造缩略图索引
        HashMap<String,String> thumbnailMap = getThumbnailList(cr);
        if(thumbnailMap == null)
        {
            thumbnailMap = new HashMap<>();
        }
        ImageBucket bucketAll = new ImageBucket();
        bucketAll.bucketName = "全部图片";
        bucketAll.imageList = new ArrayList<>();

        // 构造相册索引
        String columns[] = new String[] { MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.PICASA_ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.SIZE, MediaStore.Images.Media.BUCKET_DISPLAY_NAME , MediaStore.Images.Media.DATE_MODIFIED};
        // 得到一个游标
        Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null,
                MediaStore.Images.Media.DATE_MODIFIED+" desc");
        if (cur.moveToFirst()) {
            // 获取指定列的索引
            int photoIDIndex = cur.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int photoPathIndex = cur.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int photoNameIndex = cur.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int photoTitleIndex = cur.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
            int photoSizeIndex = cur.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
            int bucketDisplayNameIndex = cur
                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int bucketIdIndex = cur.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
            int picasaIdIndex = cur.getColumnIndexOrThrow(MediaStore.Images.Media.PICASA_ID);
            int modifydateIndex = cur.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);

            // 获取图片总数
            int totalNum = cur.getCount();
            File f =null;

            do {
                String _id = cur.getString(photoIDIndex);
                String name = cur.getString(photoNameIndex);
                String path = cur.getString(photoPathIndex);
                String title = cur.getString(photoTitleIndex);
                String size = cur.getString(photoSizeIndex);
                String bucketName = cur.getString(bucketDisplayNameIndex);
                String bucketId = cur.getString(bucketIdIndex);
                String picasaId = cur.getString(picasaIdIndex);
                long date = cur.getLong(modifydateIndex);

//                Log.i(AlbumTools.class.getSimpleName(), _id + ", bucketId: " + bucketId + ", picasaId: "
//                        + picasaId + " name:" + name + " path:" + path
//                        + " title: " + title + " size: " + size + " bucket: "
//                        + bucketName + "---");

                f = new File(path);
                if(!f.exists()|| f.isHidden())
                {
                    totalNum --;
                    ImageBucket bucket = bucketMap.get(bucketId);
                    if (bucket != null) {
                        bucket.count--;
                    }
                }else {
                    ImageBucket bucket = bucketMap.get(bucketId);
                    if (bucket == null) {
                        bucket = new ImageBucket();
                        bucketMap.put(bucketId, bucket);
                        bucket.bucketId = bucketId;
                        bucket.imageList = new ArrayList<ImageItem>();
                        bucket.bucketName = bucketName;
                    }
                    bucket.count++;
                    ImageItem imageItem = new ImageItem();
                    imageItem.imageId = _id;
                    imageItem.imagePath = path;
                    imageItem.modifyDate = date;
                    imageItem.thumbnailPath = thumbnailMap.get(_id);
                    bucket.imageList.add(imageItem);
                    bucketAll.imageList.add(imageItem);
                }
            } while (cur.moveToNext());
            bucketAll.count = totalNum;
            buckets.add(bucketAll);
            buckets.addAll(bucketMap.values());
            cur.close();
        }
        long endTime = System.currentTimeMillis();
        Log.i(AlbumTools.class.getSimpleName(), "use time: " + (endTime - startTime) + " ms");
        return buckets;
    }
    /**
     *
     * @param cr  ContentResolver
     * @return    map of  key({@link MediaStore.Images.Thumbnails#IMAGE_ID}) and value ({@link MediaStore.Images.Thumbnails#DATA})
     */
    private static HashMap<String, String> getThumbnailList(ContentResolver cr) {

        String[] projection = {MediaStore.Images.Thumbnails._ID, MediaStore.Images.Thumbnails.IMAGE_ID,
                MediaStore.Images.Thumbnails.DATA};
        Cursor cursor = cr.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection,
                null, null, null);
        return getThumbnailColumnData(cursor);
    }

    private static HashMap<String, String> getThumbnailColumnData(Cursor cur) {
        // 缩略图列表
        HashMap<String, String> thumbnailList = new HashMap<String, String>();
        if (cur.moveToFirst()) {
            int _id;
            int image_id;
            String image_path;
            int _idColumn = cur.getColumnIndex(MediaStore.Images.Thumbnails._ID);
            int image_idColumn = cur.getColumnIndex(MediaStore.Images.Thumbnails.IMAGE_ID);
            int dataColumn = cur.getColumnIndex(MediaStore.Images.Thumbnails.DATA);

            do {
                // Get the field values
                _id = cur.getInt(_idColumn);
                image_id = cur.getInt(image_idColumn);
                image_path = cur.getString(dataColumn);

                // Do something with the values.
                // Log.i(TAG, _id + " image_id:" + image_id + " path:"
                // + image_path + "---");
                // HashMap<String, String> hash = new HashMap<String, String>();
                // hash.put("image_id", image_id + "");
                // hash.put("path", image_path);
                // thumbnailList.add(hash);
                thumbnailList.put("" + image_id, image_path);
            } while (cur.moveToNext());
            cur.close();
        }
        return thumbnailList;
    }
}

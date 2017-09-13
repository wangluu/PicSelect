package top.cusoon.picselect.album;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 一个图片对象
 * 
 * @author Administrator
 * 
 */
public class ImageItem implements Parcelable
{
	public String imageId;
	public String thumbnailPath;
	public String imagePath;
	public long modifyDate;

	public ImageItem()
	{
	}

	protected ImageItem(Parcel in) {
		imageId = in.readString();
		thumbnailPath = in.readString();
		imagePath = in.readString();
		modifyDate = in.readLong();
	}

	public static final Creator<ImageItem> CREATOR = new Creator<ImageItem>()
	{
		@Override
		public ImageItem createFromParcel(Parcel in) {
			return new ImageItem(in);
		}

		@Override
		public ImageItem[] newArray(int size) {
			return new ImageItem[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(imageId);
		dest.writeString(thumbnailPath);
		dest.writeString(imagePath);
		dest.writeLong(modifyDate);
	}
}

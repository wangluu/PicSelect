package top.cusoon.picselect.album;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import top.cusoon.picselect.CommonAdapter;
import top.cusoon.picselect.R;
import top.cusoon.picselect.tools.WUtils;

public class DirAdapter extends CommonAdapter<ImageBucket>
{
	private int checkedItem;
	private int themeColor=-1;

	public DirAdapter(Context context, List<ImageBucket> l)
	{
		super(context, l);
	}

	public DirAdapter(Context context)
	{
		super(context);
	}

	public int getCheckedItem()
	{
		return checkedItem;
	}

	public void setCheckedItem(int checkedItem)
	{
		this.checkedItem = checkedItem;
	}

	@Override
	public int getItemLayoutRes(int position) {
		return R.layout.wl_lib_list_item_dir;
	}

	@Override
	public void convert(CommonAdapter.ViewHolder holder,
			ImageBucket item) {
		TextView count = (TextView) holder.findViewById(R.id.wl_lib_tv_count);
		ImageView image  = (ImageView) holder.findViewById(R.id.wl_lib_iv_image);
		holder.setText(R.id.wl_lib_tv_name,item.bucketName);
		count.setText(String.format(context.getString(R.string.wl_lib_pic_count), item.count ));

		image.setImageResource(R.drawable.wl_lib_p_no_pic);
		if (item.imageList != null && item.imageList.size() > 0)
		{
			String sourcePath = item.imageList.get(0).imagePath;
			if(holder.getPosition()==0 && item.imageList.size()>1)
			{
				sourcePath = item.imageList.get(1).imagePath;
			}

			//ImageLoader.getInstance().displayImage("file://" + sourcePath, image);
			Glide.with(context).load(sourcePath).placeholder(R.drawable.wl_lib_p_no_pic).sizeMultiplier(0.6f).into(image);
		}

		View checked = holder.findViewById(R.id.wl_lib_iv_checked);
		if(themeColor!=-1) {
			ColorStateList csl = new ColorStateList(new int[][]{{}}, new int[]{themeColor});
			checked.setBackgroundDrawable(WUtils.tintDrawable(checked.getBackground(), csl));
		}
		if (checkedItem == holder.getPosition())
		{
			checked.setVisibility(View.VISIBLE);
		} else
		{
			checked.setVisibility(View.INVISIBLE);
		}
		
	}

	public int getThemeColor() {
		return themeColor;
	}

	public void setThemeColor(int themeColor) {
		this.themeColor = themeColor;
	}
}

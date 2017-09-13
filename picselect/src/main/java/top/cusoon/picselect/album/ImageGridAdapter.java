package top.cusoon.picselect.album;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import top.cusoon.picselect.CommonAdapter;
import top.cusoon.picselect.R;


public class ImageGridAdapter extends CommonAdapter<ImageItem>
{
	private List<String> selectedImgs;
	private ImageGridAdapterListener listener;

	public ImageGridAdapter(Context context, List<ImageItem> l)
	{
		super(context, l);
	}

	public ImageGridAdapter(Context context)
	{
		super(context);
	}

	public ImageGridAdapterListener getListener()
	{
		return listener;
	}

	public void setListener(ImageGridAdapterListener listener)
	{
		this.listener = listener;
	}


	public List<String> getSelectedImgs() {
		return selectedImgs;
	}

	public void setSelectedImgs(List<String> selectedImgs) {
		this.selectedImgs = selectedImgs;
	}

	public interface ImageGridAdapterListener
	{
		void onCaptureClick();
		void onSelectClicked(int position, ImageView slc, View shadow, ImageItem item);
		void onImageClicked(int position);
	}

	@Override
	public int getItemLayoutRes(int position) {
		return R.layout.wl_lib_grid_item_album;
	}

	@Override
	public void convert(final CommonAdapter.ViewHolder holder,
			final ImageItem item) {
		final ImageView slc = (ImageView) holder.findViewById(R.id.wl_lib_iv_slc);
		final View shadow = holder.findViewById(R.id.wl_lib_v_shadow);
		ImageView image = (ImageView) holder.findViewById(R.id.wl_lib_iv);
		image.setImageResource(R.drawable.wl_lib_p_no_pic);

		//DrawableRequestBuilder thumbnail = Glide.with(context).load("file://"+item.thumbnailPath);
		if ("capture".equals(item.imageId))
		{
			slc.setVisibility(View.GONE);
			image.setImageResource(R.drawable.wl_lib_p_capture);
			image.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{

					if(listener!=null)
					{
						listener.onCaptureClick();
					}
				}
			});
		} else
		{
			Glide.with(context).load(item.imagePath).placeholder(new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{Color.rgb(0x50,0x50,0x50), Color.rgb(0x30,0x30,0x30)})).sizeMultiplier(0.6f).into(image);
			slc.setVisibility(View.VISIBLE);
			image.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v) {
					if(listener!=null)
					{
						listener.onImageClicked(holder.getPosition());
					}
				}
			});
			slc.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					listener.onSelectClicked(holder.getPosition(),slc,shadow,item);
				}
			});

			slc.setSelected(selectedImgs!=null&&selectedImgs.contains(item.imagePath));
			if (slc.isSelected())
			{
				shadow.setVisibility(View.VISIBLE);
			} else
			{
				shadow.setVisibility(View.GONE);
			}

		}
		
	}
}

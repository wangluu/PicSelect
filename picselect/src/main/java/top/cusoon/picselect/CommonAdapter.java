package top.cusoon.picselect;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public abstract class CommonAdapter<T> extends BaseAdapter
{
	protected List<T> data;
	protected Context context;

	public CommonAdapter(Context context) {
		this(context, new ArrayList<T>());
	}

	public CommonAdapter(Context context, List<T> data) {
		this.context = context;
		this.data = data;
	}

	@Override
	public int getCount() {
		return data == null ? 0 : data.size();
	}

	@Override
	public T getItem(int position) {
		if(data == null || position<0 ||position>=data.size())
		{
			return null;
		}
		
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = ViewHolder.get(context, convertView, parent,
				getItemLayoutRes(position), position);
		convert(holder, getItem(position));
		return holder.convertView;
	}

	/**
	 * 设置adapter item layout resource id
	 * 
	 * @return
	 */
	public abstract @LayoutRes
    int getItemLayoutRes(int position);

	/**
	 * 开发者实现该方法，进行业务处理
	 */
	public abstract void convert(ViewHolder holder, T item);

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public static class ViewHolder {
		private View convertView;
		private SparseArray<View> views;
		private int position;

		public ViewHolder(Context context, ViewGroup parent, int layoutId,
                          int position) {
			this.position = position;
			this.views = new SparseArray<View>();
			convertView = LayoutInflater.from(context).inflate(layoutId,
					parent, false);
			convertView.setTag(this);
		}

		public static ViewHolder get(Context context, View convertView,
                                     ViewGroup parent, int layoutId, int position) {
			if (convertView == null) {
				return new ViewHolder(context, parent, layoutId, position);
			} else {
				ViewHolder holder = (ViewHolder) convertView.getTag();
				holder.position = position;
				return holder;
			}
		}

		public View findViewById(int id) {
			View v = views.get(id);
			if (v == null) {
				v = convertView.findViewById(id);
				views.put(id, v);
			}
			return v;
		}

		public View getConvertView() {
			return convertView;
		}

		public ViewHolder setText(int viewResId, String text) {
			TextView tv = (TextView) findViewById(viewResId);
			tv.setText(text);
			return this;
		}

		public int getPosition() {
			return position;
		}

	}

}

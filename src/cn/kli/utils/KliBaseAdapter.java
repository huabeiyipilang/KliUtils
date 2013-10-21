package cn.kli.utils;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class KliBaseAdapter extends BaseAdapter {

	private List mDataList;
	private int mItemRes;
	private Context mContext;
	
	public KliBaseAdapter(Context context, List list){
		mDataList = list;
		mContext = context;
	}
	
	public void setItemViewInfo(int itemRes){
		mItemRes = itemRes;
	}
	
	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public Object getItem(int pos) {
		return mDataList.get(pos);
	}

	@Override
	public long getItemId(int id) {
		return id;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup arg2) {
		if(convertView == null){
			convertView = inflateItem();
			onViewTagCreate(convertView);
		}else{
			
		}
		return null;
	}

	abstract void onViewTagCreate(View root);
	
	private View inflateItem(){
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View root = inflater.inflate(mItemRes, null);
		return root;
	}
}

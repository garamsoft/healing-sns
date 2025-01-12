package com.example.test_andorid_grid_view_main;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MainGridViewAdapter extends BaseAdapter {
	
	Context mContext;
	ArrayList<MainGridItem> mGridItems;
	LayoutInflater mInflate;
	
	public MainGridViewAdapter(Context _Context, ArrayList<MainGridItem> _GridItems) {
		mContext = _Context;
		mGridItems = _GridItems;
		mInflate = LayoutInflater.from(mContext);
	}

	public int getCount() {
		return mGridItems.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mGridItems.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		MainGridItem gridItem = (MainGridItem)convertView;
		
		if(gridItem == null){
			gridItem = new MainGridItem(mContext);
		}
		gridItem.setData(R.drawable.ic_action_search, "test", position);
		
		return gridItem;
	}

}

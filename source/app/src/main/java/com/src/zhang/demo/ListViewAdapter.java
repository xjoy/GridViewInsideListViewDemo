package com.src.zhang.demo;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.src.zhang.demo.bean.ChannelGroup;
import com.src.zhang.demo.bean.ChannelItem;

/**
 * 
 * @author Administrator
 * 
 */
public class ListViewAdapter extends BaseExpandableListAdapter implements
		OnItemClickListener
{
	public static final int ItemHeight = 48;//
	public static final int PaddingLeft = 36;//
	private int myPaddingLeft = 0;

	private MyGridView toolbarGrid;

	private Context parentContext;

	private LayoutInflater layoutInflater;

    private ArrayList<ChannelGroup> treeNodes = new ArrayList<ChannelGroup>();


	public ListViewAdapter(Context view, int myPaddingLeft)
	{
		this(view,myPaddingLeft,new ArrayList<ChannelGroup>());
	}

    public ListViewAdapter(Context view,int myPaddingLeft,ArrayList<ChannelGroup> groups){
        parentContext = view;
        this.myPaddingLeft = myPaddingLeft;
        this.treeNodes = groups;
    }

	public ArrayList<ChannelGroup> GetNodes()
	{
		return treeNodes;
	}

    public void UpdateTreeNode(ArrayList<ChannelGroup> groups){
        this.treeNodes = groups;
    }

	public void RemoveAll()
	{
		treeNodes.clear();
	}

	public Object getChild(int groupPosition, int childPosition)
	{
		return treeNodes.get(groupPosition).getItems().get(childPosition);
	}

	public int getChildrenCount(int groupPosition)
	{
		return 1;//这里是一维数据，即每个组只有一个items数组
	}

	static public TextView getTextView(Context context)
	{
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ItemHeight);

		TextView textView = new TextView(context);
		textView.setLayoutParams(lp);
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		return textView;
	}

	/**
	 * ExpandableListView
	 */
    @Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent)
	{
		if (convertView == null)
		{
			layoutInflater = (LayoutInflater) parentContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.view, null);

			toolbarGrid = (MyGridView) convertView
					.findViewById(R.id.GridView_toolbar);
			toolbarGrid.setNumColumns(4);// 列数
			toolbarGrid.setGravity(Gravity.CENTER);// 居中
			toolbarGrid.setHorizontalSpacing(10);// 水平空间
			toolbarGrid.setAdapter(getMenuAdapter(getGroup(groupPosition)));// Adapter
			toolbarGrid.setOnItemClickListener(this);

		}

		return convertView;
	}

	/**
	 *
	 */
    @Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent)
	{

		TextView textView = getTextView(this.parentContext);
		textView.setText(getGroup(groupPosition).getGroup());
		textView.setPadding(myPaddingLeft + PaddingLeft, 0, 0, 0);

        //覆盖expandablelistview标题默认滴点击展开行为
        textView.setClickable(true);

		return textView;
	}

	public long getChildId(int groupPosition, int childPosition)
	{
		return childPosition;
	}

	public ChannelGroup getGroup(int groupPosition)
	{
		return treeNodes.get(groupPosition);
	}

	public int getGroupCount()
	{
		return treeNodes.size();
	}

	public long getGroupId(int groupPosition)
	{
		return groupPosition;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return true;
	}

	public boolean hasStableIds()
	{
		return true;
	}

	/**
	 * SimpleAdapter
	 * 
	 * @param item
	 * @return SimpleAdapter
	 */
	private SimpleAdapter getMenuAdapter(ChannelGroup item)
	{
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        ArrayList<ChannelItem> items = item.getItems();
		for (int i = 0; i < items.size(); i++)
		{
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", items.get(i).getIcon());
			map.put("itemText", items.get(i).getName());
			data.add(map);
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(parentContext, data,
				R.layout.item_menu, new String[] { "itemImage", "itemText" },
				new int[] { R.id.item_image, R.id.item_text });

        simperAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {

            @Override
            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                if (view instanceof ImageView) {
                    ImageView iv = (ImageView) view;
                    UIHelper.showLoadImage(iv,data.toString(),"Error loading image:"+data);
                    return true;
                }
                return false;
            }
        });

        return simperAdapter;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id)
	{
		Toast.makeText(parentContext, "click:" + position, Toast.LENGTH_SHORT)
				.show();

	}
}
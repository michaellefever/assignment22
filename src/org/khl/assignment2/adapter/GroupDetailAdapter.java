package org.khl.assignment2.adapter;

import java.util.List;

import model.Member;
import model.Facade.Facade;

import org.khl.assignment2.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupDetailAdapter extends BaseAdapter {

	private Activity activity;
	private List<Member> members;
	private static LayoutInflater inflater=null;
	private Facade facade;
	//		public ImageLoader imageLoader; 

	public GroupDetailAdapter(Activity a, List<Member> members, Facade facade) {
		activity = a;
		this.members=members;
		this.facade = facade;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//			imageLoader=new ImageLoader(activity.getApplicationContext());
	}

	public int getCount() {
		return members.size();
	}

	public Object getItem(int position) {
		return members.get(position);
	}

	public long getItemId(int position) {
		return members.get(position).getId();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if(convertView==null){
			view = inflater.inflate(R.layout.group_detail_row, null);
		}
		TextView memberName = (TextView)view.findViewById(R.id.memberName); // member
		TextView amountText = (TextView)view.findViewById(R.id.amount); // who owes who how much
		ImageView image = (ImageView)view.findViewById(R.id.list_image);

		// Setting all values in listview
			Member m = members.get(position);
			memberName.setText(m.toString());
			double amount = facade.getAmount(m.getId());
			String amountString ="";
			if(amount > 0){
				amountString = "owes you "+ amount;
				amountText.setTextColor(Color.GREEN);
			}else if(amount < 0){
				amount *= -1;
				amountString = "you owe " + amount;
				amountText.setTextColor(Color.RED);
			}else{
				amountString = "settled";
			}
			amountText.setText(amountString);
			//image.setImageBitmap(m.getExpenses().get(2).getPhoto());
		//song.get(CustomizedListView.KEY_ARTIST));
		//members.setText(song.get(CustomizedListView.KEY_DURATION));
		//imageLoader.DisplayImage(song.get(CustomizedListView.KEY_THUMB_URL), thumb_image);
		return view;
	}
}
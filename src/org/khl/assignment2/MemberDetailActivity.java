package org.khl.assignment2;

import java.util.ArrayList;

import org.khl.assignment2.adapter.MemberDetailAdapter;

import db.DBWriter;
import model.Expense;
import model.Member;
import model.Facade.Facade;
import model.Facade.FacadeImpl;
import model.observer.Observer;
import service.FetchData;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class MemberDetailActivity extends ListActivity implements  OnItemClickListener, Observer{
	
	private TextView memberName, joinedOn, totalBalance;
	private int memberid;
	private Member member;
	private FetchData fetchData;
	private Facade facade;
	private MemberDetailAdapter memberDetailAdapt;
	private ListView listView;
	private DBWriter dbWriter;
	public static final String EXPENSE_ID = "expenseid", MEMBER_ID="memberId";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_member_detail);
		Bundle b = getIntent().getExtras();
        memberid = b.getInt(GroupDetailActivity.MEMBER_ID);
        fetchData = new FetchData(this.getApplicationContext());
        String dbWriterType = (fetchData.isConnected()? "OnlineDBWriter": "OfflineDBWriter");
		facade = new FacadeImpl(dbWriterType);
		dbWriter = facade.getDBWriter();
		dbWriter.addObserver(this);
		initializeComponents();
	}

	private void initializeComponents(){
		member = facade.getMembers().get(memberid);
		memberName = (TextView)findViewById(R.id.member);
		joinedOn = (TextView)findViewById(R.id.joinedOn);
		totalBalance = (TextView)findViewById(R.id.totalBalance);
		listView = (ListView)findViewById(android.R.id.list);
		memberDetailAdapt = new MemberDetailAdapter(this, new ArrayList<Expense>(member.getExpenses().values()), facade, memberid);
		listView.setAdapter(memberDetailAdapt);
		listView.setOnItemClickListener(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.member_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if(id == R.id.menu_groups){
			Intent intent = new Intent(MemberDetailActivity.this, MainActivity.class);
			startActivity(intent);
			return true;
		}else if(id == R.id.menu_create_group){
			Intent intent = new Intent(MemberDetailActivity.this, CreateGroupActivity.class);
			startActivity(intent);
			return true;
		}else if(id == R.id.menu_invitations){
			return true;
		}else if (id == R.id.action_settings) {
			Intent intent = new Intent(MemberDetailActivity.this, SettingsActivity.class);
			startActivity(intent);
			return true;
		} 
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Expense expense = (Expense) parent.getItemAtPosition(position);
		Intent intent = new Intent(MemberDetailActivity.this, ExpenseDetailActivity.class);
		intent.putExtra(EXPENSE_ID, expense.getId());
		startActivity(intent);
	}

	@Override
	public void update() {
		memberDetailAdapt = new MemberDetailAdapter(this, new ArrayList<Expense>(member.getExpenses().values()), facade, memberid);
		memberDetailAdapt.notifyDataSetChanged();
		listView.setAdapter(memberDetailAdapt);
		
	}
}

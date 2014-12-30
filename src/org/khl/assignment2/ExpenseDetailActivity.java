package org.khl.assignment2;

import org.khl.assignment2.adapter.MemberDetailAdapter;

import db.DBWriter;
import model.Expense;
import model.Member;
import model.Facade.Facade;
import model.Facade.FacadeImpl;
import model.observer.Observer;
import service.FetchData;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ExpenseDetailActivity extends Activity implements Observer {
	
	private TextView member, amount, date, description;
	private int expenseid;
	private FetchData fetchData;
	private Facade facade;
	private MemberDetailAdapter memberDetailAdapt;
	private DBWriter dbWriter;
	public static final String EXPENSE_ID = "expenseid";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_expense_detail);
		Bundle b = getIntent().getExtras();
        expenseid = b.getInt(MemberDetailActivity.EXPENSE_ID);
        fetchData = new FetchData(this.getApplicationContext());
        String dbWriterType = (fetchData.isConnected()? "OnlineDBWriter": "OfflineDBWriter");
		facade = new FacadeImpl(dbWriterType);
		dbWriter = facade.getDBWriter();
		dbWriter.addObserver(this);
		initializeComponents();
	}

	private void initializeComponents(){
		member = (TextView)findViewById(R.id.member);
		amount = (TextView)findViewById(R.id.amount);
		date = (TextView)findViewById(R.id.date);
		description = (TextView)findViewById(R.id.description);
	}
	
	
	private void fetchNewData(){
		fetchData = new FetchData(this.getApplicationContext());
		fetchData.execute();
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
			Intent intent = new Intent(ExpenseDetailActivity.this, MainActivity.class);
			startActivity(intent);
			return true;
		}else if(id == R.id.menu_create_group){
			Intent intent = new Intent(ExpenseDetailActivity.this, CreateGroupActivity.class);
			startActivity(intent);
			return true;
		}else if(id == R.id.menu_invitations){
			return true;
		}else if (id == R.id.action_settings) {
			Intent intent = new Intent(ExpenseDetailActivity.this, SettingsActivity.class);
			startActivity(intent);
			return true;
		} 
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}
}

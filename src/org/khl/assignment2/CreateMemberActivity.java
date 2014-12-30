package org.khl.assignment2;

import model.Member;
import model.Facade.Facade;
import model.Facade.FacadeImpl;
import service.FetchData;
import service.Validator;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class CreateMemberActivity extends Activity{
	private Facade facade;
	private EditText firstnameText, lastnameText, emailText;
	private FetchData fetchData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_member);
		fetchData = new FetchData(this.getApplicationContext());
		String dbWriterType = (fetchData.checkIfConnected()? "OnlineDBWriter": "OfflineDBWriter");
		facade = new FacadeImpl(dbWriterType);
		initializeComponents();
	}

	private void initializeComponents(){
		firstnameText = (EditText)findViewById(R.id.firstname);
		lastnameText = (EditText)findViewById(R.id.lastname);
		emailText = (EditText)findViewById(R.id.email);
	}

	public void create(View v){
		String firstname = firstnameText.getText().toString();
		String lastname = lastnameText.getText().toString();
		String email = emailText.getText().toString();
		if(Validator.isValidEmailAddress(email)){
			Member member = new Member(firstname, lastname , email);
			facade.createMember(member);
			finish();
			}else{
				emailText.setError(getString(R.string.error_email));
			}
	}

	public void cancel(View v){
		finish();
	}
}

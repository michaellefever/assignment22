package db;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.util.Log;

import model.Expense;
import model.Group;
import model.Member;
import model.Settings;
import model.observer.Observer;

public class OfflineDBWriter implements DBWriter {

	private MemberDB memberDB;
	private GroupDB groupDB;
	private Settings settings;
	private ArrayList<Observer> observers;

	public OfflineDBWriter() {
		observers = new ArrayList<Observer>();
		memberDB = MemberDB.getInstance();
		groupDB = GroupDB.getInstance();
		settings = Settings.getInstance();
	}
	
	@Override
	public void closeConnection() {
		//NOTHING
	}
	
	@Override
	public void writeSettings(Settings settings) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void writeMember(Member member) {
		Log.v("bram", memberDB.getMembers().size()+"");
		memberDB.addMember(member);
		Log.v("bram", memberDB.getMembers().size()+"");
		notifyObservers();
	}

	@Override
	public void writeGroup(Group group) {
		groupDB.addGroup(group);
		notifyObservers();
	}

	@Override
	public Map<Integer, Group> getGroups() {
		return groupDB.getGroups();
	}

	@Override
	public void writeExpense(Expense expense, List<Member> recipients) {
		Member me = memberDB.getMembers().get(settings.getCurrentMember().getId());
		me.addExpense(expense);
		notifyObservers();
	}

	@Override
	public void updateGroup(int groupId, String groupName, List<Member> membersInvited) {
		groupDB.addGroup(new Group(groupId, groupName, membersInvited));
		notifyObservers();
	}

	@Override
	public void notifyObservers() {
		for(Observer o : observers){
			o.update();
		}
	}

	@Override
	public void addObserver(Observer o) {
		observers.add(o);
	}

	@Override
	public void clearDatabase() {}

	@Override
	public void writeMembers(List<Member> members) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeGroups(List<Group> groups) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeExpenses(List<Expense> expenses) {
		// TODO Auto-generated method stub	
	}

	@Override
	public Map<Integer, Member> getMembers() {
		return memberDB.getMembers();
	}

	@Override
	public Member getMemberForId(int id) {
		// TODO Auto-generated method stub
		return null;
	}
}

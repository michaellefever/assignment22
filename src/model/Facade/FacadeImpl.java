package model.Facade;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import android.content.Context;

import service.StoreData;
import service.XMLWriter;

import model.Expense;
import model.Group;
import model.Member;
import model.Settings;
import db.DBWriter;
import db.DBWriterFactory;
import db.GroupDB;
import db.MemberDB;
import db.DBFacade;
import db.DBFacadeImpl;

public class FacadeImpl implements Facade {
	private DBFacade dbFacade;
	private MemberDB memberDB = MemberDB.getInstance();
	private GroupDB groupDB = GroupDB.getInstance();
	private Settings settings = Settings.getInstance();
	private DBWriter writer;

	public FacadeImpl(String dbWriterType) {
		writer = DBWriterFactory.createDBWriter(dbWriterType);
		dbFacade = new DBFacadeImpl(writer);
	}

	public DBWriter getDBWriter() {
		return writer;
	}

	@Override
	public void createGroup(Group group) {
		dbFacade.writeGroup(group);
	}

	@Override
	public void updateGroup(int groupId, String groupName,
			List<Member> membersInvited) {
		dbFacade.updateGroup(groupId, groupName, membersInvited);
	}

	@Override
	public Member getCurrentMember() {
		return memberDB.getCurrMember();
	}

	@Override
	public Map<Integer, Member> getMembers() {
		return memberDB.getMembers();
	}
	
	public Member getMemberForId(int id){
		return dbFacade.getMemberForId(id);
	}

	@Override
	public Map<Integer, Member> getMembersOnline() {
		return dbFacade.getMembers();
	}

	public List<Member> getMembersInGroup(int groupId) {
		return groupDB.getMembersInGroup(groupId);
	}

	@Override
	public void settlePayments() {
		// TODO Auto-generated method stub
	}

	@Override
	public Map<Integer, Group> getGroups() {
		return groupDB.getGroups();
	}

	@Override
	public Map<Integer, Group> getGroupsOnline() {
		return dbFacade.getGroups();
	}

	public void writeExpense(Expense expense, List<Member> recipients) {
		dbFacade.writeExpense(expense, recipients);
	}

	public double getAmountIPaidMember(int memberid) {
		Map<Integer, Member> members = getMembers();
		double amount = 0;
		Member me = members.get(settings.getCurrentMember().getId());
		for (Expense e : me.getExpenses().values()) {
			if (e.getMembersPaidFor().contains(memberid)) { // I paid member
				amount += e.getAmount() / e.getMembersPaidFor().size();
			}
		}
		return amount;
	}

	public double getAmountMemberPaidMe(int memberid) {
		Map<Integer, Member> members = getMembers();
		double amount = 0;
		Member member = members.get(memberid);
		Member me = members.get(settings.getCurrentMember().getId());
		for (Expense e : member.getExpenses().values()) {
			if (e.getMembersPaidFor().contains(me.getId())) { // member paid me
				amount += e.getAmount() / e.getMembersPaidFor().size();
			}
		}
		return amount;
	}

	public double getAmount(int memberid) {
		return getAmountIPaidMember(memberid) - getAmountMemberPaidMe(memberid);
	}

	@Override
	public void clearDatabase() {
		dbFacade.clearDatabase();
	}

	public void writeMembers(List<Member> members) {
		dbFacade.writeMembers(members);
	}

	public void writeGroups(List<Group> groups) {
		dbFacade.writeGroups(groups);
	}

	public void writeExpenses(List<Expense> expenses) {
		dbFacade.writeExpenses(expenses);
	}

	@Override
	public boolean checkIfNewDataAvailable() {
		return true;
	}

	@Override
	public void createMember(Member member) {
		dbFacade.writeMember(member);
	}

	public void writeSettings(Context context, Settings settings) {
		StoreData storeData = new StoreData(context, null, null);
		dbFacade.writeSettings(settings);
		storeData.saveSettings(settings);
	}
}

package db;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import model.Expense;
import model.Group;
import model.Member;
import model.Settings;
import model.observer.Observer;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.util.Log;

public class OnlineDBWriter implements DBWriter {

	private Connection connection;
	private PreparedStatement statement;
	private Properties connectionProperties;
	private ArrayList<Observer> observers = new ArrayList<Observer>();
	private volatile static DBWriter instance;
	private MemberDB memberDB = MemberDB.getInstance();
	private GroupDB groupDB = GroupDB.getInstance();
	private Settings settings = Settings.getInstance();
	private static final String JDBC_DRIVER = "org.postgresql.Driver";
	private static final String DB_URL = "jdbc:postgresql://gegevensbanken.khleuven.be:51415/probeer";

	private OnlineDBWriter(){
		if (android.os.Build.VERSION.SDK_INT >= 9) {														//Schijnt kan SDK <= 9 geen HTTP connecties maken
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		try {
			Class.forName(JDBC_DRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		initializeProperties();
		try {
			connection = DriverManager.getConnection(DB_URL,connectionProperties);
			connection.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static DBWriter getInstance(){
		if(instance == null){
			synchronized(GroupDB.class){
				if(instance == null){
					instance = new OnlineDBWriter();
				}
			}
		}
		return instance;
	}

	private void initializeProperties(){
		connectionProperties =new Properties();
		connectionProperties.setProperty("user", "r0265929");
		connectionProperties.setProperty("password", "Zy5xfx10");
		connectionProperties.setProperty("ssl", "true");
		connectionProperties.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
	}

	public void writeMembers(List<Member> members){
		try {
			statement = connection.prepareStatement("INSERT INTO member (firstname, lastname, email) VALUES(?,?,?)");

			for (int i = 0; i < members.size(); i++) {
				Member member = members.get(i);
				statement.setString(1, member.getFirstName());
				statement.setString(2, member.getLastName());
				statement.setString(3, member.getEmail());
				statement.addBatch();
				if ((i + 1) % 1000 == 0) {
					statement.executeBatch(); // Execute every 1000 items.
				}
			}
			statement.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void writeMember(Member member) {
		ResultSet set = null;
		try {
			statement = connection.prepareStatement("SELECT * FROM member WHERE firstname = '" + member.getFirstName()
					+ "' AND lastname ='" + member.getLastName() + "'");
			set = statement.executeQuery();
			if(!set.next()) { 																		//member bestaat nog niet
				statement = connection.prepareStatement("INSERT INTO member (firstname, lastname, email) VALUES(?,?,?)");
				statement.setString(1, member.getFirstName());
				statement.setString(2, member.getLastName());
				statement.setString(3, member.getEmail());
				statement.executeUpdate();
				memberDB.addMember(member);
				notifyObservers();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Map<Integer, Member> getMembers() {
		Map<Integer, Member> members = new HashMap<Integer, Member>();
		try {
			statement = connection.prepareStatement("SELECT * FROM member");
			ResultSet set = statement.executeQuery();

			for(@SuppressWarnings("unused")int i = 0; set.next(); i++) {
				members.put(set.getInt(1), new Member(set.getInt(1), set.getString(2), set.getString(3), set.getString(4), getExpensesOfMember(set.getInt(1))));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try{
				statement.close();
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
		return members;
	}

	public Group getGroupForId(int id){
		Group group = null;
		try {
			statement = connection.prepareStatement("SELECT * FROM groups WHERE id= '" + id +"'");
			ResultSet set = statement.executeQuery();
			for(@SuppressWarnings("unused")int i = 0; set.next(); i++) {
				group = new Group(set.getInt(1), set.getString(2), getMembersInGroup(id));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return group;
	}
	
	public Member getMemberForId(int id){
		Member member = null;
		try {
			statement = connection.prepareStatement("SELECT * FROM expense WHERE id= '" + id +"'");
			ResultSet set = statement.executeQuery();
			for(@SuppressWarnings("unused")int i = 0; set.next(); i++) {
				member = new Member(set.getInt(1), set.getString(2), set.getString(3), set.getString(4));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return member;
	}

	private Group getGroupForName(String name){
		Group group = null;
		try {
			statement = connection.prepareStatement("SELECT * FROM groups WHERE groupname= '" + name +"'");
			ResultSet set = statement.executeQuery();

			for(@SuppressWarnings("unused")int i = 0; set.next(); i++) {
				group = new Group(set.getInt(1), set.getString(2));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return group;
	}

	public List<Member> getMembersInGroup(int groupid){
		List<Member> members = new ArrayList<Member>();
		try {
			statement = connection.prepareStatement("SELECT member.id, firstname, lastname, email FROM group_member " +
					"INNER JOIN groups ON group_member.groupid = '" + groupid 
					+"' INNER JOIN member ON group_member.memberid = member.id GROUP BY member.id");
			ResultSet set = statement.executeQuery();

			for(@SuppressWarnings("unused")int i = 0; set.next(); i++) {
				members.add(new Member(set.getInt(1), set.getString(2), set.getString(3), set.getString(4)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try{
				statement.close();
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
		return members;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public PreparedStatement getStatement() {
		return statement;
	}

	public void setStatement(PreparedStatement statement) {
		this.statement = statement;
	}

	public void closeConnection(){
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] compressImage(Bitmap bmp){
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
		return stream.toByteArray();
	}
/*
	@Override
	public void writeExpense(Expense expense, List<Member> recipients){
		try {
			statement = connection.prepareStatement("INSERT INTO expense (senderid, amount, expensedate, description, groupid) VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, expense.getSenderId());
			statement.setDouble(2, expense.getAmount());
			statement.setString(3, expense.getDate());
			statement.setString(4, expense.getDescription());
			statement.setInt(5, expense.getGroupId());
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		ResultSet set;
		try {
			set = statement.getGeneratedKeys();
			if(set.next()){
				writeExpenseReceivers(set.getInt(1), recipients);
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}*/
	
	@Override
	public void writeExpense(Expense expense, List<Member> recipients){
		int senderid = getIdCurrentMember();
		try {
			statement = connection.prepareStatement("INSERT INTO expense (senderid, amount, expensedate, description, groupid, image) VALUES(?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, senderid);
			statement.setDouble(2, expense.getAmount());
			statement.setString(3, expense.getDate());
			statement.setString(4, expense.getDescription());
			statement.setInt(5, expense.getGroupId());
			if (expense.getPhoto() != null){
			Log.v("bytea", compressImage(expense.getPhoto()) + "");
			statement.setBytes(6, compressImage(expense.getPhoto()));
			}
			statement.executeUpdate();
			memberDB.getCurrMember().addExpense(expense);
			notifyObservers();
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		ResultSet set;
		try {
			set = statement.getGeneratedKeys();
			if(set.next()){
				writeExpenseReceivers(set.getInt(1), recipients);
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void writeExpenses(List<Expense> expenses){
		Map<Integer,Member> membersMap = getMembers();
		ArrayList<Member> members = new ArrayList<Member>();
		for(Expense e : expenses){
			for(int memberid : e.getMembersPaidFor()){
				members.add(membersMap.get(memberid));
			}
			writeExpense(e, members);
		}
	}

	private void writeExpenseReceivers(int expenseid, List<Member> recipients) {
		try {
			for(int i = 0; i < recipients.size(); i++){
				statement = connection.prepareStatement("INSERT INTO expense_receiver (expenseid, receiverid) VALUES(?,?)");
				statement.setInt(1, expenseid);
				statement.setInt(2, recipients.get(i).getId());
				statement.executeUpdate();
			}
			notifyObservers();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private Set<Integer> getExpenseReceivers(int expenseid){
		Set<Integer> receivers = new HashSet<Integer>();
		try {
			statement = connection.prepareStatement("SELECT receiverid FROM expense_receiver WHERE expenseid ='" + expenseid +"'");
			ResultSet set = statement.executeQuery();

			for(@SuppressWarnings("unused")int i = 0; set.next(); i++) {
				receivers.add(Integer.valueOf((set.getInt(1))));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try{
				statement.close();
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
		return receivers;
	}

	public void updateGroup(int groupid, String groupname, List<Member> members){
		try {
			statement = connection.prepareStatement ("UPDATE groups SET groupname = '" + groupname + "' WHERE id='"+ groupid + "'");
			statement.executeUpdate();
			notifyObservers();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			updateGroupMembers(groupid, members);
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		notifyObservers();
	}

	private void updateGroupMembers(int groupid, List<Member> members){
		try {
			statement = connection.prepareStatement("DELETE FROM group_member WHERE groupid='" + groupid + "'");
			statement.executeUpdate();
			for(int i = 0; i < members.size(); i++){
				statement = connection.prepareStatement("INSERT INTO group_member (groupid, memberid) VALUES(?,?)");
				statement.setInt(1, groupid);
				statement.setInt(2, members.get(i).getId());
				statement.executeUpdate();
			}
			notifyObservers();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void writeGroup(Group group) {
		try {
			if(getGroupForName(group.getName()) == null) { 
				statement = connection.prepareStatement("INSERT INTO groups (groupname) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
				statement.setString(1, group.getName());
				statement.executeUpdate();
				groupDB.addGroup(group);
			}
			notifyObservers();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		ResultSet set;
		try {
			set = statement.getGeneratedKeys();
			if(set.next() && !group.getMembers().isEmpty()){
				writeGroupMembers(set.getInt(1), group.getMembers());			
			}
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void writeGroups(List<Group> groups){
		for(Group g : groups){
			writeGroup(g);
		}
	}

	private void writeGroupMembers(int groupid, List<Member> members){
		try {
			for(int i = 0; i < members.size(); i++){
				statement = connection.prepareStatement("INSERT INTO group_member (groupid, memberid) VALUES(?,?)");
				statement.setInt(1, groupid);
				statement.setInt(2, members.get(i).getId());
				statement.executeUpdate();
			}
			notifyObservers();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Bitmap decompressByteArray (byte[] bytea){
		Bitmap bitmap = BitmapFactory.decodeByteArray(bytea, 0, bytea.length);
		return bitmap;
	}

	public Map<Integer, Expense> getExpensesOfMember(int memberid){
		Map<Integer, Expense> expenses = new HashMap<Integer, Expense>();
		try {
			statement = connection.prepareStatement("SELECT expense.id, senderid, amount, expensedate, description, groupid, image FROM expense WHERE senderid ='" + memberid +"'");
			ResultSet set = statement.executeQuery();
/*
			for(@SuppressWarnings("unused")int i = 0; set.next(); i++) {
				expenses.put(set.getInt(1), new Expense(set.getInt(1), set.getInt(2),set.getDouble(3), set.getString(4), set.getString(5), getExpenseReceivers(set.getInt(1)), set.getInt(6)));
			}*/
			
			for(@SuppressWarnings("unused")int i = 0; set.next(); i++) {
				expenses.put(set.getInt(1), new Expense(set.getInt(1), set.getInt(2),set.getDouble(3), set.getString(4), set.getString(5), getExpenseReceivers(set.getInt(1)), set.getInt(6), decompressByteArray(set.getBytes(7))));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try{
				statement.close();
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
		return expenses;
	}

	@Override
	public Map<Integer, Group> getGroups() {
		Map<Integer, Group> groups = new HashMap<Integer, Group>();
		try {
			statement = connection.prepareStatement("SELECT * FROM groups");
			ResultSet set = statement.executeQuery();

			for(@SuppressWarnings("unused")int i = 0; set.next(); i++) {
				groups.put(set.getInt(1), new Group(set.getInt(1),set.getString(2), getMembersInGroup(set.getInt(1))));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try{
				statement.close();
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
		return groups;
	}
	
	@Override
	public void writeSettings(Settings settings) {
		try {
			Member m = settings.getCurrentMember();
			statement = connection.prepareStatement("INSERT INTO member (firstname, lastname, email) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, m.getFirstName());
			statement.setString(2, m.getLastName());
			statement.setString(3, m.getEmail());
			statement.executeUpdate();
			memberDB.addMember(m);
			ResultSet set = statement.getGeneratedKeys();
			if(set.next()){		
				statement = connection.prepareStatement("INSERT INTO settings (id, currency) VALUES(?,?)");
				statement.setInt(1, set.getInt(1));
				statement.setString(2, settings.getCurrency());
				statement.executeUpdate();
				notifyObservers();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private int getIdCurrentMember(){
		int id=-1;
		try {
			statement = connection.prepareStatement("SELECT * FROM settings");
			ResultSet set = statement.executeQuery();
			if(set.next()){		
				id = set.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}
	
	public Settings getSettings() {
		try {
			statement = connection.prepareStatement("SELECT * FROM settings");
			ResultSet set = statement.executeQuery();
			settings.setCurrency(set.getString(2));
			int memberid = set.getInt(1);
			String stmt = "SELECT * FROM member WHERE id = ?";
			statement = connection.prepareStatement(stmt);
			statement.setInt(1, memberid);
			set = statement.executeQuery();
			for(@SuppressWarnings("unused")int i = 0; set.next(); i++) {
				settings.setCurrentMember(new Member(set.getInt(1),set.getString(2),set.getString(3),set.getString(4)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try{
				statement.close();
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
		return settings;
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
	public void clearDatabase() {
		try {
			statement = connection.prepareStatement("TRUNCATE group_member, expense_receiver, groups, member, expense");
			statement.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

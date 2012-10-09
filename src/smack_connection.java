import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

public class smack_connection {
	public static String admin = "admin";
	public static String guest = "guest";
	public static String domain = "vopenfire";
	public static String host = "192.168.200.19"; 
	
	private ConnectionConfiguration config;
	private Connection connection;
	private String username;
	private String password;
	private String resource;
	private String oppo;
	private String target = null;
	private Chat chat;
	private List<Chat> chats = new ArrayList<Chat>();
	private RosterListener roster_listener;
	private MessageListener myMessageListener;
	
	private Counter receive_count;
	
	public smack_connection(){
	   	// Create the configuration for this new connection
		config = new ConnectionConfiguration( smack_connection.host , 5222 );
	   	config.setCompressionEnabled(true);
	   	config.setSASLAuthenticationEnabled(true);
	   	this.connection = new XMPPConnection(config);
	   	//XMPPConnection.DEBUG_ENABLED = true;
	}
	public boolean connect(){
	   	while(true){
		   	try {
				this.connection.connect();
				return true;
			} catch (XMPPException e1) {
				return false;
			}
	   	} 
	}
	public void init_after_connection(Counter receive_count){
		
		this.receive_count = receive_count;
		
    	build_roster_listener();
    	
		// create chat
		this.myMessageListener = new MessageListener() {
    	    public void processMessage(Chat chat, Message message) {
				if ( message.getType() == Message.Type.chat && message.getBody() != null ) {
					String str = "";
					str += "received " + addReceiveCount() + " ";
					str += message.getBody();
					str += " (";
					str += getResourceByFrom( message.getFrom() );
					str += " => ";
					str += getUser();
					str += ")";
					System.out.println( str );
				}
    	    }
    	};
    	this.chat = this.connection.getChatManager().createChat(this.oppo, this.myMessageListener);
	}
	public int addReceiveCount(){
		return this.receive_count.add();
	}
	public boolean login(String username, String password, String resource){
		this.username = username;
		this.password = password;
		this.resource = resource;
    	// Log into the server
    	try {
			this.connection.login(
				this.username ,
				this.password ,
				this.resource
			);
		} catch (XMPPException e1) {
	    	return false;
		}
    	if(this.username.equals(smack_connection.admin)){
    		this.oppo = smack_connection.guest + "@" + smack_connection.domain;
    	}
    	else{
    		this.oppo = smack_connection.admin + "@" + smack_connection.domain;
    	}
    	return true;
	}
	public String get_target(){
		return this.target;
	}
	public String get_oppo(){
		return this.oppo;
	}
	public void build_roster_listener(){
			
		this.roster_listener = new RosterListener() {
			// Ignored events public void entriesAdded(Collection<String> addresses) {}
		    public void entriesDeleted(Collection<String> addresses) {
		    }
		    public void entriesUpdated(Collection<String> addresses) {
		    }
		    public void presenceChanged(Presence presence) {
		    }
			@Override
			public void entriesAdded(Collection<String> arg0) {
			}
		};
		this.connection.getRoster().addRosterListener(roster_listener);
	}
	public String getUser(){
		return getResourceByFrom(this.connection.getUser());
	}
	String getResourceByFrom(String from){
		return from.substring(from.lastIndexOf("/") + 1);
	}
	void set_target(String resource){
    	this.target = resource;
	}
	public void send_message(String str){
		if( this.target != null){
			Message newMessage = new Message();
			newMessage.setBody(str);
			newMessage.setProperty("favoriteColor", "red");
			Chat this_chat;
			
			this_chat = getChatByTarget();
			if(this_chat == null){
				this_chat = this.connection.getChatManager().createChat( this.oppo + "/" + this.target , this.myMessageListener );
				this.chats.add(this_chat);
			}
			
			try {
				this_chat.sendMessage(newMessage);
			} catch (XMPPException e) {
				System.out.println("Send message failed!");
			}
		}
	}
	public Chat getChatByTarget(){
		for(Chat this_chat : this.chats){
			if(this_chat.getParticipant().equals(
					this.oppo + "@" + smack_connection.domain + "/" + this.target
				)
			){
				return this_chat;
			}
		}
		return null;
	}
	public void disconnect(){
		this.myMessageListener = null;
		this.connection.getRoster().removeRosterListener(this.roster_listener);
		this.connection.disconnect();
	}
}

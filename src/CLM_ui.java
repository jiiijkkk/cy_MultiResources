import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CLM_ui {
	private long num = 0;
	private long oppo_num = 0;
	private long message_num = 0;
	
	private Counter send_counter = new Counter();
	private Counter receive_counter = new Counter(); 
	
	private Scanner scanner = new Scanner( System.in );
	private smack_connection connection;
	private List<smack_connection> connections = new ArrayList<smack_connection>();
	
	public static void main(String[] args){
		CLM_ui ui = new CLM_ui();
		ui.start();
	}
	public void start(){
		
		this.connection = new smack_connection();
		
		if(this.try_connection()){
			boolean flag = true;
			while(flag){
				this.connection.disconnect();
				System.out.print("Please insert \"admin\" , \"guest\" or \"exit\" : ");
				String str = this.scanner.next();
				if(str.equals("admin")||str.equals("guest")){
					
					String oppo;
					if(str.equals("admin")){
						oppo = "guest"; 
					}
					else {
						oppo = "admin";
					}
					
					System.out.print("How many " + str + " to be created? ");
					this.num = this.scanner.nextInt();
					System.out.println("There are " + this.num + " of " + str + "." );
					
					System.out.print("How many " + oppo + " to be created? ");
					this.oppo_num = this.scanner.nextInt();
					System.out.println("There are " + this.oppo_num + " of " + oppo + "." );
					
					System.out.print("How many " + "messages" + " to be sent? ");
					this.message_num = this.scanner.nextInt();
					System.out.println("There are " + this.num*this.oppo_num*this.message_num + " of " + "messages" + "." );

					for( long i = 0 ; i < num ; i++ ){
						smack_connection this_connection = new smack_connection();
						connections.add(this_connection);
						this_connection.connect();
						this_connection.login(str, str, str + Long.toString(i) );
						this_connection.init_after_connection(this.receive_counter);
					}
					
					//	WAITING FOR NEXTLINE SIGNAL
					System.out.println("Press enter to send messages");
					this.scanner.nextLine();
					this.scanner.nextLine();
					
					//	SENDING MESSAGES

					for ( long j = 0 ; j < this.message_num ; j++ ){
						for( smack_connection this_connection : this.connections){
							for( long i = 0 ; i < this.oppo_num ; i++ ){
								this_connection.set_target( oppo + Long.toString(i) );
								this_connection.send_message( "message" + Long.toString(j) );
								String str_out = "";
								str_out += "sent " + this.send_counter.add();
								str_out += " (";
								str_out += this_connection.getUser();
								str_out += " => ";
								str_out += this_connection.get_target();
								str_out += ")";
								System.out.println( str_out );
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
					
					//	WAITING FOR NEXTLINE SIGNAL
					System.out.println("Press enter to logout");
					this.scanner.nextLine();
					
					for( smack_connection this_connection : this.connections){
						this_connection.disconnect();
					}
					
					flag = false;
				}
				else if(str.equals("exit"))flag = false; 
			}
		}
		System.out.println("Exit!");
	}
	public boolean try_connection(){
		int i = 5;
		boolean flag = false;
		while(!flag && i > 0){
			flag = this.connection.connect();
			if(flag)return true;
			System.out.println("Connection failed!");
			System.out.println("Reconnecting...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			i--;
		}
		return false;
	}
	public void connection_failed(){
		System.out.println("Connection failed!");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Reconnecting...");
	}
}

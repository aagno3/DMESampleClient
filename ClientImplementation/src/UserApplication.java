import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class UserApplication {

	//current client info
	public static int myPort;
	private static String name;
	public static String myIP = "127.0.0.1";
	
	// file server info
	public static int serverPort = 4000;
	public static String serverIP = "127.0.0.1";

    private static Socket socket            = null;
    private static DataOutputStream out     = null;
    private static DataInputStream in     = null;

	// all client info
	public static int[] portArray = { 2000, 3000,6000 };
	public static String[] ipArray = {"127.0.0.1", "127.0.0.1","127.0.0.1"};

	static DMEImplementation dmeImplementation = new DMEImplementation();

	static String messageToPost = null;
	
	static Thread thread;

	public static void main(String[] ag) {
		myPort = Integer.parseInt(ag[0]);
		name = ag[1];
		
		connectToServer();
		
		System.out.println("this is my port "+myPort);
		thread = new Thread(dmeImplementation);
		thread.start();
		
		Scanner scan = new Scanner(System.in);
		
		while (true) {
			
			String input = scan.nextLine();
			
			if(input == null)
				break;
			
			input = input.trim();
			
			int l =  input .length();
			String command = input.substring(0,4);
			
			if (command.equals("view")) {
				messageToPost = null;
				viewFromFile();
			}else if (command.equals("post") ) {
				
			
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");
				LocalDateTime now = LocalDateTime.now();
				
			    String msg = input.substring(5,l);
				messageToPost = dtf.format(now) + " " + name + " " + msg;
 
				System.out.println("this is post command "+messageToPost);
				dmeImplementation.requestToGetCriticalSection();

			} else {
				System.out.println("please use correct format and command, refer readme");
				return;
			}

		}
	}

   public static void connectToServer() {
	   try {
	       socket = new Socket(serverIP, serverPort);
	       out = new DataOutputStream(socket.getOutputStream());
	       in = new DataInputStream(socket.getInputStream());
	       System.out.println("Connected");
	   } catch(Exception e) {
		   
	   }
   }
   
   public static void disconnectToServer() {
	   try {
	       socket.close();
	       out.close();
	       in.close();
	       System.out.println("Disconnected");
	   } catch(Exception e) {
		   
	   }
   }
    
	public static void writeToFile() {
		System.out.println("written : "+messageToPost);
		try
        {
            out.writeUTF("post " +messageToPost);
            out.flush(); 
        }
        catch(UnknownHostException u)
        {
            System.out.println(u);
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
	}

	public static void viewFromFile() {
		try
        {
            out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("view");
            out.flush(); 
            
            String str = in.readUTF();  
            System.out.println(str);
        }
        catch(UnknownHostException u)
        {
            System.out.println(u);
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
	}

}

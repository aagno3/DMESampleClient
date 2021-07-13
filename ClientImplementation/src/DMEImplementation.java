import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

public class DMEImplementation implements Runnable {

	private static boolean search(LinkedList<Response> responses, Response r) {
		for (Response x : responses) {
			if (x.getNodeId() == r.getNodeId() && x.getTimeStamp() == r.getTimeStamp())
				return true;
		}
		return false;
	}

	private static void remove(LinkedList<Request> requests, Request r) {
		Iterator<Request> iterator = requests.iterator();
		while (iterator.hasNext()) {
			Request x = iterator.next();
			if (x.getNodeId() == r.getNodeId() && x.getTimeStamp() <= r.getTimeStamp()) {
				iterator.remove();
			}
		}
	}

	public static final int REQUEST = 0;
	public static final int RESPONSE = 1;
	public static final int RELEASE = 2;
	int timestamp = 1;

	LinkedList<Request> requests = new LinkedList<Request>();
	LinkedList<Response> responses = new LinkedList<Response>();
	LinkedList<Request> allRequests = new LinkedList<Request>();

	public void requestToGetCriticalSection() {
		allRequests.add(new Request(UserApplication.myPort, timestamp));
	}

	@Override
	public void run() {
		
		int nodes = UserApplication.portArray.length;
		boolean flag;
		byte buffer[] = new byte[1024];

		try {
			DatagramSocket ds = new DatagramSocket(UserApplication.myPort);

			Thread.sleep(1000);

			while (true) {
				DatagramPacket p = new DatagramPacket(buffer, 1024);
				ds.setSoTimeout(1000);
				flag = false;
				while (true) {
					try {
						ds.receive(p);
						String s = new String(p.getData(), 0, p.getLength());
						Scanner scan = new Scanner(s);
						int type = scan.nextInt();
						int node = scan.nextInt();
						int t = scan.nextInt();
						scan.close();
						
						if (t >= timestamp)
							timestamp = t + 1;

						switch (type) {
						case REQUEST:
							requests.add(new Request(node, t));
							String result = String.valueOf(RESPONSE) + " " + String.valueOf(UserApplication.myPort)
									+ " " + String.valueOf(timestamp);
							byte buf[] = result.getBytes();
							
							System.out.println("request received responsed send to "+node+ " at time stamp "+timestamp);
							ds.send(new DatagramPacket(buf, buf.length, InetAddress.getByName(UserApplication.myIP), node));
							break;
						case RESPONSE:
							System.out.println("responsed received from "+node+ " at time stamp "+t);
							responses.add(new Response(node, t));
							break;
						case RELEASE:
							remove(requests, new Request(node, t));
							break;
						}
					} catch (SocketTimeoutException e) {
						break;
					}
				}
				Request x;
				if (allRequests.peek() != null) {
					x = allRequests.peek();
					if (x.getTimeStamp() <= timestamp) {
						requests.add(new Request(UserApplication.myPort, timestamp));
						for (int i = 0; i < nodes; i++) {
							int port = UserApplication.portArray[i];
							if (port == UserApplication.myPort)
								continue;
							String result = String.valueOf(REQUEST) + " " + String.valueOf(UserApplication.myPort) + " "+ String.valueOf(timestamp);
							
							System.out.println("sending request to : "+result + " to port "+port);
							byte buf[] = result.getBytes();
							ds.send(new DatagramPacket(buf, buf.length,
									InetAddress.getByName(UserApplication.ipArray[i]), port));
						}
						allRequests.remove(x);
					}
				}
				if (requests.size() != 0) {
					x = requests.get(0);
					if (x.getNodeId() == UserApplication.myPort) {
						flag = true;
						for (int i = 0; i < nodes; i++) {
							if (UserApplication.portArray[i] == UserApplication.myPort)
								continue;
							int j;
							for (j = x.getTimeStamp(); j <= timestamp; j++)
								if (search(responses, new Response(UserApplication.portArray[i], j)))
									break;
							if (j > timestamp)
								flag = false;
						}
						if (flag) {
							// access critical section
							UserApplication.writeToFile();

							remove(requests, x);
							for (int i = 0; i < nodes; i++) {
								int port = UserApplication.portArray[i];
								if (port == UserApplication.myPort)
									continue;
								String result = String.valueOf(RELEASE) + " " + String.valueOf(UserApplication.myPort)
										+ " " + String.valueOf(timestamp);
								byte buf[] = result.getBytes();
								ds.send(new DatagramPacket(buf, buf.length,
										InetAddress.getByName(UserApplication.ipArray[i]), port));
							}
						}
					}

				}
				timestamp++;
				Thread.sleep(1000);
			}
		} catch (Exception e) {
            System.out.println(e.getMessage());
		}
	}
}

class Request {
	private int timestamp;
	private int nodeId;

	public Request(int nodeId, int timestamp) {
		this.timestamp = timestamp;
		this.nodeId = nodeId;
	}

	public int getNodeId() {
		return nodeId;
	}

	public int getTimeStamp() {
		return timestamp;
	}
}

class Response extends Request {
	public Response(int nodeId, int timestamp) {
		super(nodeId, timestamp);
	}
}

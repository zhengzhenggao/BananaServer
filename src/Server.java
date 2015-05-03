
import java.beans.XMLEncoder;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;


/**
 * Created by Gao Liangjie on 4/30/2015.
 */
public class Server {
	private static Hashtable<String, String> names;// = new Hashtable<String, String>();
	private static Hashtable<String, PrintWriter> writers = new Hashtable<String, PrintWriter>();

	public static void main(String[] args) throws IOException, FileNotFoundException, ClassNotFoundException {
		System.out.println("server start");

		ServerSocket serverSocket = new ServerSocket(7577);

		loadServerData();

		// int timeCount = 0;
		while (true) {
		
			Socket socket = serverSocket.accept();
			new Thread(new Handler(socket)).start();

			serverDataSaving(names);

		}

	}


	public static class Handler implements Runnable {
		Socket socket;
		BufferedReader in;
		PrintWriter out;
		private String message;

		private Handler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			loginProcedure();
		}


		private void loginProcedure(){
			try{
				System.out.println(socket.getInetAddress().getHostAddress() + " is connected...");
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);

				String status = in.readLine();
				String name = in.readLine();
				String password = in.readLine();

				if (status.equals("register")) {
					if (names.containsKey(name)) {
						System.out.println("your user name is already registered");
						out.println("your user name is already registered");
						socket.close();
						return;
					} else {
						names.put(name, password);
						System.out.println("log in successfully");
						out.println("log in successfully");
						in.close();
						out.close();
						socket.close();
						return;
					}
				} else if (status.equals("login")) {
					if (!names.containsKey(name)) {
						System.out.print("user name doesn't exist");
						out.println("user name doesn't exist");
						in.close();
						out.close();
						socket.close();
						return;
					} else if (!names.get(name).equals(password)) {
						System.out.println("wrong password");
						out.println("wrong password");
						in.close();
						out.close();
						socket.close();
						return;
					} else {
						System.out.println("log in successfully");
						out.println("log in successfully");
						in.close();
						out.close();
						socket.close();
						return;
					}
				} else if (status.equals("ready")) {
					System.out.print("ready");

					writers.put(name, out);
				}


				do {
					try {
						message = in.readLine();
						System.out.println(message);
						for (String n : writers.keySet()) {
							writers.get(n).println(name + ": " + message);
						}
					} catch (IOException e) {
						out.print("send it again");
					}

				} while (!message.equals("end"));

				in.close();
				out.close();
				socket.close();
			}
			catch(IOException e){
				out.print("send it again");
			}
		}
	}

	private static void serverDataSaving(Hashtable<String, String> tableName) throws IOException{
		FileOutputStream fos = new FileOutputStream("serverData.xml");
		XMLEncoder e = new XMLEncoder(fos);
		e.writeObject(tableName);
		e.close();

		fos = new FileOutputStream("serverData.tmp");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(tableName);
		oos.close();
		System.out.println("Server Data Saved!!!");

	}
	private static void loadServerData() throws IOException, ClassNotFoundException{
		String fileName="serverData.tmp";
		System.out.println("");
		try{
			FileInputStream fis = new FileInputStream(fileName);
			ObjectInputStream fos = new ObjectInputStream(fis);
			//Hashtable<String,String> temp =(Hashtable<String,String>) fos.readObject();
			names =(Hashtable<String,String>) fos.readObject();
		}catch(FileNotFoundException e)
		{
			names = new Hashtable<String,String>();
		}
	}
}


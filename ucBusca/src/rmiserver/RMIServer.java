/**
 * Raul Barbosa 2014-11-07
 */
package rmiserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Scanner;

public class RMIServer extends UnicastRemoteObject implements RMIServerInterface {
	private final int portServer = 8000;
	private final int portMulticast = 4000;
	private InetAddress address;
	private MulticastSocket socket;
	private boolean value;
	ArrayList queue;
	ArrayList loggedUsers;


	public RMIServer() throws IOException {
		super();
		socket = new MulticastSocket(portServer);
		address = InetAddress.getByName("224.0.224.0");
		socket.joinGroup(address);
		loggedUsers = new ArrayList();
	}

	public static void main(String args[]) throws IOException {
		Registry registry;
		Scanner sc = new Scanner(System.in);
		String IP;
		System.out.println("Insert the IP of the machine: ");
		System.out.print(">>> ");
		IP = sc.nextLine();
		RMIServer server = new RMIServer();
		try {
			registry = LocateRegistry.getRegistry(server.portServer);
			while (registry.lookup(IP + ":" + server.portServer) != null) {
				System.out.println("Waiting for Backup Server to crash.");
			}
		} catch (RemoteException re) {
			registry = LocateRegistry.createRegistry(server.portServer);
			registry.rebind(IP + ":" + server.portServer, server);
			System.out.println("Server ready.");
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public String connected() {
		return "\nWelcome to ucBusca!";
	}

	public String sendData(String data) {
		boolean clean = false;
		byte[] bufferSender, bufferReceiver;
		String dataReceived = null;
		DatagramPacket packetSender, packetReceiver;
		bufferSender = data.getBytes();
		packetSender = new DatagramPacket(bufferSender, bufferSender.length, address, portMulticast);
		if (!value) {
			try {
				bufferReceiver = new byte[99999];
				packetReceiver = new DatagramPacket(bufferReceiver, bufferReceiver.length);
				socket.send(packetSender);
				this.queue.remove(data);
				socket.setSoTimeout(30000);
				socket.receive(packetReceiver);
				socket.setSoTimeout(100);
				while (!clean) {
					try {
						dataReceived = new String(packetReceiver.getData(), 0, packetReceiver.getLength());
						socket.receive(packetReceiver);
						System.out.println(dataReceived);
					} catch (IOException x) {
						clean = true;
					}
				}
				return dataReceived;
			} catch (IOException e) {
				System.out.println("Failed to send request. Multicast servers are down!");
			}
		} else {
			try {
				socket.send(packetSender);
			} catch (IOException e) {
				System.out.println("Failed to send request. Multicast servers are down!");
			}
		}
		return null;
	}

	public boolean isLoggedIn(String user) {
		boolean value = false;
		if (!loggedUsers.isEmpty()) {
			for (Object logged : loggedUsers) {
				if (user.equals(logged)) {
					value = true;
				} else {
					value = false;
				}
			}
		}
		return value;
	}

	public ArrayList<String> getData(String user, String password, String type, String request) {
		String dataSent, dataReceived;
		System.out.println("YO BRO "+request);
		ArrayList<String> newData = null;
		queue = new ArrayList();
		switch (type) {
			case "login":  // Done
				dataSent = "type|login;user|" + user + ";password|" + password + ";";
				System.out.println("SENT: " + dataSent);
				value = false;
				this.queue.add(dataSent);
				dataReceived = sendData(dataSent);
				newData = dataHandler(dataReceived, type);
///                System.out.println(newData.get(1));
				if (newData.get(1).equals("Not found") || newData.get(1).equals("Wrong password")) {

					System.out.println("Login failed");
				} else {
					loggedUsers.add(newData.get(0));
				}
				break;

			case "register": // Done
				dataSent = "type|register;user|" + user + ";password|" + password + ";";
				System.out.println("SENT: " + dataSent);
				this.queue.add(dataSent);
				sendData(dataSent);
				break;

			case "newurl": // Done
				dataSent = "type|newurl;url|" + request + ";";
				System.out.println("SENT: " + dataSent);
				this.queue.add(dataSent);
				dataReceived = sendData(dataSent);
				newData = dataHandler(dataReceived, type);
				break;

			case "search": // Done
				newData = dataHandler(request, type);
				dataSent = "type|search;";
				System.out.println(user);
				if ((user != null))
					dataSent = dataSent.concat("user|" + user + ";");
				for (int i = 0; i < newData.size(); i++) {
					dataSent = dataSent.concat("word|" + newData.get(i) + ";");
				}
				newData.clear();
				System.out.println("SENT: " + dataSent);
				this.queue.add(dataSent);
				dataReceived = sendData(dataSent);
				System.out.println(dataReceived);
				newData = dataHandler(dataReceived, "handle");
				break;

			case "admin": // Done
				dataSent = "type|admin;";
				System.out.println("SENT: " + dataSent);
				this.queue.add(dataSent);
				dataReceived = sendData(dataSent);
				newData = dataHandler(dataReceived, type);
				break;

			case "giveadmin": // Done
				dataSent = "type|giveadmin;user|" + user + ";";
				System.out.println("SENT: " + dataSent);
				this.queue.add(dataSent);
				dataReceived = sendData(dataSent);
				newData = dataHandler(dataReceived, type);
				break;

			case "logout": // Done
				dataSent = "type|logout;user|" + user + ";";
				System.out.println("SENT: " + dataSent);
				value = true;
				this.queue.add(dataSent);
				sendData(dataSent);
				loggedUsers.remove(user);
				break;

			case "mysearch": // Done
				dataSent = "type|mysearch;user|" + user + ";";
				this.queue.add(dataSent);
				System.out.println("SENT: " + dataSent);
				dataReceived = sendData(dataSent);
				newData = dataHandler(dataReceived, type);
				break;

			case "connections":
				dataSent = "type|connections;user|" + user + ";" + "page|" + request + ";";
				System.out.println("SENT: " + dataSent);
				this.queue.add(dataSent);
				dataReceived = sendData(dataSent);
				newData = dataHandler(dataReceived, type);
				break;

			case "info":
				dataSent = "type|info;";
				System.out.println("SENT: " + dataSent);
				this.queue.add(dataSent);
				dataReceived = sendData(dataSent);
				newData = dataHandler(dataReceived, type);
				break;

		}
		System.out.println(loggedUsers);
		return newData;
	}

	public ArrayList<String> dataHandler(String data, String type) {
		ArrayList<String> newDataArray = new ArrayList<>();
		System.out.println(data);
		if (data == null) {
			return null;
		} else {
			if (type.equals("admin")) {
				String[] dataArray = data.split(";");
				for (int i = 0; i < dataArray.length / 2; i++) {
					String[] nome = dataArray[2 * i].split("\\|");
					String[] status = dataArray[1 + 2 * i].split("\\|");
					if (status[1].equals("true"))
						newDataArray.add("Admin user " + (i + 1) + ":" + nome[1]);
					else
						newDataArray.add("User " + (i + 1) + ":" + nome[1]);
				}
			}
			if (type.equals("login") || type.equals("newurl") || type.equals("mysearch") || type.equals("connections")) {
				String[] dataArray = data.split(";");
				for (int i = 0; i < dataArray.length / 2; i++) {
					String[] nome = dataArray[2 * i].split("\\|");
					String[] status = dataArray[1 + 2 * i].split("\\|");
					newDataArray.add(nome[1]);
					newDataArray.add(status[1]);
				}
			}
			if (type.equals("search")) {
				String[] dataArray = data.split(" ");
				for (int i = 0; i < dataArray.length; i++)
					newDataArray.add(dataArray[i]);
				System.out.println(newDataArray);
			}
			if (type.equals("handle")) {
				String[] dataArray = data.split(";");
				for (int i = 0; i < dataArray.length - 2; i++) {
					String[] info = dataArray[i + 2].split("\\|", 2);
					if (i % 3 == 0) {
						newDataArray.add("\nTitle: " + info[1]);
					}
					if (i % 3 == 1) {
						newDataArray.add("URL: " + info[1]);
					}
					if (i % 3 == 2) {
						newDataArray.add("Text: " + info[1]);
					}
				}
			}


			if (type.equals("giveadmin")) {
				String[] dataArray = data.split("\\|");
				newDataArray.add(dataArray[1]);
			}
			if (type.equals("info")) {
				String[] dataArray = data.split(";");
				for (int i = 0; i < dataArray.length - 3; i++) {
					String[] info = dataArray[i + 3].split("\\|", 2);
					if (i % 3 == 0) {
						newDataArray.add("\nTitle: " + info[1]);
					}
					if (i % 3 == 1) {
						newDataArray.add("URL: " + info[1]);
					}
					if (i % 3 == 2) {
						newDataArray.add("Text: " + info[1]);
					}
				}
			}

		}
		return newDataArray;
	}
}

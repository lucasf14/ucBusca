/**
 * Raul Barbosa 2014-11-07
 */
package model;

import rmiserver.RMIServerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

public class HeyBean {
    private RMIServerInterface server;
    private String username; // username and password supplied by the user
    private String password;
    private String message;
    private String IP = "localhost";
    private int port = 8000;

    public HeyBean() {
        try {
            server =  (RMIServerInterface) LocateRegistry.getRegistry(port).lookup(IP +":" + port);
            System.out.println(this.server.connected());
        }
        catch(NotBoundException| RemoteException e) {
            e.printStackTrace(); // what happens *after* we reach this line?
        }
    }

    public ArrayList mysearch(String username) throws RemoteException {
        return (ArrayList) this.server.getData(username,null,"mysearch",null);
    }

    public ArrayList checkAdmins() throws RemoteException{
        return (ArrayList) this.server.getData(null,null,"admin",null);
    }

    public ArrayList giveAdmin(String word) throws RemoteException {
        return (ArrayList) this.server.getData(word,null,"giveadmin",null);
    }

    public ArrayList indexNewUrl(String username, String word) throws RemoteException {
        return (ArrayList) this.server.getData(username,null,"newurl",word);
    }

    public String login(String username, String password) throws RemoteException {
        return (String) this.server.getData(username, password, "login", null).get(1);
    }

    public ArrayList search(String username,String password,String word) throws RemoteException {
        return (ArrayList) this.server.getData(username,password,"search",word);
    }

    public String register(String username, String password) throws RemoteException {
        if(!username.equals("") && !password.equals("")){
            this.server.getData(username,password,"register",null);
            return "Done";
        }
        return "Error";
    }





    //public ArrayList<String> getAllUsers() throws RemoteException {
    //eturn server.getAllUsers(); // are you going to throw all exceptions?
    //}

	/*public boolean getUserMatchesPassword() throws RemoteException {
		return server.userMatchesPassword(this.username, this.password);
	}*/


    public void setUsername(String username) {
        this.username = username;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

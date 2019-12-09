/**
 * Raul Barbosa 2014-11-07
 */
package rmiserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RMIServerInterface extends Remote {
	String connected() throws RemoteException;
	ArrayList getData(String user, String password, String type, String request) throws RemoteException;
	boolean isLoggedIn(String user) throws RemoteException;
}

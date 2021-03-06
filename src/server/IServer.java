package server;

import client.IClient;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;

/**
 * Remote server that registers user interfaces and notification systems..
 */
public interface IServer extends Remote {
    String getPublicKey()throws RemoteException;
    boolean registerUI(IClient client) throws RemoteException;
    boolean registerNotificationSystem(IClient client) throws RemoteException;
    boolean newUser(IClient client)throws RemoteException;
    boolean unregisterUI(IClient client)throws RemoteException;
    boolean unregisterNotificationSystem(IClient client)throws RemoteException;
}

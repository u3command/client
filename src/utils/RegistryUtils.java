package utils;


import client_impl.Client;
import client.IClient;
import server.IServer;
import journal.IJournalManager;

import javax.crypto.Cipher;
import javax.swing.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * Class for working with rmi registry and remotes objects.
 */
public class RegistryUtils {
    /**
     * Algorithm of encryption.
     */
    private static final java.lang.String ALGO = "RSA";
    /**
     * Rmi registry.
     */
    private static Registry registry;
    /**
     * Remote server for registration clients.
     */
    private static IServer server;
    /**
     * Client for registration on server.
     */
    private static IClient client;
    /**
     * Remote journal manager.
     */
    private static IJournalManager manager;
    /**
     * Searches remote server.
     */
    private static void getServerInstance() {
        getRegistryInstance();
        if(server == null)
        {
            try {
                server = (IServer) registry.lookup("IAuthorizationService");
            } catch (RemoteException  e) {

               JOptionPane.showMessageDialog(new JFrame(),"Cannot connect to server, try again later.");
                server = null;
            }
            catch (NotBoundException e)
            {
                JOptionPane.showMessageDialog(new JFrame(),"Cannot authorize, try again later.");
                server = null;
            }
        }
    }

    /**
     * Gets journal manager from {@link #registry} by specified login.
     * @param login login by which journal manager will be found in {@link #registry}.
     * @return journal manager.
     */
    public static IJournalManager getJournalManagerInstance(String login)
    {
        getRegistryInstance();
        if(manager == null)
        {
            try {
                manager = (IJournalManager) registry.lookup(login);
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(new JFrame(),"Cannot connect to server, try again later.");
                manager = null;
            }
            catch (NotBoundException e)
            {
                JOptionPane.showMessageDialog(new JFrame(),"Cannot find journal, try again later.");
            }
        }
        return manager;
    }

    /**
     * Intializes {@link #registry}.
     */
    private static void getRegistryInstance() {
        if(registry == null)
        {
            try {
                registry = LocateRegistry.getRegistry("localhost", 7777);
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(new JFrame(), Arrays.toString(e.getStackTrace()));
                registry = null;
            }
        }
    }

    /**
     * Registers client with specified login and password on server.
     * @param login login.
     * @param pass password.
     * @return true if registration is successful, else - false.
     * @throws RemoteException
     * @see RemoteException
     */
    public static boolean registerClient(String login, String pass) throws RemoteException {
        getServerInstance();
       // client = new Client(login, pass);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGO);
            byte[] public_key_bytes = Base64.getDecoder().decode(server.getPublicKey());

            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(public_key_bytes);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            String pass1 = encrypt(pass,publicKey);
            client = new Client(login, pass1);
        } catch (NoSuchAlgorithmException e) {
           // e.printStackTrace();
        } catch (InvalidKeySpecException e) {
          //  e.printStackTrace();
        }

        if(server != null) {
            return server.registerUI(client);
        }
        else {
            return false;
        }
    }
    /**
     * Unregister {@link #client}.
     */
    public static void unregisterClient()
    {
        getServerInstance();
        if(client != null)
        {
            try {
                server.unregisterUI(client);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Gets client's login.
     * @return login.
     */
    public static String getClientLogin() {
        try {
            return client.getLogin();
        } catch (RemoteException e) {
            return "";
        }
    }

    /**
     * Creates new user with specified login and password.
     * @param login login.
     * @param pass password.
     * @return true if new user was created, else - false.
     * @throws RemoteException
     */
    public static boolean newUser(String login, String pass) throws RemoteException {
        getServerInstance();
        if(server != null) {
            //return server.newUser(new Client(login, pass));
            try {
                KeyFactory keyFactory = KeyFactory.getInstance(ALGO);
                byte[] public_key_bytes = Base64.getDecoder().decode(server.getPublicKey());
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(public_key_bytes);
                PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
                String encrypted_pass = encrypt(pass,publicKey);
                return server.newUser(new Client(login, encrypted_pass));
            } catch (NoSuchAlgorithmException e) {
                //e.printStackTrace();
            } catch (InvalidKeySpecException e) {
               // e.printStackTrace();
            }
        }
        return false;
    }
    /**
     * Encrypts specified password with specified public key.
     * @param pass password.
     * @param key public key.
     * @return encrypted password.
     */
    private static String encrypt(String pass, PublicKey key) {
        byte[] cipherText = null;
        try {
            final Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(pass.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(cipherText);
    }
}

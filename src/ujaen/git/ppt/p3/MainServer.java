package ujaen.git.ppt.p3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Servidor TCP concurrente sencillo
 * Prácticas de Protocolos de Transporte
 * Grado en Ingeniería Telemática
 * Universidad de Jaén
 * 
 * @author Juan Carlos Cuevas Martínez
 */
public class MainServer {

    public static final String MSG_HANDSHAKE="Servidor HTTP/1.1 iniciándose...";

    private static ServerSocket mMainServer= null;
    
     
    public static void main(String[] args)  {
  
       // new Thread((Runnable) new Cliente("1")).start();
       
        try {
            mMainServer= new ServerSocket(80);
            System.out.println(MSG_HANDSHAKE);
            while(true) {
                Socket socket =mMainServer.accept();
                 System.out.println("Conexión entrante desde: "+socket.getInetAddress().toString());
                 Thread connection= new Thread(new HTTPSocketConnection(socket));
                 connection.start();
            }
        } catch (java.net.BindException ex) {
            System.err.println(ex.getMessage());
        } catch (IOException ex2){
            System.err.println(ex2.getMessage());
        }
    }  
}

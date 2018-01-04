package ujaen.git.ppt.p3;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Clase de atención de un servidor TCP sencillo
 * Prácticas de Protocolos de Transporte
 * Grado en Ingeniería Telemática
 * Universidad de Jaén
 * 
 * @author Juan Núñez Lerma / Pedro Javier Saéz Mira /
 *         Grupo 08
 */
public class HTTPSocketConnection implements Runnable {
    
    public static final String HTTP_Ok="200";
    
    private Socket mSocket=null;
    
    /**
     * Se recibe el socket conectado con el cliente
     * @param s Socket conectado con el cliente
     */
    public HTTPSocketConnection(Socket s){
        mSocket = s;
    }
    public void run() {
        //Declaración de las variables que vamos a usar
        Random r = new Random(System.currentTimeMillis());
        int n=r.nextInt();
        String request_line="";
        BufferedReader input;
        DataOutputStream output;
        FileInputStream input_file;
        String Server="",Allow="",Content_type="",Content_length="",Connection="";
       
        try {
            //aqui se declaran las variables que van a recoger los datos de entrada y salida del socket.
            byte[] outdata=null;
            String outmesg="";
            input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            output = new DataOutputStream(mSocket.getOutputStream());
            
            //do{
                Server="Servidor HTTP - Pedro / Juan"; //Este será el servidor usado, es decir el nuestro
                Allow="GET"; //El único método permitido es el GET                                                                   
                
                request_line= input.readLine();
                String parts[]=request_line.split(" ");
                
                if(request_line.startsWith("GET "))//Si la linea peticion tiene el metodo GET entramos al bucle y analizamos
                   //las cabeceras y demás, sino no entramos directamente.
                    
                {
                    String resourceFile="";
                    //Dividimos la peticion en partes y comprobamos si tiene 3 
                    //String parts[]=request_line.split(" ");
                    if(parts.length==3)
                    {
                      
                        //Analizamos las 3 partes de la petición
                        
                        if(parts[0].equalsIgnoreCase("GET")){//La primera parte(parts[0]) tiene que ser GET tal como ya hemos puesto antes                                   
                            //La segunda parte (parts[1]) tiene que ser el fichero index.html o la imagen.jpeg 
                            
                            //Si es el index.html o la "/" que por defecto tambien es el index.html mostramos un mensaje de OK 
                            //y le pasamos el recurso a abrir que será el index.html. La cabecera Content-Type sera logicamente text/html. 
                        //if(parts[1].equals("1") || parts[1].equals("1.1")){
                            if(parts[1].equalsIgnoreCase("/") || parts[1].equalsIgnoreCase("/index.html")){
                                outmesg="HTTP/1.1 200 OK \r\nContent-type:text/html\r\n\r\n";   
                                resourceFile="index.html";                                      
                                System.out.println("HTTP/1.1 200 OK");                          
                                Content_type="text/html";                                                
                            }                                                                   
                            //La otra opción es que sea la imagen.jpeg por tanto también mostramos un mensaje de OK 
                            //y le pasamos el recurso a abrir que será la imagen.jpeg La cabecera Content-Type sera logicamente image/jpeg
                            else if (parts[1].equalsIgnoreCase("/uja.jpg")){                                              
                                outmesg="HTTP/1.1 200 OK \r\nContent-type:image \r\n\r\n";      
                                resourceFile="uja.jpg";                                      
                                System.out.println("HTTP/1.1 200 OK");                          
                                Content_type="image/jpeg";                                                 
                            }                                                                   
                            
                            //Aquí llamamos a la funcion leerRecurso implementada despues para que nos lea el recurso anterior
                            
                            outdata=leerRecurso(resourceFile);                                  
                            //Si al leer este recurso , este recurso está vacio significa que no ha encontrado el recurso
                            //por lo que nos encontramos ante el error 404 e informamos de ello.
                            if(outdata==null){                                                  
                                outmesg="HTTP/1.1 404\r\nContent-type:text/html\r\n\r\n<html><body><h1>Recurso no encontrado</h1></body></html>"; 
                                outdata=outmesg.getBytes();                                     
                                System.out.println("HTTP/1.1 404 Error. Recurso no encontrado");                       
                            }                                                                   
                            //Si no esta vacío es que lo ha encontrado por tanto lo mostramos.
                            else{                                                                      
                                outmesg="HTTP/1.1 200 OK \r\nContent-type:text/html\r\n\r\n"+outdata+""; 
                            }
                            
                            // Aquí sacamos la longitud del recurso para rellenar la cabecera Content-Length
                            Content_length=""+outdata.length+"";                                        
                            
                            //Si la tercera parte(parts[2]) tiene la version HTTP/2 nos encontramos ante el error 505 el cual nos dice que 
                            //esa versión de HTTP no esta soportada
                            if(parts[2].equalsIgnoreCase("HTTP/2")){                            
                                                                                                
                                outmesg="HTTP/1.1 505\r\nContent-type:text/html\r\n\r\n<html><body><h1>Version del protocolo HTTP no soportada</h1></body></html>";
                                outdata=outmesg.getBytes();                                     
                                System.out.println("HTTP/1.1 505 Error. Version del protocolo HTTP no soportada");                       
                            }                                                                   
                        }  
                    }
                    else{//En el caso de que la petición no tenga 3 partes es que la petición es errónea
                        outmesg="HTTP/1.1 400\r\nContent-type:text/html\r\n\r\n<html><body><h1>Peticion errónea</h1></body></html>";
                        outdata=outmesg.getBytes();
                        System.out.println("HTTP/1.1 400 Error. Peticion erronea");
                    }                   
                }
                //Tambien puede empezar por Connection lo que significará que la conexión esta cerrada ya que no permite conexiones persistentes. 
                else if(request_line.startsWith("Connection")){                                
                    Connection="Close";                                                                                                                                                                          
                }
                else{//Si no empieza ni por GET ni por Connection es que es otro método el cuál no esta permitido en nuestra práctica
                                                                                                
                    outmesg="HTTP/1.1 405\r\nContent-type:text/html\r\n\r\n<html><body><h1>Metodo no permitido</h1></body></html>";
                    outdata=outmesg.getBytes();                                                
                    System.out.println("HTTP/1.1 405 Error. Método no permitido");                                   
                }  
                
                System.out.println(request_line);
            //}while(request_line.compareTo("")!=0);
            
            //Se ha eliminado el bucle do while, ya que al hacer la segunda iteración del bucle siempre entraba en el 405
            do{
                request_line= input.readLine();        
                System.out.println(request_line);
            }while(request_line.compareTo("")!=0);
            
            //Para obtener la hora usamos la clase DateTimeFormatter, Instant y ZoneOffset
             Instant instant = Instant.now();  
             String fecha = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC).format(instant);
        
            //CABECERAS
            //Mostramos por pantalla las distintas cabeceras
            System.out.println("------------------------------------"); 
            System.out.println("Connection: "+Connection+"");
            System.out.println("Content-Type: "+Content_type+"");
            System.out.println("Date: "+fecha+"");                                              
            System.out.println("Server: "+Server+"");                                          
            System.out.println("Allow: "+Allow+"");                                                                                           
            System.out.println("Content-Length: "+Content_length+"");                                                                        
            System.out.println("------------------------------------");                                                            
            
            //Recurso
            //Escribimos en output el resultado de outdata y cerramos el input, el output y el socket
           output.write(outdata);
            input.close();
            output.close();
            mSocket.close();
    
        } catch (IOException e) {//Capturamos la excepcion que surja y mostramos su mensaje.
            System.err.println("Exception" + e.getMessage());
        }
        }

    /**
     * Método para leer un recurso del disco
     * @param resourceFile
     * @return los bytes del archio o null si éste no existe
     */
     
    private byte[] leerRecurso(String resourceFile) throws FileNotFoundException, IOException {
        //Inicializamos las variables de entrada del stream a null
         FileInputStream fileInputStream = null;
	 BufferedInputStream bufferedInputStream = null;
         byte[] bytes = null;
         //Creamos el fichero con directorio ./ y el recurso
         File f = new File ("C:\\\\Users\\\\Juan_\\\\OneDrive\\\\Documentos\\\\GitHub\\\\PPT1718_Practica3_G08\\\\src\\\\ujaen\\\\git\\\\ppt\\\\p3\\\\"+resourceFile+"");
         //File f= new File("./"+resourceFile);
         //File f = new File ("C:\\\\Users\\\\Juan_\\\\PPT1718_Practica3_G08\\\\src\\\\ujaen\\\\git\\\\ppt\\\\p3\\\\"+resourceFile+"");
         
          //si existe el archivo calculamos su longitud, creamos los objetos de entrada del stream, 
          //leemos el archivo como bytes y lo devolvemos esos bytes
         
          //Con la función exits no funciona
          /*
          if (f.exists())
          { 
          long length = f.length();
          byte[] bytes = new byte[(int) length];
          fileInputStream = new FileInputStream(f);
          bufferedInputStream = new BufferedInputStream(fileInputStream);
          bufferedInputStream.read(bytes,0,bytes.length); 
	  return bytes;
          }
          else {//sino existe el archivo devolvemos nulo
           return null;
          }*/
                    
        try{
            fileInputStream = new FileInputStream(f);
            bytes = new byte[(int) f.length()];
            fileInputStream.read(bytes);
        }catch(IOException e) {
            }finally{
                if(fileInputStream !=null){
                    try{
                        fileInputStream.close();
                    }catch(IOException e){
                        e.printStackTrace();     
                    }
                }
            }
        return bytes;
    }   
}           

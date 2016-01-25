package frontservice;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FrontService extends Thread{
    
    String query;
    static String ipIndex="localhost";
    static String ipCaching="localhost";

    private FrontService(String query) {
        this.query = query;
    }
        
    // Para recibir desde el CachingService
    
    public void socketServidorFrontServiceParaCachingService() throws Exception{    
        //Variables
        String desdeCachingService;        
        //Socket para el servidor en el puerto 5000
        ServerSocket socketDesdeCachingService = new ServerSocket(5002);
        //Socket listo para recibir 
        Socket connectionSocket = socketDesdeCachingService.accept();
        //Buffer para recibir desde el cliente
        BufferedReader inDesdeCachingService = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        //Buffer para enviar al cliente
            
        //Recibimos el dato del cliente y lo mostramos en el server
        desdeCachingService =inDesdeCachingService.readLine();
        System.out.println("(Front Service) Recibidos: " + desdeCachingService);
        socketDesdeCachingService.close();
        if(desdeCachingService.equals("MISS!")){
            System.out.println("(Front Service) Enviando la consulta: "+ query + " al Index Service");
            socketClienteDesdeFrontServiceHaciaIndexService(query);
            socketServidorFrontServiceParaIndexService();                    
        }
        else{
            String[] tokens = desdeCachingService.split(",");
            for(int i=0; i<tokens.length;i++){
                String[] tokensResp = tokens[i].split("#");
                System.out.println("--------------------");
                System.out.println("Está en el documento: " + tokensResp[0]);
                System.out.println("Con una frecuencia de: " + tokensResp[1]);
                System.out.println("En la URL de wikipedia: " + tokensResp[2]);
            }
        }
    }
    
    // Para recibir desde el IndexService
    
    public static void socketServidorFrontServiceParaIndexService() throws Exception{    
        
        //Variables
        String desdeIndexService;        
        //Socket para el servidor en el puerto 5000
        ServerSocket socketDesdeIndexService = new ServerSocket(5004);
        
        //Socket listo para recibir 
        Socket connectionSocket = socketDesdeIndexService.accept();
        //Buffer para recibir desde el cliente
        BufferedReader inDesdeIndexService = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        //Buffer para enviar al cliente
            
        //Recibimos el dato del cliente y lo mostramos en el server
        desdeIndexService =inDesdeIndexService.readLine();
        System.out.println("(Front Service) Recibidos: " + desdeIndexService);
        socketDesdeIndexService.close();
        if(desdeIndexService.equals("MISS!!")){
            System.out.println("(Front Service) Palabra no encontrada en Index Service");
            System.out.println("(Front Service) No se han encontrado resultados");
        }
        else{
            String[] tokens = desdeIndexService.split(",");
            for(int i=0; i<tokens.length;i++){
                String[] tokensResp = tokens[i].split("#");
                System.out.println("--------------------");
                System.out.println("Está en el documento: " + tokensResp[0]);
                System.out.println("Con una frecuencia de: " + tokensResp[1]);
                System.out.println("En la URL de wikipedia: " + tokensResp[2]);
            }
        }
    }
    
    // Para enviar al CachingService
    
    public static void socketClienteDesdeFrontServiceHaciaCachingService(String query) throws Exception{        
        //Socket para el cliente (host, puerto)
        Socket socketHaciaCachingService = new Socket(ipCaching, 5001); //ipCachingService
        
        //Buffer para enviar el dato al server
        DataOutputStream haciaCachingService = new DataOutputStream(socketHaciaCachingService.getOutputStream());
        
        haciaCachingService.writeBytes(query + '\n');
        
        socketHaciaCachingService.close();  
    }
    
    // Para enviar al IndexService
    
    public static void socketClienteDesdeFrontServiceHaciaIndexService(String query) throws Exception{        
        //Socket para el cliente (host, puerto)
        Socket socketHaciaIndexService = new Socket(ipIndex, 5003); //ipCachingService
        
        //Buffer para enviar el dato al server
        DataOutputStream haciaIndexService = new DataOutputStream(socketHaciaIndexService.getOutputStream());
        
        haciaIndexService.writeBytes(query + '\n');
        
        socketHaciaIndexService.close();  
    }
    
    public static void socketClienteUsuarioaFrontService()throws Exception{
        //Variables
        String sentence;
        String fromServer;
        
        //Buffer para recibir desde el usuario
        BufferedReader entradaUsuario = new BufferedReader(new InputStreamReader(System.in));
        
        //Socket para el cliente (host, puerto)
        Socket clientSocket = new Socket("localhost", 5000);  //ipCachingService
        
        //Buffer para enviar el dato al server
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        
        //Buffer para recibir dato del servidor
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        
        //Leemos del cliente y lo mandamos al servidor
        sentence = entradaUsuario.readLine();
        outToServer.writeBytes(sentence + '\n');
        
        //Recibimos del servidor
        fromServer = inFromServer.readLine();
        System.out.println("Respuesta servidor: " + fromServer);
        
        //Cerramos el socket
        clientSocket.close();
    }
    
    
    @Override
    public void run(){        
        try {                            
            System.out.println("(Front Service) Soy el thread: " + getName() + ". Enviando la query '" + query + "' al Caching Service");
            String querySinEspacios = query.replaceAll(" ", "-");
            String httpMetodo = "GET /consulta/";
            String queryREST = new StringBuilder(httpMetodo).append(querySinEspacios).toString();
            socketClienteDesdeFrontServiceHaciaCachingService(queryREST);
            socketServidorFrontServiceParaCachingService();
        } catch (Exception ex) {
            Logger.getLogger(FrontService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws Exception {
        while(true){
          String query;
          BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
          query = inFromUser.readLine();
          FrontService hilo = new FrontService(query);
          hilo.start();
        }
    }    
}
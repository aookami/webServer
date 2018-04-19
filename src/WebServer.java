    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.* ;
import java.net.* ;
import java.util.* ;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Desktop;
import java.io.File;


public final class WebServer
{
	public static void main(String argv[]) throws Exception
	{
        int port = 3003;
        ServerSocket servidor = null;
            try {
                        servidor = new ServerSocket(port);
                    }
                    catch (IOException e) {
                        System.out.println("ERROR SERVER SOCKET");
                        System.out.println(e);
                    }
        while(true){   
            //server socket is open at "port", now we need an socket object to accept connections
            Socket clientSocket = null;
            try {
                clientSocket = servidor.accept();
                }
            catch (IOException e) {
                System.out.println("ERROR CLIENT SOCKET");
                System.out.println(e);
            }        
            //we hang at this point while waiting for a connection at port 
            HttpRequest request = new HttpRequest(clientSocket, servidor);
            Thread thread = new Thread(request);
            thread.start(); 
            
  
            System.out.println("loop");
         
        }
            
	}
}

final class HttpRequest implements Runnable
{
    Socket client;
    ServerSocket server;
    
    final static String CRLF = "\r\n";
    
    public HttpRequest(Socket client, ServerSocket server) throws Exception 
	{
		this.client = client;
                this.server = server;
	}
	


    @Override
    public void run() {
        try {
		processRequest();
	} catch (Exception e) {
		System.out.println(e);
	}

    }
    
    public void processRequest(){
    try{
                System.out.println(client.toString());
                InputStreamReader isr =  new InputStreamReader(client.getInputStream());
                //Aberto um parser de bytes para char, em formato de stream
                
                BufferedReader reader = new BufferedReader(isr);
                //Aberto um leitor de streams para strings
                
                String line = reader.readLine();//aqui é onde lemos cada string do request, utilizaremos apenas a primeira
                System.out.println(line);
                StringTokenizer tokens = new StringTokenizer(line);
                tokens.nextToken();  // skip over the method, which should be "GET"
                String fileName = tokens.nextToken();
                System.out.println(fileName);
                fileName = "." + fileName; //definimos o nome do arquivo a ser aberto
                
                //verifica se o arquivo existe
                FileInputStream fis = null;
                boolean fileExists = true;
                try {
                        fis = new FileInputStream(fileName);
                } catch (FileNotFoundException e) {
                        fileExists = false;
                }
                
                //construindo a mensagem de resposta
                String statusLine = null;
                String contentTypeLine = null;
                String entityBody = null;
                if (fileExists) {
                        statusLine = "HTTP/1.0 200 OK";
                        contentTypeLine = "ContentType: " + 
                                contentType(fileName) + CRLF;
                } else {
                        statusLine = "HTTP/1.0 404 Not Found ";
                        contentTypeLine = "Content-Type: text/html; charset=utf-8"+ CRLF;
                        entityBody = "<HTML>" +
                                "<HEAD><TITLE>Not Found</TITLE></HEAD>" +
                                "<BODY>Not Found</BODY></HTML>" + CRLF;
                }
                // Send the status line.
                //System.out.println(statusLine+contentTypeLine+entityBody);
                OutputStream os;
                os = client.getOutputStream();
        
                os.write(statusLine.getBytes());

                // Send the content type line.
                os.write(contentTypeLine.getBytes());

                // Send a blank line to indicate the end of the header lines.
                os.write(CRLF.getBytes());
                // Send the entity body.
                if (fileExists)	{
                        sendBytes(fis, os);
                        fis.close();
                } else {
                    String errormsg = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" + "<BODY>Not Found</BODY></HTML>";
                        os.write(errormsg.getBytes());
                }



        System.out.println("fechouse");
        client.close();
     
        
        this.finalize();
    }catch(IOException e){
        }
    catch (Throwable ex) {
            Logger.getLogger(HttpRequest.class.getName()).log(Level.SEVERE, null, ex);
    }
           
                    
    }
    
private static String contentType(String fileName)
{
	if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
		return "text/html";
	}
        if(fileName.endsWith(".png")) {
		return "image/PNG";
	}
        
        if(fileName.endsWith(".exe")) {
		return "text/html";
	}
        if(fileName.endsWith(".jpg")||fileName.endsWith(".jpeg")) {
		return "image/JPEG";
	}
        if(fileName.endsWith(".css")) {
		return "text/css";
	}
        if(fileName.endsWith(".gif")) {
		return "image/GIF";
	}
	return "application/octet-stream";
}
private static void sendBytes(FileInputStream fis, OutputStream os) 
throws Exception
{
    // Construct a 1K buffer to hold bytes on their way to the socket.
    byte[] buffer = new byte[1024];
    int bytes = 0;

     // Copy requested file into the socket's output stream.
     while((bytes = fis.read(buffer)) != -1 ) {
      os.write(buffer, 0, bytes);
    }
}

	
}

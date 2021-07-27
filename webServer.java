
import java.io.*;
import java.net.*;
import java.util.*;

public final class webServer {
	public static void main (String argv[]) throws Exception
	{
		//set port number
		int port = 6789;

		//Process HTTP service request on infinite loop
		ServerSocket serverSocket = new ServerSocket(port);
		Socket socket;

		while(true) {
			//Socket socket = serverSocket.accept();
			socket = serverSocket.accept();

			//Object to process HTTP request
			HttpRequest request = new HttpRequest(socket);

			//Create new thread to process request
			Thread thread = new Thread(request);

			//start thread
			thread.start();
		} // while
	} // main
} // webServer

final class HttpRequest implements Runnable {
	final static String CRLF = "\r\n";
	Socket socket;

	//Constructor
	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			processRequest();
		} catch(Exception e) {
			System.out.print(e);
		}
	}

	private void processRequest() throws Exception {
		//reference to the sockets input/output stream
		InputStream is = socket.getInputStream();
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());

		//stream filters
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		//request line of HTTP request message
		String requestLine = br.readLine();

		//print request line
		System.out.println();
		System.out.println(requestLine);

		//get and print header lines
		String headerLine = null;
		while((headerLine = br.readLine()).length()!= 0) {
			System.out.println(headerLine);
		} //while

		//Extract filename from request line
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); // skip over "GET" line
		String fileName = tokens.nextToken();
		fileName = "." + fileName;

		//Open requested file
		FileInputStream fis = null;
		boolean fileExists = true;
		try {
			fis = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			fileExists = false;
		} // try catch

		//Response message
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;
		if (fileExists) {
			statusLine = "HTTP/1.0 200 OK";
			contentTypeLine = "Content-type:" + contentType(fileName) + CRLF;
		} else {
			statusLine = "HTTP/1.0 404 Not Found";
			contentTypeLine = "Content-type:" + contentType(fileName) + CRLF;
			entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" +
						 "<BODY>Not Found</BODY> </HTML>";
		} // if/else

		//Send Status line
		os.writeBytes(statusLine);

		//Send content line
		os.writeBytes(contentTypeLine);

		//Send blank
		os.writeBytes(CRLF);

		//Send Entity Body
		if(fileExists) {
			sendBytes(fis,os);
			fis.close();
		} else {
			os.writeBytes(entityBody);
		}

		//Close stream and socket
		os.close();
		br.close();
		socket.close();
	} //processRequest

	private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {

		//Construct 1k buffer that hold bytes on their way to the socket
		byte [] buffer = new byte[1024];
		int bytes = 0;

		//copy requested file into socket output stream
		while((bytes = fis.read(buffer)) != -1 ) {
			os.write(buffer, 0, bytes);
		} // while
	} // sendBytes

	private static String contentType(String fileName) {
		if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		} //if
		if(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
			return "image/jpeg";
		} //if
		if(fileName.endsWith(".gif")) {
			return "image/gif";
		} //if
		return "application/octet-stream";
	} // contentTypes
} //HttpRequest

package serverhost;
import java.io.*;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class ServerProtocol {
	
	public static String charset = "UTF-8";
	
	public ServerProtocol() {
		
	}
	
	public static byte[] serialize(Object obj) throws IOException {
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(obj);
	    byte[] answer = out.toByteArray();
	    os.close();
	    return answer;
	}
	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
	    ByteArrayInputStream in = new ByteArrayInputStream(data);
	    ObjectInputStream is = new ObjectInputStream(in);
	    Object answer = is.readObject();
	    is.close();
	    return answer;
	}
	
	public static int processInput(InputStream in, OutputStream out){ //TODO: Databasing
		
		char[] req = new char[8192];
		
		InputStreamReader rd;
		
		int reqlen = 0;
		
		try {
			rd = new InputStreamReader(in, charset);
			reqlen = rd.read(req);
		} catch (UnsupportedEncodingException e) {
			System.err.println(Thread.currentThread().getName()+": System does not support charset " + charset);
		}catch (IOException e) {
			System.err.println(Thread.currentThread().getName()+": Failed to read input from client");
		}
		
		
		String request = new String(req).substring(0, reqlen);
		
		//String output = 
		
		byte[] outputBytes = processHTTP(request);
		try {
			//outputBytes = output.getBytes(charset);
			out.write(outputBytes);
		} catch (UnsupportedEncodingException e) {
			System.err.println(Thread.currentThread().getName()+": System does not support charset " + charset);
		}catch (IOException e) {
			System.err.println(Thread.currentThread().getName()+": Failed to write output to client");
		}
		
		return 0;
	}

	private static byte[] processHTTP(String request) {
		
		String[] requestLines = request.split("\n");
		
		if (requestLines.length <= 0)
			return null;
		
		DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy H:mm:ss zzz");
		new Date(0);
		
		String[] requestLine = requestLines[0].split(" ");
		
		byte[] response = null;
		
		String statusLine = "";
		String httpHeaders = "Date: "+dateFormat.format(new Date(System.currentTimeMillis()));
		
		boolean returnBody = false;
		long bodyLen = 0;
		byte[] body = null;
		
		switch(requestLine[0]){
		case "GET": //response to a get request
		case "HEAD":
			String filename = requestLine[1];
			if (!ServerDriver.isValidFile(filename)) {
				statusLine = "HTTP/1.1 404 BAD";
				break; 
			}
			RandomAccessFile file = null;
			System.out.println("Trying to open file " + System.getProperty("user.dir")+"/serverdata"+requestLine[1]);
			try { file = new RandomAccessFile(System.getProperty("user.dir")+"/serverdata"+requestLine[1], "r"); } 
			catch (FileNotFoundException e) { statusLine = "HTTP/1.1 502 BAD"; System.err.println(Thread.currentThread().getName()+": FILE ERROR"); break; }
			statusLine = "HTTP/1.1 200 OK";
			try { bodyLen = file.length(); }
			catch (IOException e) { statusLine = "HTTP/1.1 502 BAD"; System.err.println(Thread.currentThread().getName()+": IO ERROR"); break; }
			if (requestLine[0].equals("HEAD")) // head requests don't return body
				break;
			returnBody = true;
			body = new byte[(int)bodyLen];
			try { file.readFully(body); }
			catch (IOException e) { statusLine = "HTTP/1.1 502 BAD"; System.err.println(Thread.currentThread().getName()+": IO ERROR"); break; }
		default:
			break;
		}
		
		try { response = (statusLine + "\n" + httpHeaders + "\n\n").getBytes(charset); } 
		catch (UnsupportedEncodingException e){ System.err.println(Thread.currentThread().getName()+": System does not support charset " + charset); }
		if (returnBody){
			byte[] reply = new byte[response.length+(int)bodyLen];
			for (int i = 0; i < response.length; i++)
				reply[i] = response[i];
			for (int i = response.length; i < reply.length; i++)
				reply[i] = body[i-response.length];
			response = reply;
		}
		
		return response;
	}
}

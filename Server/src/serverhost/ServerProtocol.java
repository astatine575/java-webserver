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
		
		byte[] outputBytes = null;
		
		boolean outputBytesSet = true;
		
		switch (ServerDriver.mode){
		case "http":
			processHTTP(request,out);
			outputBytesSet = false;
			break;
		default:
			try { outputBytes = request.getBytes(charset); } 
			catch (UnsupportedEncodingException e1) { outputBytes = new byte[0]; System.err.println(Thread.currentThread().getName()+": System does not support charset " + charset); }
			break;
		}
		
		
		try {
			if (outputBytesSet) out.write(outputBytes);
		}catch (IOException e) {
			System.err.println(Thread.currentThread().getName()+": Failed to write output to client");
		}
		
		return 0;
	}

	private static void processHTTP(String request,OutputStream out) {
		
		String[] requestLines = request.split("\n");
		
		if (requestLines.length <= 0)
			return;
		
		DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy H:mm:ss zzz");
		new Date(0);
		
		String[] requestLine = requestLines[0].split(" ");
		
		byte[] response = null;
		
		String statusLine = "";
		String httpHeaders = "Date: "+dateFormat.format(new Date(System.currentTimeMillis()));
		
		boolean returnBody = false;
		long bodyLen = 0;
		byte[] body = null;
		RandomAccessFile file = null;
		
		switch(requestLine[0]){
		case "GET": //response to a get request
		case "HEAD":
			String filename = requestLine[1];
			if (filename.equals("/"))
				filename = ServerDriver.homepage;
			if (!ServerDriver.isValidFile(filename)) 
				statusLine = "HTTP/1.1 404 FILE_NOT_FOUND";
			try { file = new RandomAccessFile(System.getProperty("user.dir")+ServerDriver.serverdata+filename, "r"); } 
			catch (FileNotFoundException e) { statusLine = "HTTP/1.1 404 FILE_NOT_FOUND"; System.err.println(Thread.currentThread().getName()+": FILE "+System.getProperty("user.dir")+ServerDriver.serverdata+filename+" NOT FOUND"); break; }
			statusLine = "HTTP/1.1 200 OK";
			try { bodyLen = file.length(); }
			catch (IOException e) { statusLine = "HTTP/1.1 502 BAD"; System.err.println(Thread.currentThread().getName()+": CAN'T READ FILE"); break; }
			if (requestLine[0].equals("HEAD")) // head requests don't return body
				break;
			returnBody = true;
		default:
			break;
		}
		
		try { response = (statusLine + "\n" + httpHeaders + "\n\n").getBytes(charset); } 
		catch (UnsupportedEncodingException e){ System.err.println(Thread.currentThread().getName()+": System does not support charset " + charset); }
		
		try { out.write(response); }
		catch (IOException e) { System.err.println(Thread.currentThread().getName()+": Failed to write output to client"); return; }
		
		if (returnBody){
			body = new byte[256000];
			int repititions = -1;
			try {
				int rem=file.read(body);
				do{
					repititions++;
					if (rem==-1) break;
					try { out.write(body,0,rem); }
					catch (IOException e) { System.err.println(Thread.currentThread().getName()+": Failed to write output to client"); break; }
				} while ((rem=file.read(body))==256000);
				try { out.write(body,0,rem); }
				catch (IOException e) { System.err.println(Thread.currentThread().getName()+": Failed to write output to client"); }
			} catch (IOException e) {
				System.err.println(Thread.currentThread().getName()+": Failed to read file");
			}
			
			body=null;
			
		}
		
		try { if (file!=null) file.close(); }
		catch (IOException e) { System.err.println(Thread.currentThread().getName()+": CAN'T CLOSE FILE"); return; }
	}
}

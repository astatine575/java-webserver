package serverhost;
import java.io.*;
import java.sql.*;
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
		
		char[] request = new char[4096];
		
		InputStreamReader rd;
		
		try {
			rd = new InputStreamReader(in, charset);
			rd.read(request);
		} catch (UnsupportedEncodingException e) {
			System.err.println(Thread.currentThread().getName()+": System does not support charset " + charset);
		}catch (IOException e) {
			System.err.println(Thread.currentThread().getName()+": Failed to read input from client");
		}
		
		
		//System.out.println(request);
		
		String output = "HUEHUEHUEHUE";
		
		byte[] outputBytes;
		try {
			outputBytes = output.getBytes(charset);
			out.write(outputBytes);
		} catch (UnsupportedEncodingException e) {
			System.err.println(Thread.currentThread().getName()+": System does not support charset " + charset);
		}catch (IOException e) {
			System.err.println(Thread.currentThread().getName()+": Failed to write output from client");
		}
		
		/*
		byte[] authorization = new byte[Long.BYTES];
		
		int typeOfRequest = 0;
		 
		try {
			if ((in.read(authorization))!=128){
				out.write(-2);
				return -2;
			}
			try{
				Long auth = (Long) deserialize(authorization);
			} catch (ClassNotFoundException f){
				out.write(-2);
				return -2;
			}
		} catch (IOException e){
			System.out.println("IO error in client authorization");
			return -1;
		}

		System.out.println(in);
		//System.out.println("one run of processInput");

		while(true){

			try {
				typeOfRequest = in.read();
				switch (decodeRequestType(typeOfRequest)){
				case ERRONEOUS:
					out.write(0);
					break;
				default:
					out.write(-1);
					return -1;
				}
			} catch (IOException e) {
				System.out.println("IO error in client reading");
				return -1;
			}
			return 0;
		}
		*/
		return 0;
	}
	
	private static INPUT_TYPE decodeRequestType(int i){
		
		switch (i){
		case 0: return INPUT_TYPE.PING;
		default:
			return INPUT_TYPE.ERRONEOUS;
		}
	}
}

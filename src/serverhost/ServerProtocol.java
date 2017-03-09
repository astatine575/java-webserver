package serverhost;
import java.io.PrintWriter;
import java.sql.*;

public class ServerProtocol {
	
	public ServerProtocol() {
		
	}
	
	public static boolean processInput(String input, PrintWriter out){ //TODO: Databasing
		
		switch (decodeRequestType(input)){
		case ERRONEOUS:
		default:
			out.print("ERRONEOUS");
			break;
		}
		
		return false;
	}
	
	private static INPUTTYPE decodeRequestType(String s){
		return INPUTTYPE.ERRONEOUS;
	}
}

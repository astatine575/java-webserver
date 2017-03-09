package serverhost;
import java.io.PrintWriter;
import java.sql.*;

public class ServerProtocol {
	
	public ServerProtocol() {
		
	}
	
	public static boolean processInput(String input, PrintWriter out){ //TODO: Databasing
		
		System.out.println(input);
		System.out.println("one run of processInput");
		
		switch (decodeRequestType(input)){
		case ERRONEOUS:
		default:
			out.print("<html>hi</html>");
			break;
		}
		
		return false;
	}
	
	private static INPUT_TYPE decodeRequestType(String s){
		return INPUT_TYPE.ERRONEOUS;
	}
}

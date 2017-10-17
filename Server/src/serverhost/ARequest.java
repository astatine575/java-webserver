package serverhost;

public class ARequest {
	
	public int _BOUNTY;
	public String _TITLE;
	public String _DESC;
	public String _POSTER;
	
	public ARequest(String s){
		fromString(s);
	}
	
	public String toString(){
		return "";
	}
	
	public static String toString(ARequest r){
		return r.toString(); //format $TITLE|$DESC|$BOUNTY|$POSTER
	}
	
	public ARequest fromString(String s){
		return new ARequest(s); //format $TITLE|$DESC|$BOUNTY|$POSTER
	}
}

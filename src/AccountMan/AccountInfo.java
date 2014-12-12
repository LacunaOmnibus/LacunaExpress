package AccountMan;

public class AccountInfo{
    public AccountInfo(){};
    AccountInfo(String userName, String password, String aPIKey, String server, String sessionID, String sessionDate, Boolean defaultAccount){
        this.userName = userName;
        this.password = password;
        this.aPIKey = aPIKey;
        this.server = server;
        this.sessionID = sessionID;
        this.sessionDate = sessionDate;
        this.defaultAccount = defaultAccount;
    }
    public String userName, password, aPIKey, server, sessionID, sessionDate, displayString;
    public Boolean defaultAccount;
    public void CreateDisplayString(){
    	this.displayString = userName+" (";
    	if(server.equals("https://us1.lacunaexpanse.com"))
    		this.displayString +="US1)";
    	if(server.equals("https://pt.lacunaexpanse.com"))
    		this.displayString +="PT)";
    }
}

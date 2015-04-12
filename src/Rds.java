import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;


public class Rds {
    private String password = null;
    private static Rds instance = null;
    private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private final String DB_URL = "jdbc:mysql://tweet.cssmopf7grit.us-east-1.rds.amazonaws.com/tweets";
    
    public Connection conn = null;
    public String table = "tweet_sentiment";
    
    private Rds() {
    	conn = null;
    }
    
    public synchronized static Rds getInstance() {
    	if (instance == null)
    		instance = new Rds();
    	return instance;
    }
    
    public boolean isConnected() {
    	return conn != null;
    }
    
    public boolean isPasswordSet() {
    	return this.password != null;
    }
    
    public void setPassword(String password) {
    	this.password = password;
    	if (conn == null)
    		init();
    }
    
    public synchronized void init() {
    	while (true) {
            try {
                Class.forName(JDBC_DRIVER);
                System.out.println("Connecting to database...");
                conn = DriverManager.getConnection(DB_URL, "FallMonkey", password);
                break;
            } catch (Exception e) {
            	e.printStackTrace();
            }
    	}
    }

    HashMap<String, String> map = new HashMap<String, String>();

    private void createMap() {
        map.put("Jan", "01");
        map.put("Feb", "02");
        map.put("Mar", "03");
        map.put("Apr", "04");
        map.put("May", "05");
        map.put("Jun", "06");
        map.put("Jul", "07");
        map.put("Aug", "08");
        map.put("Sep", "09");
        map.put("Oct", "10");
        map.put("Nov", "11");
        map.put("Dec", "12");
    }

    private String convertTime(String date) {
        String processed = null;

        if (map.size() == 0) {
            createMap();
        }

        // hard coded according to tweet format
        String[] s = date.split(" ");
        String year = s[5];
        String month = s[1];
        String day = s[2];
        String time = s[3];
        processed = year+"-"+map.get(month)+"-"+day+" "+time;

        Timestamp timestamp = Timestamp.valueOf(processed);
        return String.valueOf(timestamp.getTime());
    }
 
    @SuppressWarnings("static-access")
	public synchronized void insert(String id_str, String keyword, String user, String text, String latitude, String longitude, String created_at) {
        System.out.println("Inserting into table " +table );
        String sql = "INSERT INTO " + table + " VALUES (?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps;
        while (true) {
	        try {
	            ps = conn.prepareStatement(sql);
	            String timestamp = convertTime(created_at);
	   
	            ps.setString(1, id_str);
	            ps.setString(2, keyword);
	            ps.setString(3, user);
	            ps.setString(4, text);
	            ps.setString(5, latitude);
	            ps.setString(6, longitude);
	            ps.setString(7, timestamp);
	            ps.setBoolean(8, false);
	            ps.setDouble(9, 0.0);
	
	            ps.executeUpdate();
	
	            ps.close();
	            break;
	            
	        } catch (SQLException e) {
				System.out.println(sql);
				System.out.println("Reconnect to database in 3 seconds.");
				try {
					Thread.currentThread().sleep(3000);
					conn.close();
				} catch (Exception e1) {
					e1.printStackTrace(System.out);
				}
            	init();
                e.printStackTrace(System.out);
                break;
	        }
        }


    }
    
    @SuppressWarnings("static-access")
	public synchronized void update(String id, double value){
    	String sql = "update tweet_sentiment set sentiment = ?, sentiment_exist = ? where id_str = ?";
    	PreparedStatement ps;
    	while (true) {
			 try {
			     ps = conn.prepareStatement(sql);
			     
			     ps.setDouble(1, value);
			     ps.setBoolean(2, true);
			     ps.setString(3, id);
			     ps.executeUpdate();
			
			     ps.close();
			     break;
			     
			 } catch (Exception e) {
					System.out.println(sql);
					System.out.println("Reconnect to database in 3 seconds.");
					try {
						Thread.currentThread().sleep(3000);
						conn.close();
					} catch (Exception e1) {
						e1.printStackTrace(System.out);
					}
	            	init();
	                e.printStackTrace(System.out);
	                break;
			 }
    	}

    	
    }
}
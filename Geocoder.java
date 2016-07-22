import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Geocoder {
	public static void main(String[] args){
		Statement statement;
		PreparedStatement statement1;
		ResultSet rs;
		Connection conn = null;
		Connection conn1 = null;
		String name = "test.Syringa_nodes";
		try{
			String userName = "root";
            String password = "";
            String url = "jdbc:mysql://127.0.0.1:3306/DB_INSTANCE";
            Class.forName ("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection (url, userName, password);
            System.out.println ("Database connection established");
            statement = conn.createStatement();
            //rs = statement.executeQuery("select * from "+name+";");
            rs = statement.executeQuery("SELECT * FROM `DB_INSTANCE`.`NODE_TABLE_NAME`;");
            ArrayList<String> arr=new ArrayList<String>(); 
            ArrayList<Integer> exID = new ArrayList<Integer>();
            int i=0;
            while(rs.next())
            {
            	arr.add(rs.getString("placeLocn")); // placeLocn has the street address of a node
            	i++;
            }
            statement.close();
            conn.close();
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            conn1 = DriverManager.getConnection (url, userName, password);
            System.out.println ("Database connection established");
            // placeLat and placeLon will be populated with the geo coordinates
            statement1 = conn1.prepareStatement("update "+name+" set placeLat=?, placeLon=? where placeLocn=?");
            String uAddress;
            String address;
            //String splitter[];
            //String cAddress;
            int n = arr.size();
            for(i=0;i<n;i++)
            {
            	uAddress = arr.get(i); 
            	//splitter = uAddress.split(",");
            	//cAddress=splitter[0]+" county,"+splitter[1].toLowerCase();
        		address = URLEncoder.encode(uAddress, "UTF-8");
        		System.out.println(address);
            	URL urlOpen = new URL("http://maps.googleapis.com/maps/api/geocode/xml?address="+address+"&sensor=false");
            	BufferedReader in = new BufferedReader(new InputStreamReader(urlOpen.openStream()));
				String inputLine;
				StringBuffer sb = new StringBuffer();
				while ((inputLine = in.readLine()) != null)
				{
					sb.append(inputLine);
				}
				System.out.println(sb.toString());
				try{
					String geometry = sb.toString().substring(sb.toString().indexOf("<geometry>")+10, sb.toString().indexOf("</geometry>"));
					String location = geometry.substring(geometry.indexOf("<location>")+10,geometry.indexOf("</location>"));
					float lat = Float.parseFloat(location.substring(location.indexOf("<lat>")+5,location.indexOf("</lat>")));
					float lng = Float.parseFloat(location.substring(location.indexOf("<lng>")+5,location.indexOf("</lng>")));
					System.out.println("Address : "+uAddress+"--Lat :"+lat+"--Lon : "+lng);
					statement1.setFloat(1, lat);
					statement1.setFloat(2, lng);
					statement1.setString(3, uAddress);
					statement1.executeUpdate();
				}
				catch(StringIndexOutOfBoundsException e)
				{
					continue;
				}
				in.close();
            }
            statement1.close();
            conn1.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		}
}

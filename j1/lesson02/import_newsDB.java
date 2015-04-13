package j1.lesson02;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class import_newsDB {
	public static String[] import_news1(String event){
		String[] news_aids = new String[100];
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection(
					//"jdbc:sqlite:/Users/admin/Documents/java_set/test.db"); 
					"jdbc:sqlite:/Users/admin/Documents/workspace/article_data.db"); 
			Statement st = conn.createStatement();
			ResultSet rs = 
		    st.executeQuery("select * from " + event);
			int i =0;
		while(rs.next()) {
			news_aids[i] = rs.getString(1) + ".txt";//aid.txt
			i++;
			}
			rs.close();
			st.close();
			conn.close(); 
			int j = 0;
			String[] news_files = new String[i];
			while(j < i){
				news_files[j] = news_aids[j];
				System.out.println(news_files[j]);
				j++;
			}
			return news_files;
	
		} catch (ClassNotFoundException e) {
			System.out.println(e);
			return null;
			} catch (SQLException e) { 
				System.out.println("sql:"+ e);
				return null;
				}
	}




public static void entry_measure(Map<String,Double> scores, String kind){ //種類
//	Map<String,Double> scores = new HashMap<String,Double>();
//			String kind = "diversity";
//			scores.put("air2.txt", 0.0045);
//			scores.put("air3.txt", 0.034);

	try {
		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection(
				//"jdbc:sqlite:/Users/admin/Documents/java_set/test.db");
				"jdbc:sqlite:/Users/admin/rails_lesson/news_app/db/development.sqlite3"); 
		Statement st = conn.createStatement();
		Set<String> keys = scores.keySet();
		Iterator<String> it = keys.iterator();
		System.out.println(scores);
		while(it.hasNext()){
			//aidの取り出し
			String file_name = it.next();
			System.out.println(file_name);
			String[] file_ids = file_name.split("\\.");
			int aid = Integer.parseInt(file_ids[0]);
			
			//aidが存在するかをチェック
			String check_sql = "select * from "+ kind + " where aid = "+aid;
			ResultSet rs = st.executeQuery(check_sql);
			System.out.println("check_sql:");
			String sql = null;
			//aidがあれば更新、無ければ挿入
			if(rs.next()){
				sql = "update "+ kind + " set score = "+scores.get(file_name)+" where aid = "+aid;
			}else{
				sql = "insert into "+ kind +"(aid, score) values("+ aid + "," + scores.get(file_name) + ")";
			}
			
			System.out.println(sql+"aa");
			st.executeUpdate(sql);
			System.out.println("exe_sql");
			System.out.println(sql);
			rs.close();
			
		}
		st.close();
		conn.close(); 
		
	} catch (ClassNotFoundException e) {
		System.out.println(""+ e);

		} catch (SQLException e) { 
			System.out.println(""+ e);
			}
	
	
}





}


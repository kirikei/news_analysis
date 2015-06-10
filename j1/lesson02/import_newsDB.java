package j1.lesson02;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class import_newsDB {
	private static int Max_file = 10000;
	private static String Database_path = "jdbc:sqlite:/Users/admin/Documents/workspace/a_measure.clean/relevant_news.db";
	//"jdbc:sqlite:/Users/admin/Documents/java_set/test.db");

	//トップニュースを取り出す(pid = NULL)

	public static String[] import_top_news(String database_name){
		String[] news_tids = new String[Max_file];
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection(Database_path); 
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery("select * from " + database_name + " where pid is NULL");
			int i = 0;
			while(rs.next()) {
				news_tids[i] = rs.getString(1) + ".txt";//aid.txt
				String news_text = rs.getString(5);//text ニュースの本文
				try{
					FileWriter fw = new FileWriter(connecter_stan.ArticleFolder + news_tids[i]);  //���P
					PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
					pw.println(news_text);
					pw.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				i++;
			}
			rs.close();
			st.close();
			conn.close(); 
			int j = 0;

			String[] news_files = new String[i];

			while(j < i){
				news_files[j] = news_tids[j];
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



	public static String[] import_related_news(String database, String pid_txt){
		String[] news_aids = new String[Max_file];
		//トップ記事は配列の０番目
		news_aids[0] = pid_txt;
		//.txtを外す
		String pid = pid_txt.split("\\.")[0];
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection(Database_path); 
			Statement st = conn.createStatement();
			ResultSet rs = 
					st.executeQuery("select * from " + database + " where pid = '"+ pid +"'");

			//トップ記事以外を格納
			int i = 1;
			while(rs.next()) {
				news_aids[i] = rs.getString(1) + ".txt";//aid.txt
				String news_text = rs.getString(5);
				try{
					FileWriter fw = new FileWriter(connecter_stan.ArticleFolder + news_aids[i]);  //���P
					PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
					pw.println(news_text);
					pw.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}

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
					Database_path); 
			Statement st = conn.createStatement();
			Set<String> keys = scores.keySet();
			Iterator<String> it = keys.iterator();
			System.out.println(scores);
			while(it.hasNext()){
				//aidの取り出し
				String file_name = it.next();
				System.out.println(file_name);
				String[] file_ids = file_name.split("\\.");
				String aid = file_ids[0];

				//aidが存在するかをチェック
				String check_sql = "select * from "+ kind + " where aid = '"+aid+"'";
				ResultSet rs = st.executeQuery(check_sql);
				//System.out.println("check_sql:");
				String sql = null;
				//aidがあれば更新、無ければ挿入
				if(rs.next()){
					sql = "update "+ kind + " set score = "+scores.get(file_name)+" where aid = '"+aid+"'";
				}else{
					sql = "insert into "+ kind +"(aid, score) values('"+ aid + "','" + scores.get(file_name) + "')";
				}

				//System.out.println(sql+"aa");
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

	public static void import_entities(String[] articles, Map<String, ArrayList<String>> entities){

		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection(
					//"jdbc:sqlite:/Users/admin/Documents/java_set/test.db");
					Database_path); 
			Statement st = conn.createStatement();

			for (int i = 0; i < articles.length; i++) {
				//aidの取り出し
				String[] file_ids = articles[i].split("\\.");
				String aid = file_ids[0];

				//aidが存在するかをチェック
				String check_sql = "select * from newsEntities where aid = '"+aid + "'";
				ResultSet rs = st.executeQuery(check_sql);
				System.out.println("check_sql:");
				String sql = null;
				//aidがあれば更新、無ければ挿入
				if(rs.next()){
					//'qutation'を忘れない！
					sql = "update newsEntities set entities = '"+entities.get(articles[i])+"' where aid = '"+aid+"'";
				}else{
					sql = "insert into newsEntities(aid, entities) values('"+ aid + "','" + entities.get(articles[i]) + "')";
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

	public static void import_core_entities(String top_article, ArrayList<String> core_entities){

		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection(
					//"jdbc:sqlite:/Users/admin/Documents/java_set/test.db");
					Database_path); 
			Statement st = conn.createStatement();

			//aidの取り出し
			String[] file_ids = top_article.split("\\.");
			String aid = file_ids[0];

			//aidが存在するかをチェック
			String check_sql = "select * from coreEntities where aid = '"+aid+"'";
			ResultSet rs = st.executeQuery(check_sql);
			System.out.println("check_sql:");
			String sql = null;
			//aidがあれば更新、無ければ挿入
			if(rs.next()){
				sql = "update coreEntities set core_entities = '"+core_entities+"' where aid = '"+aid+"'";
			}else{
				sql = "insert into coreEntities(aid, core_entities) values('"+ aid + "','" + core_entities + "')";
			}

			System.out.println(sql+"aa");
			st.executeUpdate(sql);
			System.out.println("exe_sql");
			System.out.println(sql);
			rs.close();


			st.close();
			conn.close(); 

		} catch (ClassNotFoundException e) {
			System.out.println(""+ e);

		} catch (SQLException e) { 
			System.out.println(""+ e);
		}


	}

	public static void import_positive_score(String article, Map<String, Double> positive_score){

		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection(
					//"jdbc:sqlite:/Users/admin/Documents/java_set/test.db");
					Database_path); 
			Statement st = conn.createStatement();

			//aidの取り出し
			String[] file_ids = article.split("\\.");
			String aid = file_ids[0];

			//aidが存在するかをチェック
			String check_sql = "select * from positivescore where aid = '"+aid+"'";
			ResultSet rs = st.executeQuery(check_sql);
			System.out.println("check_sql:");
			String sql = null;
			//aidがあれば更新、無ければ挿入
			if(rs.next()){
				sql = "update positivescore set p_score = '"+positive_score+"' where aid = '"+aid+"'";
			}else{
				sql = "insert into positivescore(aid, p_score) values('"+ aid + "','" + positive_score + "')";
			}

			System.out.println(sql+"aa");
			st.executeUpdate(sql);
			System.out.println("exe_sql");
			System.out.println(sql);
			rs.close();


			st.close();
			conn.close(); 

		} catch (ClassNotFoundException e) {
			System.out.println(""+ e);

		} catch (SQLException e) { 
			System.out.println(""+ e);
		}


	}
	public static void import_topic_score(String article, Map<Integer, Double> topic_score){

		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection(
					//"jdbc:sqlite:/Users/admin/Documents/java_set/test.db");
					Database_path); 
			Statement st = conn.createStatement();

			//aidの取り出し
			String[] file_ids = article.split("\\.");
			String aid = file_ids[0];

			//aidが存在するかをチェック
			String check_sql = "select * from topicscore where aid = '"+aid+"'";
			ResultSet rs = st.executeQuery(check_sql);
			System.out.println("check_sql:");
			String sql = null;
			//aidがあれば更新、無ければ挿入
			if(rs.next()){
				sql = "update topicscore set t_score = '"+topic_score+"' where aid = '"+aid+"'";
			}else{
				sql = "insert into topicscore(aid, t_score) values('"+ aid + "','" + topic_score + "')";
			}

			System.out.println(sql+"aa");
			st.executeUpdate(sql);
			System.out.println("exe_sql");
			System.out.println(sql);
			rs.close();


			st.close();
			conn.close(); 

		} catch (ClassNotFoundException e) {
			System.out.println(""+ e);

		} catch (SQLException e) { 
			System.out.println(""+ e);
		}
	}
	
	//

	//データベースでtextで格納された配列をリストへ変換
	public static ArrayList<String> change_arraylist(String array_text){
		//前後の[ ]を省く
		String array_text1 = array_text.replaceAll("[", "");
		String array_text2 = array_text1.replaceAll("]", "");

		try {
			//,とスペース繋いだ物でスプリット→配列へ
			String[] changed_array = array_text2.split("\\, ");
			ArrayList<String> result_list = new ArrayList<String>(Arrays.asList(changed_array));
			return result_list;
		} catch (Exception e) {
			// TODO: handle exception
			//失敗した場合は空のリストを返す
			ArrayList<String> null_list = new ArrayList<String>();
			return null_list;
		}
	}

	public static Map change_map(String map_text, boolean int_or_str){
		//前後の[ ]を省く
		String map_text1 = map_text.replaceAll("[", "");
		String map_text2 = map_text1.replaceAll("]", "");
		Map result_map = new HashMap();

		try {
			//,とスペース繋いだ物でスプリット→配列へ
			String[] changed_array = map_text2.split("\\, ");
			for (int i = 0; i < changed_array.length; i++) {
				//key=valueを分割
				String[] key_value = changed_array[i].split("=");
				if(int_or_str){
					result_map.put(Integer.parseInt(key_value[0]), key_value[1]);	
				}else{
					result_map.put(key_value[0], key_value[1]);	
				}
			}
			return result_map;

		} catch (Exception e) {
			// TODO: handle exception
			//失敗した場合は空のリストを返す
			return result_map;
		}
	}
}


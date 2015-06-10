package j1.lesson02;

import java.awt.RenderingHints.Key;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import j1.lesson02.positive_score;
import j1.lesson02.SentiWordNetDemoCode;

public class connecter_stan {
	public static String event = "ArunJaitley";
	public static String Article_database = "newsArticles";
	public static int[] Measure_num = {1,2,3,1,2,3};//,2,2,1,1};
	//public static String Origin_file = "0.txt";
	public static String Name_of_Dir = "re-rank-merge";
	public static SentiWordNetDemoCode sentiwordnet;
	public static String TopicFolder = "/Users/admin/Documents/workspace/news_analysis_history/topic_probability/";
	public static String ArticleFolder = "/Users/admin/Documents/workspace/news_analysis_history/articles/"+event+"/";
	public static String TopicCsvFolder = "";
	public static String EntityTreeFolder = "/Users/admin/Documents/workspace/news_analysis_history/entity_csvs/";
	public static String EntityTreeCsvFolder = "";
	public static boolean Second = true;
	
	public static void main(String[] args) throws Exception{
		//元々の文章：[aid].txt 解析後の文章: re_[aid].txt databese（配列）入りはaid.txtのみ
		//合成後の文章: [pid].txt_aid.txt 合成後かつ解析後の文章: re_[pid].txt_[aid].txt

		//String[] top_news = import_newsDB.import_top_news(Article_database);
		int top_num = 0;

		//while(top_num < top_news.length){
		
		//データベースにテーブルが存在しなければ
		//import_newsDB.file_in_database(event);
		String[] database = import_newsDB.import_news1(event);//aid.txtを取り出す(これだけ使う)

		//ファイルの合成
		//cut_file.while_combine(database);//~1_2.txt
		//cut_file.all_replace_esc(database);
		//StanfordCoreNlpDemo.start_stan1(database);//result_i,result_1_i, input_i,.....

		//エスケープ文字を排除する
		//cut_file.all_replace_esc(database);

		//SentiWordNetのMapを作る
		sentiwordnet = new SentiWordNetDemoCode("SentiWordNet_3.0.0_20130122.txt");
		//各ファイルのNamed Entityを格納してeventのentity listを作る
		
		//トピックのcsvを格納するフォルダの製作
		TopicCsvFolder = TopicFolder+event + "/";
		File new_topic_dir = new File(TopicCsvFolder);
		new_topic_dir.mkdir();

		//Named entityのサブツリーのcsvファイルを格納するフォルダの製作
		EntityTreeCsvFolder = EntityTreeFolder+event+"/";
		File new_tree_dir = new File(EntityTreeCsvFolder);
		new_tree_dir.mkdir();	
		
		
		
		Map<String, ArrayList<String>> entities_list = word_hit.file_entities(database);
		import_newsDB.import_entities(database, entities_list);
		System.out.println(entities_list);

		ArrayList<String> core_entities = core_entity.check_core(database, entities_list);//core entity生成
		import_newsDB.import_core_entities(database[0], core_entities);

		//1 = 観点の差,　2 = 極性の差, 3 = 詳細の差
		int[] re_rank = Measure_num;
		int times = 0;
		ArrayList<String> history = new ArrayList<String>();
		history.add(database[0]);
		String[] change_rows = database;
		ArrayList<String> first_result = new ArrayList<String>();
		
		//平均計算後の値達
//		Map<String, Double> ad_yori = new HashMap<String, Double>();
//		Map<String, Double> ad_igi = new HashMap<String, Double>();
//		Map<String, Double> ad_det = new HashMap<String, Double>();
		//String[] rank_art = {"17.txt", "15.txt", "8.txt", "9.txt", "1.txt", "6.txt"};
		
		//ユーザの履歴

		while(times < re_rank.length){
			//String[] new_row = decrease_row(database, history);
			//System.out.println("ccc???"+core_entities);
			//calculate_rel_div.start_rel(change_rows,core_entities, entities_list);
			Map<String, Double> yorimichi = calculate_rel_div.start_div(database, core_entities, entities_list,history);
			Map<String, Double> igiari = positive_score.start_positive(database, entities_list, core_entities,history);	
			Map<String, Double> detailedness = calculate_detail.dif_detailedness(database,entities_list,core_entities, history);

			String max_file = null;
			
//			if(times == 0){
//				ad_yori = yorimichi;
//				ad_igi = igiari;
//				ad_det = detailedness;
//			}
//			else{
//				ad_yori = cal_re_rank(yorimichi, ad_yori, times);
//				ad_igi = cal_re_rank(igiari, ad_igi, times);
//				ad_det = cal_re_rank(detailedness, ad_det, times);
//			}

			//最大の値を持つファイルを取り出し
			switch (re_rank[times]) {
			case 1:
				max_file = max_file(yorimichi, history);
				break;
			case 2 :
				max_file = max_file(igiari, history);
				break;
			case 3 :
				max_file = max_file(detailedness, history);
			default:
				break;
			}

			//csvに出力
			print_ranking(database, yorimichi, igiari, detailedness);
			//print_ranking(database, ad_yori, ad_igi, ad_det);
			if(times == 0){
				first_result = first_ranking(database, yorimichi, igiari, detailedness, re_rank);
			}

			//今までに出た物を出力
			history.add(max_file);
			System.out.println("moto_row::"+history);
			System.out.println("first_result::"+first_result);

			//元記事と入れ替え
			//change_rows = file_swap(database, max_file);
			//historyに挿入
			times++;
			System.out.println(times);
			Second = true;
		}		
		//i++;}
	}
	
	private static ArrayList<String> first_ranking(String[] database, Map<String, Double> yorimichi,Map<String, Double> igiari, Map<String, Double> detailedness, int[] re_rank){

		ArrayList<String> history = new ArrayList<String>();
		history.add(database[0]);
		for (int i = 0; i < re_rank.length; i++) {
			String max_file = "";
			switch (re_rank[i]) {
			case 1:
				max_file = max_file(yorimichi, history);
				history.add(max_file);
				break;
			case 2 :
				max_file = max_file(igiari, history);
				history.add(max_file);
				break;
			case 3 :
				max_file = max_file(detailedness, history);
				history.add(max_file);
			default:
				break;
			}
		}
		return history;
	}
	
	
	
	
	//元記事を変更するメソッド
	private static String[] file_swap(String[] articles, String origin){
		for (int i = 0; i < articles.length; i++) {
			if(origin.equals(articles[i])){
				String temp = articles[i];
				articles[i] = articles[0];
				//指定された元記事を配列の0番目へ挿入
				articles[0] = temp;
			}
		}
		return articles;
	}

	private static void print_ranking(String[] args, Map<String, Double> yorimichi, Map<String, Double> igiari, Map<String, Double> detaileds){
		try {
			FileWriter fw = new FileWriter("/Users/admin/Documents/workspace/news_analysis_history/"+connecter_stan.Name_of_Dir+"/re_"+connecter_stan.event+"_result.csv", true);  //���P
			PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
			pw.println("["+args[0]+"],view,,polarity,,detail");
			for (int i = 1; i < args.length; i++) {
				pw.print(args[i]+",");
				pw.print(yorimichi.get(args[i])+",,");
				pw.print(igiari.get(args[i])+",,");
				pw.println(detaileds.get(args[i]));
			}
			pw.println();
			pw.close();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private static String[] decrease_row(String[] data, ArrayList<String> moto_row){
		String[] result_row = new String[data.length - moto_row.size()];
		int j = 0;
		for (int i = 0; i < result_row.length; i++) {
			if(moto_row.contains(data[i]) == false){
				result_row[j] = data[i];
				j++;
			}
		}
		return result_row;
	}

	private static String max_file(Map<String, Double> maps, ArrayList<String> moto_row){
		double max_score = -10000;
		String max_file = null;
		Set<String> keys = maps.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			String file = it.next();
			double now_score= maps.get(file);
			if(now_score>max_score && (moto_row.contains(file) == false)){
				max_score = now_score;
				max_file = file;
			}
		}
		return max_file;
	}

	//平均の計算
	private static Map<String, Double> cal_re_rank(Map<String, Double> now_measure, Map<String, Double> ad_measure, int times){
		Map<String, Double> resu_measure = new HashMap<String, Double>();
		Set<String> keys = now_measure.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			String file = it.next();
			double now_score = now_measure.get(file);
			//System.out.println(times + " : file:"+ file +" now_score="+now_score);
			if(ad_measure.containsKey(file)){
				double ad_score = ad_measure.get(file);
				//System.out.println("file:"+ file +" ad_score="+ad_score);
				resu_measure.put(file, ((now_score+times*ad_score)/(times+1)));
			}
			else{
				resu_measure.put(file, now_score);
			}
		}

		return resu_measure;
	}

}

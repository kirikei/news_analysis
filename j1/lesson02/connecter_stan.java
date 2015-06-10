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
	public static String Article_database = "newsArticles";
	public static SentiWordNetDemoCode sentiwordnet;
	public static String TopicFolder = "/Users/admin/Documents/workspace/a_measure.clean/topic_probability/";
	public static String ArticleFolder = "/Users/admin/Documents/workspace/a_measure.clean/articles_txt/";
	public static String TopicCsvFolder = "";
	public static String EntityTreeFolder = "/Users/admin/Documents/workspace/a_measure.clean/entity_csvs/";
	public static String EntityTreeCsvFolder = "";

	public static void main(String[] args) throws Exception{
		//元々の文章：[aid].txt 解析後の文章: re_[aid].txt databese（配列）入りはaid.txtのみ
		//合成後の文章: [pid].txt_aid.txt 合成後かつ解析後の文章: re_[pid].txt_[aid].txt

		//親記事を取ってくる
		String[] top_news = import_newsDB.import_top_news(Article_database);
		int top_num = 0;
		//SentiWordNetのMapを作る
		sentiwordnet = new SentiWordNetDemoCode("SentiWordNet_3.0.0_20130122.txt");

		while(top_num < top_news.length){

			//トピックのcsvを格納するフォルダの製作
			TopicCsvFolder = TopicFolder+top_news[top_num].split("\\.")[0]+"/";
			File new_topic_dir = new File(TopicCsvFolder);
			new_topic_dir.mkdir();

			//Named entityのサブツリーのcsvファイルを格納するフォルダの製作
			EntityTreeCsvFolder = EntityTreeFolder+top_news[top_num].split("\\.")[0]+"/";
			File new_tree_dir = new File(EntityTreeCsvFolder);
			new_tree_dir.mkdir();


			//親記事毎に関連記事を取ってくる
			String[] database = import_newsDB.import_related_news(Article_database,top_news[top_num]);//aid.txtを取り出す(これだけ使う)

			//ファイルの合成
			cut_file.while_combine(database);//~1_2.txt
			StanfordCoreNlpDemo.start_stan1(database);//result_i,result_1_i, input_i,.....

			//エスケープ文字を排除する
			cut_file.all_replace_esc(database);

			//各ファイルのNamed Entityを格納してeventのentity listを作る

			Map<String, ArrayList<String>> entities_list = word_hit.file_entities(database);

			//entityを記事毎に格納
			import_newsDB.import_entities(database, entities_list);
			System.out.println(entities_list);

			ArrayList<String> core_entities = core_entity.check_core(database, entities_list);//core entity生成
			//core entityをデータベースへ格納
			import_newsDB.import_core_entities(database[0], core_entities);

			Map<String, Double> ev_relevance = calculate_rel_div.start_rel(database, core_entities,entities_list);
			Map<String, Double> yorimichi = calculate_rel_div.start_div(database, ev_relevance, entities_list, core_entities);
			Map<String, Double> igiari = positive_score.start_positive(database, entities_list, core_entities);	
			Map<String, Double> detailedness = calculate_detail.dif_detailedness(database,entities_list,core_entities);

			String max_file = null;

			top_num++;}

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
			FileWriter fw = new FileWriter("/Users/admin/Documents/workspace/a_measure.clean/rank_result.csv", true);  //���P
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

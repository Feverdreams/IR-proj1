// slightly modified from SearchFiles in org.apache.lucene.demo
// By Hao Liu, Siwen Zhu

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.*;


public class searcher {
    private searcher() {
    }

    public static void main(String[] args) throws Exception {
        String usage = "Usage:\tjava org.apache.lucene.demo.SearchFiles [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";
        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);
            System.exit(0);
        }

        String index = "index";
        String field = "contents";
        String queries = null;
        String method = "BM25"; // BM25 (baseline), LM, RM1, RM3

        for(int i = 0; i < args.length; ++i) {
            if ("-index".equals(args[i])) {
                index = args[i + 1];
                ++i;
            } else if ("-field".equals(args[i])) {
                field = args[i + 1];
                ++i;
            } else if ("-queries".equals(args[i])) {
                queries = args[i + 1];
                ++i;
            } else if (("-method").equals(args[i])) {
                method = args[i + 1];
                ++i;
            }
        }

        // get queries
        ArrayList<String> querylist = new ArrayList<String>();
        querylist = parser.topicparser(index);

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);

        if (method.equals("BM25")) {
            searcher.setSimilarity(new BM25Similarity());
        }
        else if (method.equals("LM")) {
            searcher.setSimilarity(new LaplaceSimilarity());
        }
        else if (method.equals("RM1")) {
            // TO DO
        }
        else if (method.equals("RM3")) {
            // TO DO
        }

        Analyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser(field, analyzer);
        for(int i = 351; i < 401; i++) {

            Query query = parser.parse(querylist.get(i-351));

            TopDocs results = searcher.search(query, 5); // only for test
            ScoreDoc[] hits = results.scoreDocs;
            int Hits = Math.toIntExact(results.totalHits.value);
            System.out.println("\n" + Hits + " total matching documents");
            for (ScoreDoc hit : hits) {
                Document document = searcher.doc(hit.doc);
                System.out.println("  DocID:" + document.get("docId"));
                System.out.println("  Score:" + hit.score);
                System.out.println("  -----------------------------------");
            }
        }
        reader.close();
    }

}

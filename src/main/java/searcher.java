import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.text.DecimalFormat;

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
        String usage = "Usage:\tjava searcher [-index dir] [-field f] [-repeat n] [-queries file] [-query string] [-raw] [-paging hitsPerPage]\n\nSee http://lucene.apache.org/core/4_1_0/demo/ for details.";
        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);
            System.exit(0);
        }

        String index = "index";
        String field = "contents";
        String queries = null;
        String method = "BM25"; // BM25 (baseline), LM, RM1, RM3
        String qdoc = null;

        int SearchSize = 5;
        int count = 1;

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
            }else if ("-queryfile".equals(args[i])){
                qdoc = args[i + 1];
                ++i;
            }
        }

        if (index == null) {
            System.err.println("Usage: " + usage);
            System.exit(1);
        }

        Path docDir = Paths.get(index);
        if (!Files.isReadable(docDir)) {
            System.out.println("Index directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        // get queries
        ArrayList<String> querylist = new ArrayList<String>();
        if (qdoc == null) {
            querylist = parser.topicparser(queries);

        }
        else{
            //read queries from file
            querylist = parser.fileparser(qdoc);
        }

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
        File temFile = new File("result.txt");
        if (temFile.exists()) { //If output file already exists, del it
            temFile.delete();
        }

        int querynum = querylist.toArray().length;

        for(int i = 9; i < querynum; i++) {

            Query query = parser.parse(querylist.get(i));

            TopDocs results = searcher.search(query, SearchSize); // 5 only for test
            ScoreDoc[] hits = results.scoreDocs;
            int Hits = Math.toIntExact(results.totalHits.value);
            System.out.println("\n" + Hits + " total matching documents");
            try {
                FileOutputStream out = new FileOutputStream("result.txt", true); //append to the end of output file.
                OutputStreamWriter outWriter = new OutputStreamWriter(out, "UTF-8");
                BufferedWriter bufWrite = new BufferedWriter(outWriter);

                for (ScoreDoc hit : hits) {
                    Document document = searcher.doc(hit.doc);
                    StringBuilder sb = new StringBuilder();
                    String res;
                    sb.append(i);
                    sb.append(" \t ");
                    sb.append("Q0");
                    sb.append(" \t ");
                    sb.append(document.get("docId"));
                    sb.append(" \t ");
                    sb.append(count);
                    sb.append(" \t ");
                    DecimalFormat df = new DecimalFormat(".000");
                    sb.append(df.format(hit.score));
                    sb.append(" \t ");
                    sb.append("yhao32");
                    sb.append("\n");
                    res = sb.toString();
                    System.out.print(res);
                    //write res to file
                    bufWrite.write(res);
                    count++;
                    if (count > SearchSize) {
                        count = 1;
                    }
                }
                bufWrite.close();
                outWriter.close();
                out.close();
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("Failed in writing results.txt");
            }
        }
        reader.close();
    }

}

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.demo.IndexFiles;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.*;

public class indexer {
    private indexer() {
    }
    private final static String[] IGNORE_DOCS = {"readchg.txt", "readmefb.txt",  //explain docs of fbis
            "readfrcg", "readmeft",  //explain docs of ft
            "readchg.txt", "readmela.txt" //explain docs of latimes
    };

    public static void main(String[] args) {
        String usage = "java org.apache.lucene.demo.IndexFiles [-index INDEX_PATH] [-docs DOCS_PATH] [-update] [-method METHOD]\n\nThis indexes the documents in DOCS_PATH, creating a Lucene indexin INDEX_PATH that can be searched with SearchFiles";
        String indexPath = "index";
        String docsPath = null;
        boolean create = true;
        String method = "BM25"; // scoring parameter: BM25, LM with Laplace smoothing, RM1, RM3

        for(int i = 0; i < args.length; ++i) {
            if ("-index".equals(args[i])) {
                indexPath = args[i + 1];
                ++i;
            } else if ("-docs".equals(args[i])) {
                docsPath = args[i + 1];
                ++i;
            } else if ("-update".equals(args[i])) {
                create = false;
            } else if (("-method").equals(args[i])) {
                method = args[i + 1];
                ++i;
            }
        }

        if (docsPath == null) {
            System.err.println("Usage: " + usage);
            System.exit(1);
        }

        Path docDir = Paths.get(docsPath);
        if (!Files.isReadable(docDir)) {
            System.out.println("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();

        try {
            System.out.println("Indexing to directory '" + indexPath + "' using method "+ method);
            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            if (method.equals("BM25")) {
                // default: k1 = 1.2 b = 0.75
                iwc.setSimilarity(new BM25Similarity());
            }
            else if (method.equals("LM")) {
                // default: alpha = 1
                iwc.setSimilarity(new LaplaceSimilarity());
            }
            else if (method.equals("RM1")) {
                // TO DO
            }
            else if (method.equals("RM3")) {
                // TO DO
            }

            if (create) {
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            } else {
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocs(writer, docDir, method);
            writer.close();
            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");
        } catch (IOException var12) {
            System.out.println(" caught a " + var12.getClass() + "\n with message: " + var12.getMessage());
        }
    }

    // traverse documents under the dir
    public static void indexDocs(final IndexWriter writer, Path path, String method) throws IOException {
        if (Files.isDirectory(path, new LinkOption[0])) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        indexDoc(writer, file, attrs.lastModifiedTime().toMillis(), method);
                    } catch (IOException var4) {
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            String filename;
            filename = path.getFileName().toString();
            for(String pass:IGNORE_DOCS){
                if (filename.equals(pass)){
                    System.out.print(filename + " passed");
                    System.out.print("\n");
                    return;
                }
            }
            indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis(), method);
        }
    }

    public static String getLineContent(String line) {
        int startIdx = line.indexOf('>');
        int endIdx = line.lastIndexOf('<');
        String LineContent;
        if (startIdx == -1 | endIdx == -1){
             LineContent = line;
        }
        else {
             LineContent = line.substring(startIdx + 1, endIdx);
        }
        return LineContent.trim();
    }

    // Non field parser
    public static void nonfieldParser(IndexWriter writer, Path file) throws IOException {
        InputStream stream = Files.newInputStream(file);
        BufferedReader buff_read = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

        Document doc = new Document();
        StringBuilder sb = new StringBuilder();
        String docId = null;
        String line;
        while ((line = buff_read.readLine()) != null) {
            if (line.startsWith("<DOC>")) {
                doc = new Document();
                sb.setLength(0);
            }
            else if (line.startsWith("<DOCNO>")) {
                docId = getLineContent(line);
            }
            else if (line.startsWith("</DOC>")) {
                doc.add(new StringField("docId", docId, Field.Store.YES));
                doc.add(new TextField("contents", sb.toString(), Store.NO));
                //System.out.println(sb.toString());
                if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                    System.out.println("adding " + docId);
                    writer.addDocument(doc);
                }
                else {
                    System.out.println("updating " + docId);
                    writer.updateDocument(new Term("path", file.toString()), doc);
                }
            }
            else {
                int startidx = line.indexOf('>');
                int lastidx = line.lastIndexOf('<');
                if (startidx == -1 & lastidx == -1){
                    sb.append(line.trim());
                }
                else{
                    if (startidx > lastidx){ ;
                    }
                    else{
                        //System.out.print(line);
                        //System.out.print("\n");
                        if (startidx == lastidx - 1){
                            break;
                        }
                        line = line.substring(startidx + 1, lastidx - 1);
                        int tempstart = line.indexOf('>');
                        if (tempstart != -1){ //In fbis format, there maybe two pairs of <> in one line, check it with this condition.
                            startidx = tempstart;
                            lastidx = line.lastIndexOf('<');
                            if (startidx == lastidx - 1){
                                break;
                            }
                            line = line.substring(startidx + 1, lastidx - 1);
                        }
                        //System.out.print(line);
                        //System.out.print("\n");
                        sb.append(line);

                    }
                }


            }
        }
    }

    public static void indexDoc(IndexWriter writer, Path file, long lastModified, String method) throws IOException {
        String filename;
        filename = file.getFileName().toString();
        for(String pass:IGNORE_DOCS){
            if (filename.equals(pass)){
                System.out.print(filename + " passed");
                System.out.print("\n");
                return;
            }
        }
        if (method.equals("BM25")) {
            nonfieldParser(writer, file);
        }
        else if (method.equals("LM")) {
            nonfieldParser(writer, file);
        }
        else if (method.equals("RM1")) {
            // TO DO
        }
        else if (method.equals("RM3")) {
            // TO DO
        }

    }
}


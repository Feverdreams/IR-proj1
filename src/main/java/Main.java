/*
 * <author>Han He</author>
 * <email>me@hankcs.com</email>
 * <create-date>2020-02-23 6:20 PM</create-date>
 *
 * <copyright file="Main.java">
 * Copyright (c) 2020, Han He. All Rights Reserved, http://www.hankcs.com/
 * See LICENSE file in the project root for full license information.
 * </copyright>
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * @author hankcs
 */
public class Main
{
    public static void main(String[] args) throws IOException, ParseException
    {
        if (args.length != 4)
        {
            System.err.printf("Wrong parameters: %s\nExpecting BM25 IndexPath QueriesPath Output\n", Arrays.toString(args));
        }
        String method = args[0];
        String indexPath = args[1];
        String queryPath = args[2];
        String outputPath = args[3];
        index(indexPath);
        search(method, queryPath, indexPath, outputPath);
    }

    private static void index(String indexPath)
    {
        File indexFile = new File(indexPath);
        boolean create = false;
        if (!indexFile.exists())
        {
            indexFile.mkdirs();
            create = true;
        }
        else if (indexFile.listFiles().length == 0)
        {
            create = true;
        }
        if (create)
        {
            System.err.printf("%s not exists, assume documents are in Data and we are indexing it...\n", indexPath);
            createOrUpdateIndex("Data", indexPath, create);
        }
    }

    static void search(String method, String queries, String index, String outputPath) throws IOException, ParseException
    {
        Map<String, Similarity> similarityMap = new HashMap<>();
        similarityMap.put("BM25".toLowerCase(), new BM25Similarity());
        similarityMap.put("LMLaplace".toLowerCase(), new LaplaceSimilarity());
        Similarity similarity = similarityMap.get(method.toLowerCase());

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        if (similarity != null)
        {
            System.err.println("Use similarity " + method);
            searcher.setSimilarity(similarity);
        }
        Analyzer analyzer = new StandardAnalyzer();
        String field = "contents";
        QueryParser parser = new QueryParser(field, analyzer);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputPath), StandardCharsets.UTF_8));

        int topic_id = 1;
        for (Topic topic : Utils.readTopic(queries))
        {
            String line = topic.getQuery();
            Query query = parser.parse(line);
            System.err.println("Searching for: " + query.toString(field));
            TopDocs topDocs = searcher.search(query, 1000);

            for (int i = 0; i < topDocs.scoreDocs.length; i++)
            {
                ScoreDoc scoreDoc = topDocs.scoreDocs[i];
                Document doc = searcher.doc(scoreDoc.doc);
                String contents = doc.getField("contents").stringValue();
                String docno = doc.getField("docno").stringValue();
                int rank = i + 1;
                bw.write(String.format("%d\tQ0\t%s\t%d\t%.1f\thhe43\n", topic_id, docno, rank, scoreDoc.score));
            }
            topic_id += 1;
        }
        bw.close();
        reader.close();
    }

    static void createOrUpdateIndex(String docsPath, String indexPath, boolean create)
    {
        final Path docDir = Paths.get(docsPath);
        if (!Files.isReadable(docDir))
        {
            System.out.println("Document directory '" + docDir.toAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        Date start = new Date();
        try
        {
            System.out.println("Indexing to directory '" + indexPath + "'...");

            Directory dir = FSDirectory.open(Paths.get(indexPath));
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            if (create)
            {
                // Create a new index in the directory, removing any
                // previously indexed documents:
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            }
            else
            {
                // Add new documents to an existing index:
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            }

            // Optional: for better indexing performance, if you
            // are indexing many documents, increase the RAM
            // buffer.  But if you do this, increase the max heap
            // size to the JVM (eg add -Xmx512m or -Xmx1g):
            //
            // iwc.setRAMBufferSizeMB(256.0);

            IndexWriter writer = new IndexWriter(dir, iwc);
            indexDocs(writer, docDir);

            // NOTE: if you want to maximize search performance,
            // you can optionally call forceMerge here.  This can be
            // a terribly costly operation, so generally it's only
            // worth it when your index is relatively static (ie
            // you're done adding documents to it):
            //
            // writer.forceMerge(1);

            writer.close();

            Date end = new Date();
            System.out.println(end.getTime() - start.getTime() + " total milliseconds");

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Indexes the given file using the given writer, or if a directory is given,
     * recurses over files and directories found under the given directory.
     * <p>
     * NOTE: This method indexes one document per input file.  This is slow.  For good
     * throughput, put multiple documents into your input file(s).  An example of this is
     * in the benchmark module, which can create "line doc" files, one document per line,
     * using the
     * <a href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
     * >WriteLineDocTask</a>.
     *
     * @param writer Writer to the index where the given file/dir info will be stored
     * @param path   The file to index, or the directory to recurse into to find files to index
     * @throws IOException If there is a low-level I/O error
     */
    static void indexDocs(final IndexWriter writer, Path path) throws IOException
    {
        if (Files.isDirectory(path))
        {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>()
            {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
                {
                    try
                    {
                        indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
                    }
                    catch (IOException ignore)
                    {
                        // don't index files that can't be read.
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        else
        {

            indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    /**
     * Indexes a single document
     */
    static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException
    {
        String dirname = file.getParent().getFileName().toString();
        if (!dirname.endsWith("latimes") && !dirname.endsWith("fbis") && !dirname.endsWith("ft"))
        {
            return;
        }
        String filename = file.getFileName().toString();
        if (filename.contains(".") || filename.contains("read"))
        {
            return;
        }
        try (InputStream stream = Files.newInputStream(file))
        {
            // make a new, empty document

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            List<Doc> parsedDoc = Utils.readDoc(reader);
            for (Doc d : parsedDoc)
            {
                Document doc = new Document();

                // Add the path of the file as a field named "path".  Use a
                // field that is indexed (i.e. searchable), but don't tokenize
                // the field into separate words and don't index term frequency
                // or positional information:
                Field pathField = new StringField("path", file.toString(), Field.Store.YES);
                doc.add(pathField);

                doc.add(new StringField("docno", d.docno, Field.Store.YES));

                // Add the last modified date of the file a field named "modified".
                // Use a LongPoint that is indexed (i.e. efficiently filterable with
                // PointRangeQuery).  This indexes to milli-second resolution, which
                // is often too fine.  You could instead create a number based on
                // year/month/day/hour/minutes/seconds, down the resolution you require.
                // For example the long value 2011021714 would mean
                // February 17, 2011, 2-3 PM.
                doc.add(new LongPoint("modified", lastModified));

                // Add the contents of the file to a field named "contents".  Specify a Reader,
                // so that the text of the file is tokenized and indexed, but not stored.
                // Note that FileReader expects the file to be in UTF-8 encoding.
                // If that's not the case searching for special characters will fail.
                doc.add(new TextField("contents", d.content, Field.Store.YES));

                if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE)
                {
                    // New index, so we just add the document (no old document can be there):
                    System.out.println("adding " + file + " " + d.docno);
                    writer.addDocument(doc);
                }
                else
                {
                    // Existing index (an old copy of this document may have been indexed) so
                    // we use updateDocument instead to replace the old one matching the exact
                    // path, if present:
                    System.out.println("updating " + file + " " + d.docno);
                    writer.updateDocument(new Term("path", file.toString()), doc);
                }
            }
        }
    }
}

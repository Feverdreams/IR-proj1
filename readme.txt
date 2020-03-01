
The following command will search with RM3 with index built if the index doesn't exist.
Note that we used a customized Analyzer with stemming and stopwords so it doesn't work with ordinary index files.

java -jar HW1.jar RM3 index topics.351-400 output.txt path_to_docs(optional if index doesn't exit)

Available search methods are:

- BM25
- LMLaplace
- LMDirichlet
- LMJelinekMercer
- RM1
- RM3
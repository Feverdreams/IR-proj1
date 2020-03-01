
The following command will search with RM3 with index built if the index doesn't exist.

java -jar target/proj1-1.0-SNAPSHOT-jar-with-dependencies.jar RM3 Data/index Data/topics.351-400 Data/output.txt Data

Available search methods are:

- BM25
- LMLaplace
- LMDirichlet
- LMJelinekMercer
- RM1
- RM3
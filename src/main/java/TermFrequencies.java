public class TermFrequencies
{
    String term;
    long tf;
    long df;
    double idf;

    public TermFrequencies(String term, long tf, long df)
    {
        this.term = term;
        this.tf = tf;
        this.df = df;
    }

    public TermFrequencies(String term, long tf, long df, double idf)
    {
        this.term = term;
        this.tf = tf;
        this.df = df;
        this.idf = idf;
    }
}

public class TermFrequencies
{
    String term;
    long tf;
    long df;

    public TermFrequencies(String term, long tf, long df)
    {
        this.term = term;
        this.tf = tf;
        this.df = df;
    }
}

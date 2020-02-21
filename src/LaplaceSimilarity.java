import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMSimilarity;


public class LaplaceSimilarity extends LMSimilarity {
    /** The &mu; parameter. */

    private final float alpha;

    public LaplaceSimilarity(CollectionModel collectionModel, float alpha) {

        super(collectionModel);

        if (!Float.isFinite(alpha) || alpha < 0) {

            throw new IllegalArgumentException("illegal alpha value: " + alpha + ", must be a non-negative finite value");

        }

        this.alpha = alpha;

    }



    /** Instantiates the similarity with the provided &mu; parameter. */

    public LaplaceSimilarity(float alpha) {

        if (!Float.isFinite(alpha) || alpha < 0) {

            throw new IllegalArgumentException("illegal alpha value: " + alpha + ", must be a non-negative finite value");

        }

        this.alpha = alpha;

    }



    /** Instantiates the similarity with the default &mu; value of 2000. */

    public LaplaceSimilarity(CollectionModel collectionModel) {

        this(collectionModel, 1);

    }



    /** Instantiates the similarity with the default &mu; value of 2000. */

    public LaplaceSimilarity() {

        this(1);

    }
    @Override

    protected double score(BasicStats stats, double freq, double docLen) {

        double score = stats.getBoost() * (Math.log((freq+alpha) /

                (docLen + stats.getNumberOfDocuments())));

        return score > 0.0d ? score : 0.0d;

    }



    @Override

    protected void explain(List<Explanation> subs, BasicStats stats,

                           double freq, double docLen) {

        if (stats.getBoost() != 1.0d) {

            subs.add(Explanation.match((float) stats.getBoost(), "query boost"));

        }

        double p = stats.getNumberOfDocuments();

        Explanation explP = Explanation.match((float) p,

                "|v|, types of documents");

        Explanation explFreq = Explanation.match((float) freq,

                "freq, number of occurrences of term in the document");



        subs.add(Explanation.match(alpha, "alpha"));

        Explanation weightExpl = Explanation.match(

                (float)(Math.log((freq+alpha) /

                        (docLen + stats.getNumberOfDocuments()))),

                "term weight, computed as log((alpha + freq) from:",

                explFreq);

        subs.add(weightExpl);

        subs.add(Explanation.match(

                (float)Math.log(docLen + stats.getNumberOfDocuments()),

                "document norm, computed as log(dl + types of docs)"));

        subs.add(Explanation.match((float) docLen,"dl, length of field"));

        super.explain(subs, stats, freq, docLen);

    }



    @Override

    protected Explanation explain(

            BasicStats stats, Explanation freq, double docLen) {

        List<Explanation> subs = new ArrayList<>();

        explain(subs, stats, freq.getValue().doubleValue(), docLen);



        return Explanation.match(

                (float) score(stats, freq.getValue().doubleValue(), docLen),

                "score(" + getClass().getSimpleName() + ", freq=" +

                        freq.getValue() +"), computed as boost * " +

                        "(term weight + document norm) from:",

                subs);

    }

    /** Returns the &mu; parameter. */

    public float getAlpha() {

        return alpha;

    }



    @Override

    public String getName() {

        return String.format(Locale.ROOT, "Laplace(%f)", getAlpha());

    }
}

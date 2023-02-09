package memorypool;

public class Record {
    private String tconst;
    private float avgRating;
    private int numVotes;

    public Record(String tconst, float avgRating, int numVotes) {
        this.tconst = tconst;
        this.avgRating = avgRating;
        this.numVotes = numVotes;
    }

    public String getTconst() {
        return tconst;
    }

    public float getAvgRating() {
        return avgRating;
    }

    public int getNumVotes() {
        return numVotes;
    }
}

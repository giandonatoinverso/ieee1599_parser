package ieee1599;

/*
@author Giandonato Inverso <giandonato.inverso@studenti.unimi.it>

A Rest class
 */
public class Rest implements Measure {
    private String event_ref;
    private int num;
    private int den;
    private final String octave;
    private final String step;
    private final String actualAccidental;

    public Rest(String event_ref, int num, int den) {
        if(event_ref == null)
            throw new IllegalArgumentException("event_ref == null");
        if(num <= 0)
            throw new IllegalArgumentException("num < 0");
        if(den <= 0)
            throw new IllegalArgumentException("den < 0");

        this.event_ref = event_ref;
        this.num = num;
        this.den = den;
        this.octave = null;
        this.step = null;
        this.actualAccidental = null;
    }

    public String getEvent_ref() {
        return this.event_ref;
    }

    @Override
    public int getType() {
        return 1;
    }

    public int getNum() {
        return this.num;
    }

    public int getDen() {
        return this.den;
    }

    @Override
    public String getOctave() {
        return this.octave;
    }

    @Override
    public String getStep() {
        return this.step;
    }

    @Override
    public String getActualAccidental() {
        return this.actualAccidental;
    }

    @Override
    public void setEvent_ref(String event_ref) {
        if(event_ref == null)
            throw new IllegalArgumentException("event_ref == null");

        this.event_ref = event_ref;
    }

    @Override
    public void setNum(int num) {
        if(num <= 0)
            throw new IllegalArgumentException("num < 0");

        this.num = num;
    }

    @Override
    public void setDen(int den) {
        if(den <= 0)
            throw new IllegalArgumentException("den < 0");

        this.den = den;
    }

    @Override
    public void setOctave(int octave) {
        if(octave != 0)
            throw new IllegalArgumentException("octave != 0");
    }

    @Override
    public void setStep(String step) {
        if(step != null)
            throw new IllegalArgumentException("step != null");
    }

    @Override
    public void setActualAccidental(String actualAccidental) {
        if(actualAccidental != null)
            throw new IllegalArgumentException("actualAccidental != null");
    }

    @Override
    public String toString() {
        return "event_ref: " + this.event_ref + " num: " + this.num + " den: " + this.den;
    }
}


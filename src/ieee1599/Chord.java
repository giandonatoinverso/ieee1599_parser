package ieee1599;

import java.util.Arrays;

/*
@author Giandonato Inverso <giandonato.inverso@studenti.unimi.it>

A Chord class
 */
public class Chord implements Measure {
    private String event_ref;
    private int num;
    private int den;
    private String octave;
    private String step;
    private String actualAccidental;
    private static final String[] stepArray = {"C", "D", "E", "F", "G", "A", "B"};
    private static final String[] accidentalArray = {"natural", "sharp", "flat",""};

    public Chord(String event_ref, int num, int den, int octave, String step, String actualAccidental) {
        if(event_ref == null)
            throw new IllegalArgumentException("event_ref == null");
        if(num <= 0)
            throw new IllegalArgumentException("num < 0");
        if(den <= 0)
            throw new IllegalArgumentException("den < 0");
        if(octave <= 0)
            throw new IllegalArgumentException("octave < 0");
        if(!Arrays.asList(stepArray).contains(step))
            throw new IllegalArgumentException("wrong step");

        if(!Arrays.asList(accidentalArray).contains(actualAccidental))
            throw new IllegalArgumentException("wrong actualAccidental");

        this.event_ref = event_ref;
        this.num = num;
        this.den = den;
        this.octave = String.valueOf(octave);
        this.step = step;
        this.actualAccidental = actualAccidental;
    }

    @Override
    public String getEvent_ref() {
        return this.event_ref;
    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getNum() {
        return this.num;
    }

    @Override
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
            throw new IllegalArgumentException("num != 0");

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
        this.octave = String.valueOf(octave);

        if(octave <= 0)
            throw new IllegalArgumentException("octave < 0");
    }

    @Override
    public void setStep(String step) {
        if(!Arrays.asList(stepArray).contains(step))
            throw new IllegalArgumentException("wrong step");

        this.step = step;
    }

    @Override
    public void setActualAccidental(String actualAccidental) {
        if(!Arrays.asList(accidentalArray).contains(actualAccidental))
            throw new IllegalArgumentException("wrong actualAccidental");

        this.actualAccidental = actualAccidental;
    }

    @Override
    public String toString() {
        return "event_ref: " + this.event_ref + " den: " + this.den + " octave " + this.octave + " step " + this.step + " actualAccidental " + this.actualAccidental;
    }
}


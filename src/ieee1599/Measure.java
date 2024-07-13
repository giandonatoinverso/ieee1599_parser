package ieee1599;

/*
@author Giandonato Inverso <giandonato.inverso@studenti.unimi.it>

Measure abstract data type
 */
public interface Measure {
    String getEvent_ref();
    int getType();
    int getNum();
    int getDen();
    String getOctave();
    String getStep();
    String getActualAccidental();

    void setEvent_ref(String event_ref);
    void setNum(int den);
    void setDen(int den);
    void setOctave(int octave);
    void setStep(String step);
    void setActualAccidental(String actualAccidental);
}
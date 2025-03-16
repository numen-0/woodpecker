package com.example.woodpecker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MorseCharSet implements Serializable {

    public static class MorseChar implements Serializable {
        public String rep;
        public String sequence;

        public MorseChar(String rep, String sequence) {
            this.rep = rep;
            this.sequence = sequence;
        }
    }

    public final String name;
    private final Map<String, MorseChar> seqToRepMap = new HashMap<>();
    private final Map<String, MorseChar> repToSeqMap = new HashMap<>();

    public MorseCharSet(String name) {
        this.name = name;
    }

    public Boolean remove(MorseChar mc) {
        if ( !this.repToSeqMap.containsKey(mc.rep) && !this.seqToRepMap.containsKey(mc.sequence) ) {
            return false;
        }
        this.repToSeqMap.remove(mc.rep);
        this.seqToRepMap.remove(mc.sequence);
        return true;
    }
    public Boolean add(String rep, String sequence) {
        if ( this.repToSeqMap.containsKey(rep) || this.seqToRepMap.containsKey(sequence) ) {
            return false;
        }
        MorseChar mc = new MorseChar(rep, sequence);
        this.repToSeqMap.put(rep, mc);
        this.seqToRepMap.put(sequence, mc);
        return true;
    }

    public Boolean updateSeq(String rep, String sequence) {
        if ( !this.repToSeqMap.containsKey(rep) || this.seqToRepMap.containsKey(sequence) ) {
            return false;
        }
        MorseChar mc = new MorseChar(rep, sequence);
        MorseChar mc_old = this.repToSeqMap.put(rep, mc);

        assert mc_old != null;
        this.seqToRepMap.remove(mc_old.sequence);

        this.seqToRepMap.put(sequence, mc);
        return true;
    }
    public Boolean updateRep(String rep, String sequence) {
        if ( this.repToSeqMap.containsKey(rep) || !this.seqToRepMap.containsKey(sequence) ) {
            return false;
        }
        MorseChar mc = new MorseChar(rep, sequence);
        MorseChar mc_old = this.seqToRepMap.put(sequence, mc);

        assert mc_old != null;
        this.repToSeqMap.remove(mc_old.rep);

        this.repToSeqMap.put(rep, mc);
        return true;
    }

    public MorseChar getRep(String sequence) {
        return this.seqToRepMap.get(sequence);
    }
    public MorseChar getSequence(String rep) {
        return this.repToSeqMap.get(rep);
    }

    public ArrayList<MorseChar> getCharset() {
        return new ArrayList<>(this.repToSeqMap.values());
    }
}

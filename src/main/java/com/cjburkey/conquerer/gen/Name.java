package com.cjburkey.conquerer.gen;

import com.cjburkey.conquerer.util.Util;
import org.joml.Random;

import static org.joml.Math.*;

/**
 * Created by CJ Burkey on 2019/01/12
 */
public final class Name {
    
    private static final String[] consonants = new String[] {
            // Monographs
            "b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "q", "r", "s", "t", "v", "w", "x", "y", "z",
            
            // Digraphs
            "st", "rt", "pt", "ck", "sm", "sc", "sk", "sl",
            "ws", "rs", "ts", "ys", "ps", "ss", "ds", "fs", "gs", "ks", "ls", "zs", "xs", "cs", "vs", "bs", "ns", "ms",
            
            // Trigraphs
            "sch", "sck",
    };
    
    private static final String[] vowels = new String[] {
            // Monographs
            "a", "e", "i", "o", "u",
            
            // Digraphs
            "aa", "ee", "ii", "oo", "uu",
            
            // Diphthongs
            "ae", "ai", "ao", "au",
            "ea", "ei", "eo", "eu",
            "ia", "ie", "io", "iu",
            "oa", "oe", "oi", "ou",
            "ua", "ue", "ui", "uo",
    };
    
    // Alternates between random monographic and digraphic consonants and vowels until the generated name reaches a length somewhere within [min, max]
    public static String generateName(Random random, int minLengthInc, int maxLengthInc) {
        StringBuilder builder = new StringBuilder();
        boolean vowel = Util.nextInt(random, 0, 1) == 0;
        int length = Util.nextInt(random, minLengthInc, maxLengthInc);
        while (builder.length() < length) {
            String[] from = vowel ? vowels : consonants;
            vowel = !vowel;
            builder.append(from[random.nextInt(from.length)]);
        }
        return builder.substring(0, min(builder.length(), maxLengthInc));
    }
    
}

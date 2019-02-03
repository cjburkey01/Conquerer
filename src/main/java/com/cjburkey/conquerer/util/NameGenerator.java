package com.cjburkey.conquerer.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Arrays;
import org.joml.Random;

import static com.cjburkey.conquerer.util.Util.*;

/**
 * Created by CJ Burkey on 2019/01/12
 * <p>
 * NOTE: THIS FILE CONTAINS A LIST OF OFFENSIVE (RACIST, SLURS, SWEARS, etc) WORDS AT THE END
 * IF YOU DON'T WANT TO SEE IT, DON'T LOOK
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "unused"})
public final class NameGenerator {

    private final String[] consonants;
    private final String[] vowels;

    private int firstConsDigraph = Integer.MAX_VALUE;
    private int firstVowelDigraph = Integer.MAX_VALUE;
    private boolean canFirstLetterBeDigraph = false;
    private boolean shouldCapitalizeFirstCharacter = true;
    private int minLengthInc = 3;
    private int maxLengthInc = 7;
    private NameFilter nameFilter = null;

    // Ensure that all single character letters preceed multi-character letters in the vowels and consonants arrays
    public NameGenerator(String[] consonants, String[] vowels) {
        this.consonants = consonants;
        this.vowels = vowels;

        for (int i = 0; i < consonants.length; i++) {
            if (consonants[i].length() > 1) {
                firstConsDigraph = i;
                break;
            }
        }
        for (int i = 0; i < vowels.length; i++) {
            if (vowels[i].length() > 1) {
                firstVowelDigraph = i;
                break;
            }
        }

        setNameFilter(generateDefaultBadWordsFilter());
    }

    public NameGenerator() {
        this(new String[] {
                // Monographs
                "b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "q", "r", "s", "t", "v", "w", "x", "y", "z",

                // Digraphs
                "st", "rt", "pt", "ck", "sm", "sc", "sk", "sl",
                "ws", "rs", "ts", "ys", "ps", "ss", "ds", "fs", "gs", "ks", "ls", "zs", "xs", "cs", "vs", "bs", "ns", "ms",

                // Trigraphs
                "sch", "sck",
        }, new String[] {
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
        });
    }

    public NameGenerator setCanFirstLetterBeDigraph(boolean canFirstLetterBeDigraph) {
        this.canFirstLetterBeDigraph = canFirstLetterBeDigraph;
        return this;
    }

    public boolean canFirstLetterBeDigraph() {
        return canFirstLetterBeDigraph;
    }

    public NameGenerator setShouldCapitalizeFirstCharacter(boolean shouldCapitalizeFirstCharacter) {
        this.shouldCapitalizeFirstCharacter = shouldCapitalizeFirstCharacter;
        return this;
    }

    public boolean shouldCapitalizeFirstCharacter() {
        return shouldCapitalizeFirstCharacter;
    }

    public int getMinLength() {
        return minLengthInc;
    }

    public NameGenerator setMinLength(int minLengthInc) {
        this.minLengthInc = minLengthInc;
        return this;
    }

    public int getMaxLength() {
        return maxLengthInc;
    }

    public NameGenerator setMaxLength(int maxLengthInc) {
        this.maxLengthInc = maxLengthInc;
        return this;
    }

    public NameFilter getNameFilter() {
        return nameFilter;
    }

    public NameGenerator setNameFilter(NameFilter nameFilter) {
        this.nameFilter = nameFilter;
        return this;
    }

    // Alternates between random monographic and digraphic consonants and vowels until the generated name reaches a length somewhere within [min, max]
    public String generate(Random random) {
        // Keep looking until the name is considered valid by the provided filter
        // This is the second time I've used a "do...while" loop in this project and the third time EVER
        CharSequence output;
        do {
            // Allow string mutation with LESS reallocation (thus, it should be faster but less memory efficient)
            StringBuilder builder = new StringBuilder();

            // Randomly start on vowel or consonant
            boolean vowel = (nextInt(random, 0, 1) == 0);

            // Predetermine length in the range [min, max]
            int length = nextInt(random, minLengthInc, maxLengthInc);

            // Loop until the length of the name meets or exceeds the predetermined name length
            while (builder.length() < length) {
                // Get a character from the correct array
                String[] from = (vowel ? vowels : consonants);

                // If this is the first character and "canFirstLetterBeDigraph" is true, make sure that the first "letter" is a single character
                String character = from[random.nextInt(
                        (canFirstLetterBeDigraph || (builder.length() > 0))
                                ? from.length
                                : (vowel ? firstVowelDigraph : firstConsDigraph))];
                builder.append(character);

                // Make next character/digraph/etc the opposite type of the current type
                vowel = !vowel;
            }

            // Make the first letter capitalized
            if (shouldCapitalizeFirstCharacter) builder.setCharAt(0, Character.toUpperCase(builder.charAt(0)));

            // Return the name which must conform to the specified bounds
            // Chops off any excess if necessary
            output = builder.substring(0, min(builder.length(), maxLengthInc));
        } while (nameFilter != null && !nameFilter.isValid(output));

        // Convert the output string builder into a string
        return output.toString();
    }

    // Generates <COUNT> names and returns in an array for faster bulk name generation
    public String[] generate(int count, Random random) {
        if (count <= 0) return new String[0];
        String[] names = new String[count];
        for (int i = 0; i < count; i++) names[i] = generate(random);
        return names;
    }

    // OFFENSIVE WORDS ALERT!
    // I had to type all this out and it physically hurt, but I did it to prevent anything bad from coming up in-game
    // If you're easily offended by things like this, I apologize :(
    // You may refer to this as the repository of things I don't want to see in the game
    private NameFilter generateDefaultBadWordsFilter() {
        return new BasicNameFilter()
                .addCensor(
                        "fuck",
                        "fucking",
                        "fucked",
                        "fucker",
                        "motherfucker",
                        "motherfucking",
                        "bitch",
                        "shit",
                        "shitter",
                        "piss",
                        "pisser",
                        "cock",
                        "dick",
                        "pussy",
                        "penis",
                        "vagina",
                        "damn",
                        "damned",
                        "dammit",
                        "ass",
                        "cuck",

                        // It gets racist, I'm sorry again D: D:
                        "nigger",
                        "kike",
                        "jew",
                        "fag",
                        "faggot"
                );
    }

    @FunctionalInterface
    public interface NameFilter {

        boolean isValid(CharSequence name);
    }

    public class BasicNameFilter implements NameFilter {

        private final ObjectOpenHashSet<String> illegal = new ObjectOpenHashSet<>();

        public BasicNameFilter addCensor(String value) {
            illegal.add(value);
            return this;
        }

        public BasicNameFilter addCensor(String... values) {
            illegal.addAll(Arrays.asList(values));
            return this;
        }

        public BasicNameFilter clearCensor() {
            illegal.clear();
            return this;
        }

        public boolean isValid(CharSequence name) {
            return !illegal.contains(name.toString());
        }
    }

}

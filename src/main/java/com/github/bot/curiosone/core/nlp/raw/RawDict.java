package com.github.bot.curiosone.core.nlp.raw;

import com.github.bot.curiosone.core.nlp.LEX;
import com.github.bot.curiosone.core.nlp.POS;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.WordnetStemmer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * RawDict for tokenizer using WordNet DB.
 *
 * @author Andrea Rivitto && Eugenio Schintu
 * @see https://wordnet.princeton.edu/
 */
public class RawDict {

  /**
   * Singleton instance on class loading is thread-safe.
   */
  private static RawDict instance = null;

  /**
   * Path of Wordnet database files.
   */
  private static final String wdnPath = "/dict";

  /**
   * Dictionary.
   */
  private Dictionary dictionary;

  /**
   * Private constructor.
   */
  private RawDict() {
    try {
      Path path = null;
      try {
        URL resource = RawDict.class.getResource(wdnPath);
        path = Paths.get(resource.toURI());
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
      dictionary = new Dictionary(new URL("file", null, path.toString()));
      dictionary.open();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /** Returns the RawDict. */
  public static RawDict getInstance() {
    if  (instance != null) {
      return instance;
    }
    instance = new RawDict();
    return instance;
  }

  /**
   * Creates RawToken Structure that contains dict info.
   * @param item String to be searched in WordNet
   * @return RawToken Structure that contains RawDict info
   * @see com.github.bot.curiosone.core.nlp.raw.RawToken
   */
  public RawToken getRawToken(String item) {
    if (item.length() == 0 || item.equals(" ")) {
      return null;
    }
    RawToken token = new RawToken(item);
    token = getRawTokenNotWn(token, item);
    token = getRawTokenWn(token, item);
    return token;
  }

  /**
   * Pronouns outside WN.
   */
  private enum PronounsOutWn {

      PERSONAL_SUBJECTIVE("i", "you", "he", "she", "it", "we", "you", "they"),
      PERSONAL_OBJECTIVE("me", "you", "him", "her", "it", "us", "you", "them"),
      POSSESSIVE("mine", "yours", "his", "hers", "ours", "theirs"),
      REFLEXIVE("myself", "yourself", "himself", "herself", "itself", "oneself",
          "ourselves", "yourselves", "themselves"),
      RECIPROCAL("each", "other", "one", "another"),
      RELATIVE("that", "which", "who", "whose", "whom", "where", "when"),
      DEMONSTRATIVE("this", "that", "these", "those"),
      INTERROGATIVE("who", "what", "why", "where", "when", "whatever"),
      INDEFINITE("anything", "anybody", "anyone", "something", "somebody",
              "someone", "nothing", "nobody", "none", "no one");

    private String[] items;

    private PronounsOutWn(String...items) {
      this.items = items;
    }

    public String[] getItems() {
      return items;
    }
  }

  /**
   * Determiners outside WN.
   */
  private enum DeterminersOutWn {

    INDEFINITE_ARTICLE("a", "an"),
    DEFINITE_ARTICLE("the");

    private String[] items;

    private DeterminersOutWn(String...items) {
      this.items = items;
    }

    public String[] getItems() {
      return items;
    }
  }

  /**
   * Conjunctions outside WN.
   */
  private enum ConjunctionsOutWn {

    COORDINATOR("and", "or", "but"),
    SUBORDINATOR("while", "because", "before", "since", "till", "unless", "whereas", "wheter");

    private String[] items;

    private ConjunctionsOutWn(String...items) {
      this.items = items;
    }

    public String[] getItems() {
      return items;
    }
  }

  /**
   * Adverbs outside WN.
   */
  private enum AdverbsOutWn {

    INTERROGATIVE("how");

    private String[] items;

    private AdverbsOutWn(String...items) {
      this.items = items;
    }

    public String[] getItems() {
      return items;
    }
  }

  /**
   * Interjections outside WN.
   */
  private enum InterjectionsOutWn {

    GENERIC("ah", "eh", "hmm", "phew", "tsk", "uhm"),
    REGARDS("bye", "goodbye", "hello", "farewell", "hi"),
    APOLOGIZE("so long excuse me", "sorry", "pardon", "i am sorry", "i'm sorry"),
    GRATITUDE("thanks", "thank you", "thanks a lot"),
    DISGUST("yuk"),
    SURPRISE("oh"),
    PAIN("ouch", "ohi");

    private String[] items;

    private InterjectionsOutWn(String...items) {
      this.items = items;
    }

    public String[] getItems() {
      return items;
    }
  }

  /**
   * Get token outside of WordNet Database.
   * @param token         [description]
   * @param item          [description]
   * @return          [description]
   */
  private RawToken getRawTokenNotWn(RawToken token, String item) {

    // Check Numeric.
    if (isNumeric(item)) {
      token.setKnown(true);
      RawWord retWord = new RawWord();
      retWord.setLemma(item);
      retWord.setPos(POS.NUMB);
      retWord.setLexType(LEX.QUANTITY);
      retWord.setGloss("Numeric outside WordNet");
      token.addWord(retWord);
      return token;
    }

    // Check mail address.
    if (isValidEmailAddress(item)) {
      token.setKnown(true);
      RawWord retWord = new RawWord();
      retWord.setLemma(item);
      retWord.setPos(POS.N);
      retWord.setLexType(LEX.MAIL);
      retWord.setGloss("Mail address outside WordNet");
      token.addWord(retWord);
      return token;
    }

    // Check Pronouns.
    for (PronounsOutWn n: PronounsOutWn.values()) {
      if (!contains(n.getItems(),item)) {
        continue;
      }

      token.setKnown(true);
      RawWord retWord = new RawWord();
      retWord.setLemma(item);
      retWord.setPos(POS.PRON);
      retWord.setLexType(LEX.valueOf(n.toString()));
      retWord.setGloss("Pronoun outside WordNet");
      token.addWord(retWord);
      return token;
    }

    // Check Determiners.
    for (DeterminersOutWn n: DeterminersOutWn.values()) {
      if (!contains(n.getItems(),item)) {
        continue;
      }

      token.setKnown(true);
      RawWord retWord = new RawWord();
      retWord.setLemma(item);
      retWord.setPos(POS.DET);
      retWord.setLexType(LEX.valueOf(n.toString()));
      retWord.setGloss("Determiners outside WordNet");
      token.addWord(retWord);
      return token;
    }

    // Check Conjunctions.
    for (ConjunctionsOutWn n: ConjunctionsOutWn.values()) {
      if (!contains(n.getItems(),item)) {
        continue;
      }

      token.setKnown(true);
      RawWord retWord = new RawWord();
      retWord.setLemma(item);
      retWord.setPos(POS.CONJ);
      retWord.setLexType(LEX.valueOf(n.toString()));
      retWord.setGloss("Conjunctions outside WordNet");
      token.addWord(retWord);
      return token;
    }

    // Check Interjections.
    for (InterjectionsOutWn n: InterjectionsOutWn.values()) {
      if (!contains(n.getItems(),item)) {
        continue;
      }

      token.setKnown(true);
      RawWord retWord = new RawWord();
      retWord.setLemma(item);
      retWord.setPos(POS.INTERJ);
      retWord.setLexType(LEX.valueOf(n.toString()));
      retWord.setGloss("Interjections outside WordNet");
      token.addWord(retWord);
      return token;
    }

    // Check Adverbs.
    for (AdverbsOutWn n: AdverbsOutWn.values()) {
      if (!contains(n.getItems(),item)) {
        continue;
      }

      token.setKnown(true);
      RawWord retWord = new RawWord();
      retWord.setLemma(item);
      retWord.setPos(POS.ADV);
      retWord.setLexType(LEX.valueOf(n.toString()));
      retWord.setGloss("Adverbs outside WordNet");
      token.addWord(retWord);
      return token;
    }
    return token;
  }

  /**
   * Get token from WordNet Database.
   * List of words descending ordered based on frequency occurrence (getTagCount()).
   * @param token [description]
   * @param item [description]
   * @return [description]
   * @see https://stackoverflow.com/questions/21264158/how-to-access-frequency-count-in-wordnet-in-any-java-wordnet-interface
   */
  private RawToken getRawTokenWn(RawToken token, String item) {
    Set<RawWord> retWords = new HashSet<RawWord>();

    for (edu.mit.jwi.item.POS p : edu.mit.jwi.item.POS.values()) {
      List<String> stems = new WordnetStemmer(dictionary).findStems(item, p);

      for (String lemma : stems) {
        IIndexWord indexWord = dictionary.getIndexWord(lemma, p);
        if (indexWord != null) {
          List<IWordID> wordIDs = indexWord.getWordIDs();
          for (IWordID id : wordIDs) {
            RawWord retWord = new RawWord();
            IWord word = dictionary.getWord(id);

            retWord.setLemma(lemma);
            switch (p) {
              case NOUN:
                retWord.setPos(POS.N);
                break;
              case VERB:
                retWord.setPos(POS.V);
                break;
              case ADJECTIVE:
                retWord.setPos(POS.ADJ);
                break;
              case ADVERB:
                retWord.setPos(POS.ADV);
                break;
              default: retWord.setPos(POS.UNKN);
            };
            retWord.setLexType(LEX.valueOf(
                word.getSynset()
                  .getLexicalFile()
                  .getName()
                  .split("\\.")[1]
                  .toUpperCase()
            ));
            retWord.setGloss(word.getSynset().getGloss());
            retWord.setWordId(id);
            retWord.setNum(
                dictionary.getSenseEntry(word.getSenseKey()).getTagCount()
            );

            // Get semantic relations from synset.
            ISynset synset = word.getSynset();

            for (Pointer pt: Pointer.values()) {
              List<ISynsetID> synList = synset.getRelatedSynsets(pt) ;
              List<IWord> words;
              for (ISynsetID sid: synList) {
                words = dictionary.getSynset(sid).getWords();
                for (Iterator<IWord> i = words.iterator(); i.hasNext();) {
                  retWord.addRelation(pt.toString(), i.next().getLemma());
                }
              }
            }

            // Get lexical relations from word.
            for (Pointer pt: Pointer.values()) {
              for (IWordID wid: word.getRelatedWords(pt)) {
                retWord.addRelation(pt.toString(), dictionary.getWord(wid).getLemma());
              }
            }

            // Add retWord
            retWords.add(retWord);
          }
        }
      }
    }

    Comparator<RawWord> cmp = Comparator.comparing(RawWord::getNum).reversed();
    List<RawWord> retOrderedWords = new ArrayList<>();
    retOrderedWords.addAll(retWords);
    Collections.sort(retOrderedWords, cmp);
    if (retOrderedWords.size() > 0) {
      token.setKnown(true);
      token.addAllWords(retOrderedWords);
    }

    return token;
  }

  /**
   * Verify if an array of String contains a String.
   * @param array [description]
   * @param searchValue [description]
   * @return [description]
   */
  private static <T> boolean contains(T[] array, T searchValue) {
    return Arrays.stream(array).anyMatch(searchValue::equals);
  }

  /**
   * Checks if the given email address is valid.
   * @param email the email address to be validated.
   * @return <code>true</code> if the given email address is valid;
             <code>false</code> otherwise
   * @see http://howtodoinjava.com/regex/java-regex-validate-email-address/
   * @see http://www.rfc-editor.org/rfc/rfc5322.txt
   */
  public static boolean isValidEmailAddress(String email) {
    String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]"
        + "+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    return Pattern.compile(regex).matcher(email).matches();
  }

  /**
   * Checks whether the given String represents a numeric value.
   * @param str the string to be checked.
   * @return <code>true</code> if the given String is a numeric value;
             <code>false</code> otherwise.
   */
  private static boolean isNumeric(String str) {
    try {
      Double.parseDouble(str);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }
}

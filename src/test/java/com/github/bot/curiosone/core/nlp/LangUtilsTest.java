package com.github.bot.curiosone.core.nlp;

// SUPPRESS CHECKSTYLE AvoidStarImport
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class LangUtilsTest {
  @Test
  public void testSplitByPuntaction() {
    List<String> l;

    l = LangUtils.splitByPuntaction("test string");
    assertEquals(1, l.size());
    assertEquals("test string.", l.get(0));

    l = LangUtils.splitByPuntaction("Unicorns are awesome! Isn't it?");
    assertEquals(2, l.size());
    assertEquals("Unicorns are awesome!", l.get(0));
    assertEquals(" Isn't it?", l.get(1));

    l = LangUtils.splitByPuntaction("!!!!!!asd!!!!! dasda.!!!!???!");
    assertEquals(2, l.size());
    assertEquals("asd!", l.get(0));
    assertEquals(" dasda.", l.get(1));

    l = LangUtils.splitByPuntaction("!!!!!!");
    assertEquals(0, l.size());

    l = LangUtils.splitByPuntaction("!!! !!!");
    assertEquals(1, l.size());
    assertEquals(" !", l.get(0));

    l = LangUtils.splitByPuntaction("Hello! Visit my website: http://evil.com/pwn.you pls");
    assertEquals(2, l.size());
    assertEquals("Hello!", l.get(0));
    assertEquals(" Visit my website: http://evil.com/pwn.you pls.", l.get(1));

    l = LangUtils.splitByPuntaction("Hello!! Contact me at spam@bot.com to get more spam! ;)");
    assertEquals(3, l.size());
    assertEquals("Hello!", l.get(0));
    assertEquals(" Contact me at spam@bot.com to get more spam!", l.get(1));
    assertEquals(" ;).", l.get(2));

    l = LangUtils.splitByPuntaction("");
    assertEquals(0, l.size());

    l = LangUtils.splitByPuntaction("$plit me");
    assertEquals(1, l.size());
    assertEquals("$plit me.", l.get(0));

    l = LangUtils.splitByPuntaction("42 is a number");
    assertEquals(1, l.size());
    assertEquals("42 is a number.", l.get(0));

    l = LangUtils.splitByPuntaction("42.0 is a double");
    assertEquals(1, l.size());
    assertEquals("42.0 is a double.", l.get(0));

    l = LangUtils.splitByPuntaction("This ://cel@.com is an invalid uri");
    assertEquals(1, l.size());
    assertEquals("This ://cel@.com is an invalid uri.", l.get(0));
  }

  @Test
  public void testRemoveDuplicatedSpaces() {
    String s;

    s = LangUtils.removeDuplicatedSpaces("Hello   world!    !");
    assertEquals("Hello world! !", s);

    s = LangUtils.removeDuplicatedSpaces("           !  ");
    assertEquals("!", s);

    s = LangUtils.removeDuplicatedSpaces("   he llo ");
    assertEquals("he llo", s);

    s = LangUtils.removeDuplicatedSpaces("");
    assertEquals("", s);

    s = LangUtils.removeDuplicatedSpaces("Hello");
    assertEquals("Hello", s);
  }

  @Test
  public void testExpandVerbs() {
    String s;

    s = LangUtils.expandVerbs("I'm in!");
    assertEquals("I am in!", s);

    s = LangUtils.expandVerbs("i'm done");
    assertEquals("i am done", s);

    s = LangUtils.expandVerbs("I'M DONE");
    assertEquals("I AM DONE", s);

    s = LangUtils.expandVerbs("i've done it");
    assertEquals("i have done it", s);

    s = LangUtils.expandVerbs("I'VE DONE IT");
    assertEquals("I HAVE DONE IT", s);

    s = LangUtils.expandVerbs("I'll kill you");
    assertEquals("I will kill you", s);

    s = LangUtils.expandVerbs("You're high!");
    assertEquals("You are high!", s);

    s = LangUtils.expandVerbs("That's it!");
    assertEquals("That is it!", s);

    s = LangUtils.expandVerbs("I won't kill you!");
    assertEquals("I will not kill you!", s);

    s = LangUtils.expandVerbs("I'm gonna put some other verbs in here! You're ok with it? I'll check if you'll!");
    assertEquals("I am gonna put some other verbs in here! You are ok with it? I will check if you will!", s);

    s = LangUtils.expandVerbs("That's it!");
    assertEquals("That is it!", s);

    s = LangUtils.expandVerbs("Mike'll leave at 10 PM.");
    assertEquals("Mike will leave at 10 PM.", s);
  }
}

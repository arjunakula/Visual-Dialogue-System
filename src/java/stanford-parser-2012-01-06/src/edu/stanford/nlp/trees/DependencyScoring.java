package edu.stanford.nlp.trees;

import java.io.LineNumberReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counters;

/**
 * Scoring of typed dependencies
 *
 * @author danielcer
 *
 */
public class DependencyScoring {
  public final List<Set<TypedDependency>> goldDeps;
  public final List<Set<TypedDependency>> goldDepsUnlabeled;

  private static List<Set<TypedDependency>> toSets(Collection<TypedDependency> depCollection) {
    Set<TypedDependency> depSet = new HashSet<TypedDependency>(depCollection);
    Set<TypedDependency> unlabeledDepSet = new HashSet<TypedDependency>();
    for (TypedDependency dep : depCollection) {
      unlabeledDepSet.add(new TypedDependencyStringEquality(null, dep.gov(), dep.dep()));
    }

    List<Set<TypedDependency>> l = new ArrayList<Set<TypedDependency>>(2);
    l.add(depSet);
    l.add(unlabeledDepSet);
    return l;
  }

  public DependencyScoring(List<Collection<TypedDependency>> goldDeps) {
    this.goldDeps = new ArrayList<Set<TypedDependency>>(goldDeps.size());
    this.goldDepsUnlabeled = new ArrayList<Set<TypedDependency>>(goldDeps.size());

    for (Collection<TypedDependency> depCollection : goldDeps) {
      List<Set<TypedDependency>> sets = toSets(depCollection);
      this.goldDepsUnlabeled.add(sets.get(1));
      this.goldDeps.add(sets.get(0));
    }
  }
  
  public static DependencyScoring newInstanceStringEquality(List<Collection<TypedDependency>> goldDeps) {
    return new DependencyScoring(convertStringEquality(goldDeps));
  }

  public DependencyScoring(String filename) throws IOException {
    this(readDeps(filename));
  }
  
  public static List<Collection<TypedDependency>> convertStringEquality(List<Collection<TypedDependency>> deps){
    List<Collection<TypedDependency>> convertedDeps = new ArrayList<Collection<TypedDependency>>();
    for(Collection<TypedDependency> depSet : deps){
      Collection<TypedDependency> converted = new HashSet<TypedDependency>();
      for(TypedDependency dep : depSet){
        converted.add(new TypedDependencyStringEquality(dep.reln(), dep.gov(), dep.dep()));
      }
      convertedDeps.add(converted);
    }
    return convertedDeps;
  }

  static private class TypedDependencyStringEquality extends TypedDependency {
    public TypedDependencyStringEquality(GrammaticalRelation reln, TreeGraphNode gov, TreeGraphNode dep)  {
       super(reln, gov, dep);
    }

    public boolean equals(Object o) {
       // some parsers, like Relex, screw up the casing
       return o.toString().toLowerCase().equals(this.toString().toLowerCase());
    }

    @Override
    public int hashCode() {
       return toString().toLowerCase().hashCode();
    }
  }

  /** Normalize all number tokens to &lt;num&gt; in order to allow
   *  for proper scoring of MSTParser productions.
   */
  static protected String normalizeNumbers(String token) {
    String norm = token.replaceFirst("^([0-9]+)-([0-9]+)$", "<num>-$2");
   
    if (!norm.equals(token)) {
      System.err.printf("%s<-%s\n", norm, token);
    }
    return token; 
  }

  /**
   * Read in typed dependencies. Warning created typed dependencies are not
   * backed by any sort of a tree structure.
   *
   * @param filename
   * @throws IOException
   */
  static protected List<Collection<TypedDependency>> readDeps(String filename) throws IOException {

    LineNumberReader breader = new LineNumberReader(new FileReader(filename));
    List<Collection<TypedDependency>> readDeps = new ArrayList<Collection<TypedDependency>>();
    Collection<TypedDependency> deps = new ArrayList<TypedDependency>();
    for (String line = breader.readLine(); line != null; line = breader.readLine()) {
      if (line.equals("null(-0,-0)") || line.equals("null(-1,-1)")) {
         readDeps.add(deps);
         deps = new ArrayList<TypedDependency>();
         continue; // relex parse error
      }
      try {
      if (line.equals("")) {
         if (deps.size() != 0) {
          //System.out.println(deps);
          readDeps.add(deps);
          deps = new ArrayList<TypedDependency>();
        }
        continue;
      }
      int firstParen = line.indexOf("(");
      int commaSpace = line.indexOf(", ");
      String depName = line.substring(0, firstParen);
      String govName = line.substring(firstParen + 1, commaSpace);
      String childName = line.substring(commaSpace+2, line.length() - 1);
      GrammaticalRelation grel = GrammaticalRelation.valueOf(depName);
      if (depName.startsWith("prep_")) {
        String prep = depName.substring(5);
        grel = EnglishGrammaticalRelations.getPrep(prep);
      }
      if (depName.startsWith("prepc_")) {
        String prepc = depName.substring(6);
        grel = EnglishGrammaticalRelations.getPrepC(prepc);
      }
      if (depName.startsWith("conj_")) {
        String conj = depName.substring(5);
        grel = EnglishGrammaticalRelations.getConj(conj);
      }
      if (grel == null) {
        throw new RuntimeException("Unknown grammatical relation '" + depName+"'");
      }
      int govDash = govName.lastIndexOf("-");
      int childDash = childName.lastIndexOf("-");

      //Word govWord = new Word(govName.substring(0, govDash));
      Word govWord = new Word(normalizeNumbers(govName));
      //Word childWord = new Word(childName.substring(0, childDash));
      Word childWord = new Word(normalizeNumbers(childName));
      TypedDependency dep = new TypedDependencyStringEquality(grel, new TreeGraphNode(govWord), new TreeGraphNode(childWord));
      deps.add(dep);
      } catch (Exception e) {
        throw new RuntimeException("Error on line "+breader.getLineNumber()+":\n\n"+e);
      }
    }
    if (deps.size() != 0) {
       readDeps.add(deps);
    }

    //System.err.println("last: "+readDeps.get(readDeps.size()-1));
    return readDeps;
  }

  /**
   * Score system typed dependencies
   *
   * @param system
   * @return a triple consisting of (labeled attachment, unlabeled attachment,
   *         label accuracy)
   */
  public Score score(List<Collection<TypedDependency>> system) {
    int parserCnt = 0;
    int goldCnt = 0;
    int correctAttachment = 0;
    int correctUnlabeledAttachment = 0;
    int labelCnt = 0;
    int labelCorrect = 0;

    ClassicCounter<String> unlabeledErrorCounts = new ClassicCounter<String>();
    ClassicCounter<String> labeledErrorCounts = new ClassicCounter<String>();
    System.out.println("Gold size: "+ goldDeps.size() + " System size: "+system.size());
    for (int i = 0; i < system.size(); i++) {
      List<Set<TypedDependency>> l = toSets(system.get(i));
      int localCnt = l.get(0).size();
      parserCnt += l.get(0).size();
      goldCnt += goldDeps.get(i).size();
      //System.out.println("Gold: "+goldDeps.get(i)+"\n");
      //System.out.println("Sys: "+l.get(0)+"\n");
      l.get(0).retainAll(goldDeps.get(i));
      //System.out.println("UGold: "+goldDepsUnlabeled.get(i)+"\n");
      //System.out.println("USys: "+l.get(1)+"\n");
      l.get(1).retainAll(goldDepsUnlabeled.get(i));
      correctAttachment += l.get(0).size();
      correctUnlabeledAttachment += l.get(1).size();
      labelCnt += l.get(1).size();
      labelCorrect += l.get(0).size();
      //System.out.println(""+i+" Acc: "+(l.get(0).size())/(double)localCnt+" "+l.get(0).size()+"/"+localCnt);
      

      // identify errors
      List<Set<TypedDependency>> errl = toSets(system.get(i));
      errl.get(0).removeAll(goldDeps.get(i));
      errl.get(1).removeAll(goldDepsUnlabeled.get(i));
      Map<String,String> childCorrectWithLabel = new HashMap<String,String>();
      Map<String,String> childCorrectWithOutLabel = new HashMap<String,String>();

      for (TypedDependency goldDep: goldDeps.get(i)) {
          //System.out.print(goldDep);
          String sChild = goldDep.dep().label().toString().replaceFirst("-[^-]*$", "");
          String prefixLabeled = "";
          String prefixUnlabeled = "";
          if (childCorrectWithLabel.containsKey(sChild)) {
            prefixLabeled = childCorrectWithLabel.get(sChild)+", ";
            prefixUnlabeled = childCorrectWithOutLabel.get(sChild)+", ";
          }
          childCorrectWithLabel.put(sChild, prefixLabeled + goldDep.reln()+"("+goldDep.gov().label().toString().replaceFirst("-[^-]*$", "")+", "+sChild+")");
          childCorrectWithOutLabel.put(sChild, prefixUnlabeled + "dep("+goldDep.gov().label().toString().replaceFirst("-[^-]*$", "")+", "+sChild+")");
      }

      for (TypedDependency labeledError: errl.get(0)) {
          String sChild = labeledError.dep().label().toString().replaceFirst("-[^-]*$", "");
          String sGov   = labeledError.gov().label().toString().replaceFirst("-[^-]*$", "");
          labeledErrorCounts.incrementCount(labeledError.reln().toString()+"("+sGov+", "+sChild+") <= "+childCorrectWithLabel.get(sChild));
      }
      for (TypedDependency unlabeledError: errl.get(1)) {
          String sChild = unlabeledError.dep().label().toString().replaceFirst("-[^-]*$", "");
          String sGov   = unlabeledError.gov().label().toString().replaceFirst("-[^-]*$", "");
          unlabeledErrorCounts.incrementCount("dep("+sGov+", "+sChild+") <= "+childCorrectWithOutLabel.get(sChild));
      }
    }
    return new Score(parserCnt, goldCnt, correctAttachment, correctUnlabeledAttachment, labelCnt, labelCorrect, labeledErrorCounts, unlabeledErrorCounts);
  }

  public class Score {
    final int parserCnt;
    final int goldCnt;
    final int correctAttachment;
    final int correctUnlabeledAttachment;
    final int labelCnt;
    final int labelCorrect;
    final ClassicCounter<String> unlabeledErrorCounts;
    final ClassicCounter<String> labeledErrorCounts;

    public Score(int parserCnt, int goldCnt, int correctAttachment, int correctUnlabeledAttachment, int labelCnt, int labelCorrect, ClassicCounter<String> labeledErrorCounts, ClassicCounter<String> unlabeledErrorCounts) {
      this.parserCnt = parserCnt;
      this.goldCnt = goldCnt;
      this.correctAttachment = correctAttachment;
      this.correctUnlabeledAttachment = correctUnlabeledAttachment;
      this.labelCnt = labelCnt;
      this.labelCorrect = labelCorrect;
      this.unlabeledErrorCounts = new ClassicCounter<String>(unlabeledErrorCounts);
      this.labeledErrorCounts = new ClassicCounter<String>(labeledErrorCounts);
    }

    public String toString() {
      return toString(false);
    }

    public String toString(boolean verbose) {
      StringBuilder sbuild = new StringBuilder();
      sbuild.append(String.format("Labeled Attachment || F ||  P||  R ||\n"));
      double lp = correctAttachment/(double)parserCnt; 
      double lr = correctAttachment/(double)goldCnt;
      double lf = 2.0*(lp*lr)/(lp+lr);
      sbuild.append(String.format("|| %.3f || %.3f (%d/%d) || %.3f (%d/%d)||\n",
        lf, lp, correctAttachment, parserCnt, lr, correctAttachment, goldCnt)); 
      /*sbuild.append(String.format("Labeled Attachment P: %.3f (%d/%d)\n", correctAttachment/(double)parserCnt, correctAttachment, parserCnt));
      sbuild.append(String.format("Labeled Attachment R: %.3f (%d/%d)\n", correctAttachment/(double)goldCnt, correctAttachment, goldCnt));
      */
      sbuild.append(String.format("Unlabeled Attachment || F ||  P||  R ||\n"));
      double ulp = correctUnlabeledAttachment/(double)parserCnt; 
      double ulr = correctUnlabeledAttachment/(double)goldCnt;
      double ulf = 2.0*(ulp*ulr)/(ulp+ulr);
      sbuild.append(String.format("|| %.3f || %.3f (%d/%d) || %.3f (%d/%d)||\n",
        ulf, ulp, correctUnlabeledAttachment, parserCnt, ulr, correctUnlabeledAttachment, goldCnt)); 
      /*
      sbuild.append(String.format("Unlabeled Attachment P: %.3f (%d/%d)\n", correctUnlabeledAttachment/(double)parserCnt, correctUnlabeledAttachment, parserCnt));
      sbuild.append(String.format("Unlabeled Attachment R: %.3f (%d/%d)\n", correctUnlabeledAttachment/(double)goldCnt, correctUnlabeledAttachment, goldCnt));
      sbuild.append(String.format("LabelAccuracy: %.3f (%d/%d)\n", labelCorrect/(double)labelCnt, labelCorrect, labelCnt));
      */
      if (verbose) {
          sbuild.append("\nLabeled Attachment Error Counts\n");
          sbuild.append(Counters.toSortedString(labeledErrorCounts, Integer.MAX_VALUE, "\t%2$f\t%1$s", "\n"));
          sbuild.append("\n");
          sbuild.append("\nUnlabeled Attachment Error Counts\n");
          sbuild.append(Counters.toSortedString(unlabeledErrorCounts, Integer.MAX_VALUE, "\t%2$f\t%1$s", "\n"));
      }
      return sbuild.toString();
    }
  }

  public static void main(String[] args) throws IOException {
    Properties props = StringUtils.argsToProperties(args);
    boolean verbose = Boolean.parseBoolean(props.getProperty("v", "False"));
    String goldFilename = props.getProperty("g");
    String systemFilename = props.getProperty("s");
    if (goldFilename == null || systemFilename == null) {
      System.err.println("Usage:\n\tjava ...DependencyScoring [-v True/False] -g goldFile -s systemFile\n");
      System.err.println("\nOptions:\n\t-v verbose output");
      System.exit(-1);
    }

    DependencyScoring goldScorer = new DependencyScoring(goldFilename);
    List<Collection<TypedDependency>> systemDeps = DependencyScoring.readDeps(systemFilename);
    Score score = goldScorer.score(systemDeps);
    System.out.println(score.toString(verbose));
  }
}
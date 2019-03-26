package edu.stanford.nlp.international.french.pipeline;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.process.treebank.AbstractDataset;
import edu.stanford.nlp.process.treebank.DefaultMapper;
import edu.stanford.nlp.process.treebank.StringMap;
import edu.stanford.nlp.stats.TwoDimensionalCounter;
import edu.stanford.nlp.trees.MemoryTreebank;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.international.arabic.ATBTreeUtils;
import edu.stanford.nlp.trees.international.french.FrenchTreeReaderFactory;
import edu.stanford.nlp.trees.international.french.FrenchTreebankLanguagePack;
import edu.stanford.nlp.trees.tregex.TregexParseException;
import edu.stanford.nlp.trees.tregex.TregexPattern;

/**
 * Produces the pre-processed version of the FTB used in the experiments of
 * Green et al. (2011).
 * 
 * @author Spence Green
 *
 */
public class FTBDataset extends AbstractDataset {

  public FTBDataset() {
    super();

    //Need to use a MemoryTreebank so that we can compute gross corpus
    //stats for MWE pre-processing
    treebank = new MemoryTreebank(new FrenchTreeReaderFactory(), FrenchTreebankLanguagePack.FTB_ENCODING);
    treeFileExtension = "xml";
  }


  @Override
  public void build() {
    for(File path : pathsToData) {
      int prevSize = treebank.size();
      if(splitFilter == null)
        treebank.loadPath(path,treeFileExtension,false);
      else
        treebank.loadPath(path,splitFilter);

      toStringBuffer.append(String.format(" Loaded %d trees from %s\n", treebank.size() - prevSize, path.getPath()));
    }

    
    PrintWriter outfile = null;
    PrintWriter flatFile = null;
    try {
      outfile = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName),"UTF-8")));
      flatFile = (makeFlatFile) ? new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(flatFileName),"UTF-8"))) : null;

      outputFileList.add(outFileName);

      if(makeFlatFile) {
        outputFileList.add(flatFileName);
        toStringBuffer.append(" Made flat files\n");
      }

      preprocessMWEs();

      List<TregexPattern> badTrees = new ArrayList<TregexPattern>();
      //These trees appear in the Candito training set
      //They are mangled by the TreeCorrector, so discard them ahead of time.
      badTrees.add(TregexPattern.compile("@SENT <: @PUNC"));
      badTrees.add(TregexPattern.compile("@SENT <1 @PUNC <2 @PUNC !<3 __"));
      
      //wsg2011: This filters out tree #552 in the Candito test set. We saved this tree for the
      //EMNLP2011 paper, but since it consists entirely of punctuation, it won't be evaluated anyway.
      //Since we aren't doing the split in this data set, just remove the tree.
      badTrees.add(TregexPattern.compile("@SENT <1 @PUNC <2 @PUNC <3 @PUNC <4 @PUNC !<5 __"));
      
      for(Tree t : treebank) {
        
        //Filter out bad trees
        boolean skipTree = false;
        for(TregexPattern p : badTrees) {
          skipTree = p.matcher(t).find();
          if(skipTree) break;
        }
        if(skipTree) {
          System.err.println("Discarding tree: " + t.toString());
          continue;
        }
        
        if(customTreeVisitor != null)
          customTreeVisitor.visitTree(t);

        outfile.println(t.toString());
        
        if(makeFlatFile) {
          String flatString = (removeEscapeTokens) ? 
              ATBTreeUtils.unEscape(ATBTreeUtils.flattenTree(t)) : ATBTreeUtils.flattenTree(t);
              flatFile.println(flatString);
        }
      }

    } catch (UnsupportedEncodingException e) {
      System.err.printf("%s: Filesystem does not support UTF-8 output%n", this.getClass().getName());
      e.printStackTrace();

    } catch (FileNotFoundException e) {
      System.err.printf("%s: Could not open %s for writing%n", this.getClass().getName(), outFileName);

    } catch (TregexParseException e) {
      System.err.printf("%s: Could not compile Tregex expressions%n", this.getClass().getName());
      e.printStackTrace();
    
    } finally {
      if(outfile != null)
        outfile.close();
      if(flatFile != null)
        flatFile.close();
    }
  }

  /**
   * Corrects MWE annotations that lack internal POS labels.
   */
  private void preprocessMWEs() {

    TwoDimensionalCounter<String,String> labelTerm = 
      new TwoDimensionalCounter<String,String>();
    TwoDimensionalCounter<String,String> termLabel = 
      new TwoDimensionalCounter<String,String>();
    TwoDimensionalCounter<String,String> labelPreterm = 
      new TwoDimensionalCounter<String,String>();
    TwoDimensionalCounter<String,String> pretermLabel = 
      new TwoDimensionalCounter<String,String>();

    TwoDimensionalCounter<String,String> unigramTagger = 
      new TwoDimensionalCounter<String,String>();

    for (Tree t : treebank) {
      MWEPreprocessor.countMWEStatistics(t, unigramTagger,
          labelPreterm, pretermLabel, 
          labelTerm, termLabel);
    }

    for (Tree t : treebank) {
      MWEPreprocessor.traverseAndFix(t, pretermLabel, unigramTagger);
    }
  }


  @Override
  public boolean setOptions(StringMap opts) {
    boolean ret = super.setOptions(opts);

    if(lexMapper == null) {
      lexMapper = new DefaultMapper();
      lexMapper.setup(null, lexMapOptions.split(","));
    }

    if(pathsToMappings.size() != 0) {
      if(posMapper == null)
        posMapper = new DefaultMapper();
      for(File path : pathsToMappings)
        posMapper.setup(path);
    }

    return ret;
  }

}
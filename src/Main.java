import java.io.*;
import java.util.*;
import java.lang.Runtime;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.*;
import C.*;
import C.Absyn.*;
import typechecker.TypeChecker;
import typechecker.TypeException;
import compiler.Compiler;

class ParseError extends RuntimeException
{
  int line;
  int column;
  public ParseError(String msg, int l, int c)
  {
    super(msg);
    line = l;
    column = c;
  }
}

class BNFCErrorListener implements ANTLRErrorListener
{
  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e)
  {
    throw new ParseError(s,i,i1);
  }
  @Override
  public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet)
  {
    throw new ParseError("Ambiguity at",i,i1);
  }
  @Override
  public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet)
  {
  }
  @Override
  public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet)
  {
  }
}

public class Main {
  public static void main(String args[]) {

    // Ensure that we are called with a single argument.
    if (args.length != 1) {
      System.err.println("Usage: flispcc <SourceFile>");
      System.exit(1);
    }

    final String srcFile   = args[0];                // Ex: path/to/file.cc
    final String fileCore  = stripSuffix(srcFile);   // Ex: path/to/file
    final String dir       = stripFileName(srcFile); // Ex: path/to  or "."
    final String className = stripPath(fileCore);    // Ex:         file
    final String out       = fileCore + ".flisp";    // Ex: path/to/file.flisp
    final String assembler = "qaflisp";              // will look for assembler in $PATH 

    CLexer  l = null;
    try {

      // Parse
      Reader input;
      if (args.length == 0)input = new InputStreamReader(System.in);
      else input = new FileReader(args[0]);
      l = new CLexer(new ANTLRInputStream(input));
              l.addErrorListener(new BNFCErrorListener());
    } catch(IOException e) {
      System.err.println("Error: File not found: " + args[0]);
      System.exit(1);
    }

    CParser p = new CParser(new CommonTokenStream(l));
            p.addErrorListener(new BNFCErrorListener());
    
    // Get ast from parser        
    Program ast = p.program().result;

    // Type check
    Program typedTree = new TypeChecker().typecheck(ast);

    // Compile into assembler code
    String jtext = new Compiler().compile(className, typedTree);
    
    // Write .j file to same directory where source file was.
    try {
      PrintWriter writer = new PrintWriter(out);
      writer.print(jtext);
      writer.close();
    } catch (FileNotFoundException e) {
      System.err.println(e);
    }

    // Don't know if neccessary
    System.out.println(String.format("Compilation successful. Output file: %s", out));

    // Call the assembler
    Runtime rt = Runtime.getRuntime();
    Process ps = null;
    try {
      try {
        ps = rt.exec(String.format("%s %s", assembler, out));
      } catch(IOException e) {
        System.err.println("Error: Could not find qaflisp (assembler) in PATH. Exiting..");
        System.exit(1);
      }
      
      // Print messages from assembler process
      InputStream in = ps.getInputStream();
      int x = -1;
      while((x = in.read()) != -1) {
        System.out.print((char) x);
      } 
    }
    catch (TypeException e) {
      System.out.println("TYPE ERROR");
      System.err.println(e.toString());
      System.exit(1);
    }
    catch (RuntimeException e) {
      System.err.println(String.format("Compiler error: %s", e.getMessage()));
      System.err.println(e.toString());
      System.exit(1);
    }
    catch (IOException e) {
      System.err.println(e.toString());
      System.exit(1);
    }
    catch (Throwable e) {
      System.out.println("SYNTAX ERROR");
      //System.out.println("At line " + String.valueOf(l.line_num())
        //         + ", near \"" + l.buff() + "\" :");
      System.out.println("     " + e.getMessage());
      //e.printStackTrace();
      System.exit(1);
    }
  }

  // From Test.java
  /*public C.Absyn.Program parse() throws Exception {
    /* The default parser is the first-defined entry point. */
    /* Other options are: */
    /* listDef, def, listArg, listStm, arg, stm, listId, exp, listExp,
       type */
   /* CParser.ProgramContext pc = p.program();
    org.antlr.v4.runtime.Token _tkn = p.getInputStream().getTokenSource().nextToken();
    if(_tkn.getType() != -1) throw new ParseError("Stream does not end with EOF",_tkn.getLine(),_tkn.getCharPositionInLine());
    C.Absyn.Program ast = pc.result;
    System.out.println();
    System.out.println("Parse Succesful!");
    System.out.println();
    System.out.println("[Abstract Syntax]");
    System.out.println();
    System.out.println(PrettyPrinter.show(ast));
    System.out.println();
    System.out.println("[Linearized Tree]");
    System.out.println();
    System.out.println(PrettyPrinter.print(ast));
    return ast;
  }*/



  // Utilities
  ///////////////////////////////////////////////////////////////////////////

  // Remove path from a file name.
  private static String stripPath(String name) {
    return new File(name).getName();
  }

  // Retain just the path from a file name.
  // Returns "." if there was no path.
  private static String stripFileName(String name) {
    String dir = new File(name).getParent();
    if (dir == null) dir = ".";
    return dir;
  }

  // Remove extension from a file name (keep the path).
  private static String stripSuffix(String filename) {
    int divider = filename.lastIndexOf('.');
    if (divider <= 0) return filename;
    else return filename.substring(0, divider);
  }
}

package typechecker;

import typechecker.FuncType;
import C.Absyn.*;
import C.*;
import java.util.*;

/**
 * Typechecker
 * Author: Karl Strålman, Tobias Campbell
 * */
public class TypeChecker implements
  Program.Visitor<Program, Void>,
  Def.Visitor<Def, Void>,
  Arg.Visitor<Arg, Void>,
  Stm.Visitor<Stm, FuncType>,
  Exp.Visitor<Type, Void>
{
  // Built-in types
  final Type INT    = new TInt();
  //final Type DOUBLE = new TDouble();
  final Type BOOL   = new TBool();
  final Type VOID   = new TVoid();

  TreeMap<String, FuncType> signatures;          // function signatures  TreeMap..
  LinkedList<TreeMap<String, Type>> context;     // blocks LinkedList<TreeMap..
  TreeMap<String, Type> globalvars;              // global variables

  /* Entry point for typechecker */
  public Program typecheck(Program p) {
    context = new LinkedList<>();
    signatures = new TreeMap<String, FuncType>();
    globalvars = new TreeMap<String, Type>();
    p.accept(this, null);
    return p;
  }

  /*=============== Auxilaries ================ */
  /* Returns type of variable from context
   * Since context works as a stack, we only
   * need to get the first one.
   * */
  public Type lookupVar(String id) {
    Type gt = globalvars.get(id);
    if(gt != null)
      return gt;

    for (Map<String, Type> m : context) {
      Type ret = m.get(id);
      if (ret != null)
        return ret;
    }

    throw new TypeException("variable is undefined");
  }

  /* Add/update variable in context */
  public void addVar(String id, Type t) {
    if(t == null)
      throw new TypeException("undefined type");

    // check global context first
    if(globalvars.containsKey(id))
      throw new TypeException(String.format("Variable already defined: %s.", id));

    Map<String, Type> ctx = context.getFirst();
    if(ctx.containsKey(id))
       throw new TypeException(String.format("Variable already defined: %s.", id));
    ctx.put(id, t);
  }

  public void addGlobal(String id, Type t) {
    if(t == null)
      throw new TypeException("undefined type");

     if(globalvars.containsKey(id))
       throw new TypeException(String.format("Global variable already defined: %s", id));
     globalvars.put(id, t);
  }

  /* Returns function types  */
  public FuncType lookupFunc(String id) {
    return signatures.get(id);
  }

  /* Add sig to map of sigs.
   * Returns false if already exist.
   * */
  public void addSignature(String id, FuncType f) {
    if(signatures.containsKey(id))
      throw new TypeException(String.format("Duplicate function found: %s.", id));
    signatures.put(id, f);
  }

  /* Method that creates a list, adds one arg,
   * then returns it.
   **/
  public ListArg oneArgFunc(String id, Type t) {
    ListArg la = new ListArg();
    la.add(new ADecl(t, id));
    return la;
  }


  /* ============= Exp Aux ==================*/
  /**
   * Checks if t1 is either a TInt or TDouble,
   * otherwise throw TypeException.
   */
  public Type checkNumber(Type t1) {
    // t1 instanceof TInt || t1 instanceof TDouble, if Double will be implemented
    if (t1 instanceof TInt)
      return t1;
    throw new TypeException(
        String.format(
          "Expected exp of type TInt or TDouble but found exp of type %s", t1));
  }

  /**
   * Checks if t2 is subtype t1, otherwise
   * throw TypeException
   **/
  public boolean subType(Type t1, Type t2) {
    // Double can be cast as int
    //if(t1 instanceof TInt && t2 instanceof TDouble)
      //return true;

    if (t1.equals(t2))
      return false;

    // Else throw exception
    throw new TypeException(
        String.format(
        "AExpected exp of type %s but found exp of type %s", t1, t2));
  }

  /* Visits exp to get type and passes it to subType() func */
  public void checkExp(Exp e, Type t) {
    Type et = e.accept(this, null);
    e.isSubType = subType(t, et);
    e.setType(et);
  }
  /* Check that Exp e is of type TBool. */
  public void checkBool(Exp e) {
    Type et = e.accept(this, null);
    e.setType(et);
    if (et instanceof TBool)
      return;
      throw new TypeException(
          String.format(
          "Expected exp of type TBool but found exp of type %s", et));

  }

  public Type checkComparison(Exp e1, Exp e2) {
    e1.setType(checkNumber(e1.accept(this, null)));
    e2.setType(checkNumber(e2.accept(this, null)));


    // Subtyping if ever needed.
    // e1 == e2
    // --------
    // 1.0 == 1
    // 1 == 1.0
    //e1.isSubType = e1.getType().equals(INT) && e2.getType().equals(DOUBLE);
    //e2.isSubType = e1.getType().equals(DOUBLE) && e2.getType().equals(INT);
      
    return BOOL;
  }

  /* Check that expressions are double or
   * int for arithmetic operations
   **/
  public Type arithExp(Exp e1, Exp e2) {
    Type t1 = e1.accept(this, null);
    Type t2 = e2.accept(this, null);
    e1.setType(t1);
    e2.setType(t2);
    if(t1 instanceof TBool || t2 instanceof TBool)
      throw new TypeException("Expected integer of boolean in arithmetic exp.");

    checkNumber(t1);
    checkNumber(t2);
    //e1.isSubType = e1.getType().equals(INT) && e2.getType().equals(DOUBLE);
    //e2.isSubType = e1.getType().equals(DOUBLE) && e2.getType().equals(INT);

    //if(t1.equals(DOUBLE) || t2.equals(DOUBLE))
      //return DOUBLE;
    return t1;
  }

  /*
   * Check that expressions are bool
   * for logical exp
   **/
  public Type logicExp(Exp e1, Exp e2) {
    Type t1 = e1.accept(this, null);
    Type t2 = e2.accept(this, null);
    e1.setType(t1);
    e2.setType(t2);

    if(t1 instanceof TBool && t2 instanceof TBool)
      return BOOL;

    // Else numbers
    checkNumber(t1);
    checkNumber(t2);

    //e1.isSubType = t1.equals(INT) && t2.equals(DOUBLE);
    //e2.isSubType = t1.equals(DOUBLE) && t2.equals(INT);
 
    return BOOL;
  }

  public void checkArgs(ListExp le, ADecl a, int i) {
    Exp e = le.get(i);
    checkExp(e, a.type_);
  }

  /**
   * When entering a scope, add a new context
   * free of signatures at the start of the stack.
   */
  public void enterScope() {
    context.addFirst(new TreeMap());
  }
  /**
   * When leaving a scope, remove the first
   * (newest) context in the stack.
   */
  public void leaveScope() {
    context.removeFirst();
  }

  /*================ Program ===================*/

  /*
   * Program is a sequence of functions definitions.
   * There are also some built-in primitive functions.
   * Defined here:
   * http://www.cse.chalmers.se/edu/course/DAT151/laborations/lab2/index.html
   * */
  public Prg visit(Prg  p, Void arg) {

    // Add built-in primitive functions to sig table
    addSignature("writeInt",    new FuncType(VOID, oneArgFunc("x", INT)));
    addSignature("readInt",     new FuncType(INT, new ListArg()));

    // Set global context
    context.addFirst(new TreeMap());

    for (Def d : p.listdef_) {
      if(d instanceof DFunc)
        addSignatures((DFunc)d);
    }

    // go through functions defs
    for(Def d : p.listdef_) {
      d.accept(this, null);
    }

    /* Specific cases for main function */
    FuncType main = lookupFunc("main");

    // Requirement: Main must exist
    if(main == null)
      throw new TypeException("Could not find main");

    // Requirement: Main must have type Int
    if(!main.rt.equals(INT))
      throw new TypeException("Main must be of type 'int'");

    // Requirement: Main must have no arguments
    if(!main.args.isEmpty())
      throw new TypeException("Main can not contain any arguments");

    return p;
  }


   /*================ Function defs. ============*/
  public Def visit(DFunc p, Void arg) {
    FuncType currFunc = new FuncType(p.type_, p.listarg_);

    // Add init context.
    context = new LinkedList();
    context.add(new TreeMap());

    // add function parameters to context
    for(Arg a : p.listarg_) {
      a.accept(this, arg);
    }

    // add statements to context
    // pass func for SReturn case
    for(Stm s : p.liststm_) {
      s.accept(this, currFunc);
    }

    return null;
  }

  public void addSignatures(DFunc p) {
    FuncType currFunc = new FuncType(p.type_, p.listarg_);
    addSignature(p.id_, currFunc);
  }

  /* Global variables handling, only SDecls and SInit allowed */
  public Def visit(DGlob p, Void arg) {
    if(p.stm_ instanceof SDecls) {
      SDecls stm = (SDecls) p.stm_;
      for(String s : stm.listid_)
        addGlobal(s, stm.type_);
      return null;
    }

    if(p.stm_ instanceof SInit) {
      SInit stm = (SInit) p.stm_;
      addGlobal(stm.id_, stm.type_);
      stm.exp_.accept(this, null); // initialize
      return null;
    }

    throw new TypeException("Statement not allowed in global scope");
  }

  /*================== Function Args =================== */
  public Arg visit(ADecl p, Void arg) {
    if(p.type_.equals(VOID))
      throw new TypeException("Variable cannot be void");
    // Add function arguments to context
    addVar(p.id_, p.type_);
    return null;
  }

  /*==================== Statements ====================*/
  /**
   * Variable declarations have one of the following formats:
   *  1. type and one or more var
   *  or ...
   */
  public Stm visit(SDecls p, FuncType arg) {
    if(p.type_.equals(VOID))
      throw new TypeException("Variable cannot be void");

    // Add variables to context
    for(String id: p.listid_) {
      addVar(id, p.type_);
    }
    return null;
  }

  /**
   * ...
   * 2. Type and a single initialized variable
   * Initializing expression must have the declared type
   * in the extended context, and the type cannot be void.
   */
  public Stm visit(SInit p, FuncType arg) {

    // Add to context
    addVar(p.id_, p.type_);
    p.exp_.isSubType = subType(p.exp_.accept(this, null), p.type_);
    return null;
  }

    // Leave block
    //leaveScope();
  /* Any expression followed by semicolon can
   * be used as a statement.
   **/
  public Stm visit(SExp p, FuncType arg) {
    Type t = p.exp_.accept(this, null);
    p.exp_.setType(t);
    return null;
  }

  /**
   * Conditional: if and else
   * if with expression followed by a statement, else,
   * and another statement.
   */
  public Stm visit(SIfElse p, FuncType arg) {
    checkBool(p.exp_);
    enterScope();
    p.stm_1.accept(this, arg);
    leaveScope();
    enterScope();
    p.stm_2.accept(this, arg);
    leaveScope();
    return null;
  }

  /**
   * Any list of statements (including empty list) between
   * curly brackets.
   */
  public Stm visit(SBlock p, FuncType arg) {
    enterScope();
    for(Stm s : p.liststm_)
      s.accept(this, arg);
    leaveScope();
    return null;
  }

  /**
   * While loop with an expression in paranthese floowed by
   * a statement.
   */
  public Stm visit(SWhile p, FuncType arg) {
    checkBool(p.exp_);
    enterScope();
    p.stm_.accept(this, arg);
    leaveScope();
    return null;
  }

  public Stm visit(SReturn p, FuncType arg) {
    Type t = p.exp_.accept(this, null);
    p.exp_.isSubType = subType(t, arg.rt);
    p.exp_.setType(t);
    return null;
  }

  /* ==================== Expressions ==================== */
  /* Literals */
  public Type visit(EInt p, Void arg) {
    p.setType(INT);
    return INT;
  } 
  public Type visit(ETrue p, Void arg) {
    p.setType(BOOL);
    return BOOL;
  }
  public Type visit(EFalse p, Void arg) {
    p.setType(BOOL);
    return BOOL;
  }

  /* Variable */
  public Type visit(EId p, Void arg) {
    return lookupVar(p.id_);
  }

  /* Arithmetic operations */
  public Type visit(EAdd p, Void arg) {
		return arithExp(p.exp_1, p.exp_2);
  }
  public Type visit(ESub p, Void arg) {
    return arithExp(p.exp_1, p.exp_2);
  }
  public Type visit(EMul p, Void arg) {
    return arithExp(p.exp_1, p.exp_2);
  }
  public Type visit(EDiv p, Void arg) {
		return arithExp(p.exp_1, p.exp_2);
  }

  /* Logic operations */
  public Type visit(ELt p, Void arg) {
    return checkComparison(p.exp_1, p.exp_2);
  }
  public Type visit(EGt p, Void arg) {
    return checkComparison(p.exp_1, p.exp_2);
  }
  public Type visit(EOr p, Void arg) {
		return logicExp(p.exp_1, p.exp_2);
  }
  public Type visit(EAnd p, Void arg) {
		return logicExp(p.exp_1, p.exp_2);
  }
  public Type visit(ENeq p, Void arg) {

		return logicExp(p.exp_1, p.exp_2);
  }
  public Type visit(EEq p, Void arg) {
		return logicExp(p.exp_1, p.exp_2);
  }
  public Type visit(EGeq p, Void arg) {
		return checkComparison(p.exp_1, p.exp_2);
  }
  public Type visit(ELEq p, Void arg) {
		return checkComparison(p.exp_1, p.exp_2);
  }
  public Type visit(EDecr p, Void arg) {
    Type pt = checkNumber(lookupVar(p.id_));
    return pt;
  }
  public Type visit(EIncr p, Void arg) {
    Type pt = checkNumber(lookupVar(p.id_));
    return pt;
  }
  public Type visit(EPDecr p, Void arg) {
    Type pt = checkNumber(lookupVar(p.id_));
    return pt;
  }
  public Type visit(EPIncr p, Void arg) {
    Type pt = checkNumber(lookupVar(p.id_));
    return pt;
  }

  /* */
  public Type visit(ECall p, Void arg) {
    // Get the function from signatures
    FuncType ft = lookupFunc(p.id_);

    // Check if function exists
    if(ft == null)
      throw new TypeException("Undefined function");

    // Check same size parameters
    if(ft.args.size() != p.listexp_.size())
      throw new TypeException("Wrong number of arguments in function call");

    /* Go through the expressions for the function call
     * to make sure the correct types are used.
     **/
    int i = 0;
    for(Exp e : p.listexp_) {
      e.isSubType = subType(e.accept(this, null), ((ADecl)ft.args.get(i++)).type_);
    }
    return ft.rt;
  }

  /* Assign */
  public Type visit(EAss p, Void arg) {
    Type pt = lookupVar(p.id_);
    Type pt2 = p.exp_.accept(this, null);
    p.exp_.isSubType = subType(pt2, pt);

    return pt; // get type mapped to identifier
  }
}

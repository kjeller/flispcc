package compiler;

import java.util.*;
import C.Absyn.*;
import C.PrettyPrinter;
import compiler.FunType;
import compiler.Code;

public class Compiler implements
  Program.Visitor<Void, Void>,
  Def.Visitor<Void, Void>,
  Arg.Visitor<Void, Void>,
  Stm.Visitor<Void, Void>,
  Exp.Visitor<Void, Exp>
{
  // The output of the compiler is a list of strings.
  LinkedList<String> output;

  // Signature mapping function names to their JVM name and type
  Map<String, Func> sig;

   // Number of locals needed for current function
  int limitLocals = 0;

  // Max stack size for current function and
  // stack size in current function
  Stack stack = new Stack(0, 0);

  // Global counter to for next label
  int nextLabel = 0;

  // Next free address
  int nextLocal = 0;

  // Context mapping variable id to their type and address
  LinkedList<Map<String, CtxEntry>> ctx;

  // Mapping global variable id to their type and address
  // Note: must be in its own map otherwise the local variable count
  // would be incorrect from getVarCount().
  Map<String, CtxEntry> global;

  // Built-in types
  final Type INT    = new TInt();
  final Type BOOL   = new TBool();
  final Type VOID   = new TVoid();

  // Compile C-- AST to a .j source file (returned as String).
  // name should be just the class name without file extension.
  public String compile(String name, C.Absyn.Program p) {
    // Initialize output
    output = new LinkedList();

    // Output boilerplate
    output.add(";;----------------------------------------+\n");
    output.add(";; flispcc assembly output\n");
    output.add(";; Command: flispcc ...         \n");
    output.add(String.format(";; Compiled: %s   \n", new Date()));
    output.add(";; Author: Karl Strålman        \n");
    output.add(";;----------------------------------------+\n");

    sig = new TreeMap();

    // Built-in functions - currently not used
    ListArg intArg = new ListArg();
    intArg.add(new ADecl(INT, "x"));

    sig.put("printInt",
        new Func("Runtime/printInt",
          new FunType(VOID, intArg)));

    sig.put("readInt",
        new Func("Runtime/readInt",
          new FunType(INT, new ListArg())));
    // Add user-defined functions
    for(Def d : ((Prg)p).listdef_) {
      if(d instanceof DFunc) {
      DFunc def = (DFunc)d;
      sig.put(def.id_,
          new Func(String.format("%s/%s", name, def.id_),
            new FunType(def.type_, def.listarg_)));
      }
    }
    // Initialize global variable storage
    global = new TreeMap<>();
    // Start compiling program
    compile(p);

    // Concatenate strings in output to .j file content.
    StringBuilder jtext = new StringBuilder();
    for (String s: output) {
      jtext.append(s);
    }
    return jtext.toString();
  }

  /*====(AUX) Compilation and helper functions ====*/

  public void compile(Program p) {
    p.accept(this, null);
  }

  public void compile(Def d) {
    d.accept(this, null);
  }

  public void compile(Arg a) {
    a.accept(this, null);
  }

  public void compile(Stm s) {
    s.accept(this, null);
  }

  public void compile(Exp e, Exp a) {
    e.accept(this, a);
  }

  public void emit(Code c) {
    String formatting = "\t\t";
    if(c instanceof Target || c instanceof VarTarget)
      formatting = "";

    output.add(String.format("%s%s",
          formatting, c.accept(new CodeToAssembler(stack))));
  }
  public void pushBlock() {
    ctx.push(new TreeMap());
  }
  public void popBlock() {
    ctx.pop();
  }

  /* Returns variable entry from ctx map */
  public CtxEntry lookupVar(String id) {
    // Check global variable first
    CtxEntry gVar = global.get(id);
    if(gVar != null)
      return gVar;

    // Then the local variables from within contexts
    for(Map<String, CtxEntry> m : ctx) {
      CtxEntry e = m.get(id);
      if(e != null)
        return e;
    }
    return null;
  }

  public Func lookupFunc(String id) {
    return sig.get(id);
  }

  /* Adds variable to context map  */
  public void addVar(String id, Type t) {
    ctx.peek().put(id, new CtxEntry(t, nextLocal, false));
    nextLocal++;
    limitLocals++;
  }

  public void addGlobal(String id, Type t) {
    global.put(id, new CtxEntry(t, 0, true));
  }

  public Label newLabel() {
    return new Label(nextLabel++);
  }

  public Type arithExp(Exp e1, Exp e2) {
      return INT;
  }

  public int getVarCount() {
    return ctx.peek().size();
  }

  public int getGlobVarCount() {
    return global.size();
  }

  // Ugly solution
  // TODO: Fix this ugly mess
  public Code branchType(Exp e, Label done) {
    Code c = null;
    if(e instanceof EGt) {
      c = new Bgt(done);
    } else if(e instanceof ELt) {
      c = new Blt(done);
    } else if(e instanceof ENeq) {
      c = new Bne(done);
    } else if(e instanceof EEq) { 
      c = new Beq(done); 
    } else if(e instanceof ETrue || e instanceof EFalse) {
      emit(new Test());
      c = new Beq(done);
    } else {
      // Error
      c = new Bra(done);
    }
    return c;
  }

  /*================ Program ===================*/
  public Void visit(Prg p, Void arg) {
    for(Def d : p.listdef_)
      compile(d);

    return null;
  }

   /*================ Function defs. ============*/
  public Void visit(DFunc p, Void arg) {
    ctx = new LinkedList();
    ctx.push(new TreeMap());

    // Reset context specific counters
    nextLocal    = 0;
    limitLocals  = 0;
    stack.count  = 0;
    stack.limit  = 0;

    // Save output from extermination
    LinkedList<String> savedOutput = output;
    output = new LinkedList();

    for(Arg a : p.listarg_)
      compile(a);
    for(Stm s : p.liststm_)
      compile(s);

    // Fetch new input and add to old
    LinkedList<String> newOutput = output;
    output = savedOutput;

    // Add to output
    Func f = new Func(p.id_,  new FunType(p.type_, p.listarg_));
    // TODO: do something with functions


    // Global varibles declared here
    if(getGlobVarCount() > 0) {
      emit(new Org(0));
      for(String key : global.keySet()) {
        // This can be extended when strings are implemented (1 byte per character)
        emit(new VarTarget(key, new Rmb(1)));
      }
      output.add("\n");
    }

    // Program starts here
    emit(new Org(20));

    // Count local varible size to determine how much space on stack
    int varsize = getVarCount();
    if(varsize > 0)
      emit(new Leasp(varsize)); //Add

    for(String s : newOutput)
      output.add(s);

    if(varsize > 0)
      emit(new Leasp(varsize));
    return null;
  }

  public Void visit(DGlob p, Void arg) {
    if(p.stm_ instanceof SDecls) {
      SDecls s = (SDecls)p.stm_;
      for(String id: s.listid_) {
        if(!s.type_.equals(VOID))
          addGlobal(id, s.type_);
       }
      return null;
    }

   /* Does not work since I do not know when to run exp.
    * Exp cannot run outside ORG 20 where the program starts..
    * if(p.stm_ instanceof SInit) {
      SInit s = (SInit)p.stm_;
      compile(s.exp_);
      addGlobal(s.id_, s.type_);
      return null;
    }*/
    throw new RuntimeException("Only global declarations are allowed.");
  }

  /*================== Function Args =================== */
  public Void visit(ADecl p, Void arg) {
    addVar(p.id_, p.type_);
    CtxEntry entry = lookupVar(p.id_);
    emit(new Comment(PrettyPrinter.print(p)));
    emit(new Load(AddrMethod.NS, entry.addr));
    return null;
   }

  /*==================== Statements ====================*/
  public Void visit(SDecls p, Void arg) {
    emit(new Comment(PrettyPrinter.print(p)));
    for(String id: p.listid_) {
      if(!p.type_.equals(VOID))
        addVar(id, p.type_);
    }
    return null;
  }

  public Void visit(SInit p, Void arg) {
    emit(new Comment(PrettyPrinter.print(p)));
    compile(p.exp_, p.exp_);
    addVar(p.id_, p.type_);
    CtxEntry entry = lookupVar(p.id_);
    emit(new Store(AddrMethod.NS, entry.addr));
    return null;
  }

  public Void visit(SExp p, Void arg) {
    emit(new Comment(PrettyPrinter.print(p.exp_)));
    compile(p.exp_, p.exp_);
    return null;
  }

  public Void visit(SIfElse p, Void arg) {
    Label lfalse = newLabel();
    Label end = newLabel();

    emit(new Comment("test if-condition (" + PrettyPrinter.print(p.exp_) + ")\n"));
    compile(p.exp_, p.exp_);
    //TODO: Implement the different branches here 
    emit(branchType(p.exp_, lfalse));
    emit(new Comment("when (" + PrettyPrinter.print(p.exp_) + ") do: \n"));
    pushBlock();
     // Save output from extermination
    LinkedList<String> savedOutput = output;
    output = new LinkedList();

    compile(p.stm_1);

     // Must do this for scoping
    LinkedList<String> newOutput = output;
    output = savedOutput;
 
    // Local variables in scope
    //TODO: fungerar inte som det ska 
    //emit(new Leasp(-getVarCount())); 
    for(String s : newOutput)
      output.add(s);
   // emit(new Leasp(getVarCount()));

    popBlock();
    emit(new Bra(end));
    emit(new Comment("unless (" + PrettyPrinter.print(p.exp_) + ") do: \n"));
    emit(new Target(lfalse));
    pushBlock();
    
    savedOutput = output;
    output = new LinkedList();

    compile(p.stm_2);

    // Must do this for scoping
    newOutput = output;
    output = savedOutput;

    // Local variables in scope
    //emit(new Leasp(-getVarCount())); 
    for(String s : newOutput)
      output.add(s);
    //emit(new Leasp(getVarCount()));

    popBlock();
    emit(new Target(end));
    output.add("\t\tNOP\n"); // kanske inte behövs
    return null;
  }

  /**
   * Any list of statements (including empty list) between
   * curly brackets.
   */
  public Void visit(SBlock p, Void arg) {
    pushBlock();
    for(Stm s : p.liststm_)
      compile(s);
    popBlock();
    return null;
  }

  /**
   * While loop with an expression in paranthese floowed by
   * a statement.
   */
  public Void visit(SWhile p, Void arg) {
    Label start = newLabel();
    Label done  = newLabel();

    emit(new Comment("test while-condition (" + PrettyPrinter.print(p.exp_) + ")\n"));
     // Start label (eg. L0)
    emit(new Target(start));
    //Check condition
    compile(p.exp_, p.exp_);
    // Compare and jump to "done" if equal
    emit(branchType(p.exp_, done));
    // newblock with more work
    emit(new Comment("while (" + PrettyPrinter.print(p.exp_) + ") do:\n"));
    pushBlock();
    compile(p.stm_);
    popBlock();
    // Loop more
    emit(new Bra(start));
    emit(new Target(done));
    return null;
  }

  public Void visit(SReturn p, Void arg) {
    throw new RuntimeException("Not yet implemented");
  }

  /* ==================== Expressions ==================== */

  /* Literals */
  public Void visit(EInt p, Exp arg) {
    emit(arg.accept(new EIntVisitor(), p));
    return null;
  }

  public Void visit(ETrue p, Exp arg) {
    emit(new Load(AddrMethod.IMMEDIATE, 1));
		return null;
  }

  public Void visit(EFalse p, Exp arg) {
    emit(new Load(AddrMethod.IMMEDIATE, 0));
    return null;
  }

  /* Variable */
  public Void visit(EId p, Exp arg) {
    emit(arg.accept(new EIdVisitor(), p));
    return null;
  }

  /* Arithmetic operations */
  public Void visit(EAdd p, Exp arg) {
    if(!(arg instanceof EAdd))
      compile(p.exp_1, arg);
    else
      compile(p.exp_1, p.exp_1);
    compile(p.exp_2, p);
		return null;
  }
  public Void visit(ESub p, Exp arg) {
    emit(new Comment(PrettyPrinter.print(p)));
    compile(p.exp_1, p.exp_2);
    //emit(new Sub(arithExp(p.exp_1, p.exp_2)));
		return null;
  }
  public Void visit(EMul p, Exp arg) {
    throw new RuntimeException("Multiplication not yet supported.");
  }
  public Void visit(EDiv p, Exp arg) {
    throw new RuntimeException("Divide not yet supported");
  }

  /* Logic operations
   * leaves 1 or 0 on register A when compiled
   * TODO: Still needs testing
   * */
  public Void visit(EOr p, Exp arg) {
    Label ltrue = newLabel();
    emit(new Comment(PrettyPrinter.print(p)));
    emit(new Load(AddrMethod.IMMEDIATE, 1));
    // Lazy eval
    compile(p.exp_1, p);
    emit(new Test());
    emit(new Bne(ltrue));
    // Eval next exp
    compile(p.exp_2, p);
    emit(new Test());
    emit(new Bne(ltrue));
    //FALSE
    emit(new Load(AddrMethod.IMMEDIATE, 0));
    emit(new Target(ltrue));
		return null;
  }
  public Void visit(EAnd p, Exp arg) {
    Label lfalse = newLabel();
    emit(new Comment(PrettyPrinter.print(p)));
    emit(new Load(AddrMethod.IMMEDIATE, 0));
    // Lazy eval
    compile(p.exp_1, p);
    emit(new Test());
    emit(new Beq(lfalse));
    // Eval next exp
    compile(p.exp_2, p);
    emit(new Test());
    emit(new Beq(lfalse));
    //FALSE
    emit(new Load(AddrMethod.IMMEDIATE, 1));
    emit(new Target(lfalse));
		return null;
  }

  public Void visit(ELt p, Exp arg) {
    compile(p.exp_1, p.exp_1);
    compile(p.exp_2, p);
    return null;
  }

  public Void visit(EGt p, Exp arg) {
    compile(p.exp_1, p.exp_1);
    compile(p.exp_2, p);
    return null;
  }
  public Void visit(ENeq p, Exp arg) {
    compile(p.exp_1, p.exp_1);
    compile(p.exp_2, p);
    return null;
  }
  public Void visit(EEq p, Exp arg) {
    compile(p.exp_1, p.exp_1);
    compile(p.exp_2, p);
    return null;
  }
  public Void visit(EGeq p, Exp arg) {
    compile(p.exp_1, p.exp_1);
    compile(p.exp_2, p);
    return null;
  }
  public Void visit(ELEq p, Exp arg) {
    compile(p.exp_1, p.exp_1);
    compile(p.exp_2, p);
    return null;
  }
  public Void visit(EDecr p, Exp arg) {
    CtxEntry x = lookupVar(p.id_);
    if(x.global) {
      emit(new Dec(AddrMethod.ABSOLUTE, p.id_));
      emit(new Load(AddrMethod.ABSOLUTE, p.id_));
    } else {
      emit(new Dec(AddrMethod.NS, x.addr));
      emit(new Load(AddrMethod.NS, x.addr));
    }
	  return null;
  }
  public Void visit(EIncr p, Exp arg) {
  	CtxEntry x = lookupVar(p.id_);
    if(x.global) {
      emit(new Inc(AddrMethod.ABSOLUTE, p.id_));
      emit(new Load(AddrMethod.ABSOLUTE, p.id_));
    } else {
      emit(new Inc(AddrMethod.NS, x.addr));
      emit(new Load(AddrMethod.NS, x.addr));
    }
	  return null;
  }
  public Void visit(EPDecr p, Exp arg) {
		CtxEntry x = lookupVar(p.id_);
    if(x.global) {
      emit(new Load(AddrMethod.ABSOLUTE, p.id_));
      emit(new Dec(AddrMethod.ABSOLUTE, p.id_));
    } else {
      emit(new Load(AddrMethod.NS, x.addr));
      emit(new Dec(AddrMethod.NS, x.addr));
    }
	  return null;
  }
  public Void visit(EPIncr p, Exp arg) {
		CtxEntry x = lookupVar(p.id_);
    if(x.global) {
      emit(new Load(AddrMethod.ABSOLUTE, p.id_));
      emit(new Inc(AddrMethod.ABSOLUTE, p.id_));
    } else {
      emit(new Load(AddrMethod.NS, x.addr));
      emit(new Inc(AddrMethod.NS, x.addr));
    }
	  return null;
  }

  public Void visit(ECall p, Exp arg) {
    throw new RuntimeException("Not yet implemented");
  }

  /* Assign */
  public Void visit(EAss p, Exp arg) {
    CtxEntry entry = lookupVar(p.id_);
    compile(p.exp_, arg);
    if(entry.global)
      emit(new Store(AddrMethod.ABSOLUTE, p.id_));
    else
      emit(new Store(AddrMethod.NS, entry.addr));
    return null;
  }

  class EIntVisitor implements Exp.Visitor<Code, EInt> {
    public Code visit(EInt p, EInt arg)   { return new Load(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(ETrue p, EInt arg)  { return new Load(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(EFalse p, EInt arg) { return new Load(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(EId p, EInt arg)    { return new Load(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(ECall p, EInt arg)  { return new Load(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(EPIncr p, EInt arg) { return new Load(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(EPDecr p, EInt arg) { return new Load(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(EIncr p, EInt arg)  { return new Load(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(EDecr p, EInt arg)  { return new Load(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(EMul p, EInt arg)   { return new Load(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(EDiv p, EInt arg)   { return new Load(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(EAdd p, EInt arg)   { return new Add(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(ESub p, EInt arg)   { return new Sub(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(ELt p, EInt arg)    { return new Cmp(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(EGt p, EInt arg)    { return new Cmp(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(ELEq p, EInt arg)   { return new Cmp(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(EGeq p, EInt arg)   { return new Cmp(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(EEq p, EInt arg)    { return new Cmp(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(ENeq p, EInt arg)   { return new Cmp(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(EAnd p, EInt arg)   { return new Load(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(EOr p, EInt arg)    { return new Load(AddrMethod.IMMEDIATE, arg.integer_); }
    public Code visit(EAss p, EInt arg)   { return new Load(AddrMethod.IMMEDIATE, arg.integer_); }
  }

  // Todo: Clean up this mess
  class EIdVisitor implements Exp.Visitor<Code, EId> {
    public Code load(Exp p, EId arg) {
      CtxEntry entry = lookupVar(arg.id_);
      if(entry.global)
        return new Load(AddrMethod.ABSOLUTE, arg.id_);
      return new Load(AddrMethod.NS, entry.addr);
    }

    public Code cmp(Exp p, EId arg) {
      CtxEntry entry = lookupVar(arg.id_);
      if(entry.global)
        return new Cmp(AddrMethod.ABSOLUTE, arg.id_);
      return new Cmp(AddrMethod.NS, entry.addr);
    }

    /* Atoms cannot be in other atom. */
    public Code visit(EInt p, EId arg)    { return load(p, arg); }
    public Code visit(ETrue p, EId arg)   { return load(p, arg); }
    public Code visit(EFalse p, EId arg)  { return load(p, arg); }
    public Code visit(EId p, EId arg)    { return load(p, arg); }

    public Code visit(ECall p, EId arg)  { throw new RuntimeException("Not implemented"); }
    public Code visit(EPIncr p, EId arg) { return load(p, arg); }
    public Code visit(EPDecr p, EId arg) { return load(p, arg); }
    public Code visit(EIncr p, EId arg)  { return load(p, arg); }
    public Code visit(EDecr p, EId arg)  { return load(p, arg); }
    public Code visit(EMul p, EId arg)   { return load(p, arg); }
    public Code visit(EDiv p, EId arg)   { return load(p, arg); }
    public Code visit(EAdd p, EId arg)   {
      CtxEntry entry = lookupVar(arg.id_);
      if(entry.global)
        return new Add(AddrMethod.ABSOLUTE, arg.id_);
      return new Add(AddrMethod.NS, entry.addr);
    }
    public Code visit(ESub p, EId arg) {
      CtxEntry entry = lookupVar(arg.id_);
        if(entry.global)
          return new Sub(AddrMethod.ABSOLUTE, arg.id_);
        return new Sub(AddrMethod.NS, entry.addr);
    }
    public Code visit(ELt p, EId arg)   { return cmp(p, arg); }
    public Code visit(EGt p, EId arg)   { return cmp(p, arg); }
    public Code visit(ELEq p, EId arg)  { return cmp(p, arg); }
    public Code visit(EGeq p, EId arg)  { return cmp(p, arg); }
    public Code visit(EEq p, EId arg)   { return cmp(p, arg); }
    public Code visit(ENeq p, EId arg)  { return cmp(p, arg); }
    public Code visit(EAnd p, EId arg)  { return load(p, arg); }
    public Code visit(EOr p, EId arg)   { return load(p, arg); }
    public Code visit(EAss p, EId arg)  { return load(p, arg); }
  }
}

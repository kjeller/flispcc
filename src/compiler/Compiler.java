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
  Exp.Visitor<Void, Void>
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

  // Built-in types
  final Type INT    = new TInt();
  //final Type DOUBLE = new TDouble();
  final Type BOOL   = new TBool();
  final Type VOID   = new TVoid();

  // Compile C-- AST to a .j source file (returned as String).
  // name should be just the class name without file extension.
  public String compile(String name, C.Absyn.Program p) {
    // Initialize output
    output = new LinkedList();

    // Output boilerplate
    output.add(".class public " + name + "\n");
    output.add(".super java/lang/Object\n");
    output.add("\n");
    output.add(".method public <init>()V\n");
    output.add("  .limit locals 1\n");
    output.add("\n");
    output.add("  aload_0\n");
    output.add("  invokespecial java/lang/Object/<init>()V\n");
    output.add("  return\n");
    output.add("\n");
    output.add(".end method\n");
    output.add("\n");
    output.add(".method public static main([Ljava/lang/String;)V\n");
    output.add("  .limit locals 1\n");
    output.add("  .limit stack  1\n");
    output.add("\n");
    output.add("  invokestatic " + name + "/main()I\n");
    output.add("  pop\n");
    output.add("  return\n");
    output.add("\n");
    output.add(".end method\n");
    output.add("\n");

    sig = new TreeMap();

    // Built-in functions
    ListArg intArg = new ListArg();
    intArg.add(new ADecl(INT, "x"));

    ListArg doubleArg = new ListArg();
    doubleArg.add(new ADecl(DOUBLE, "x"));

    sig.put("printInt",
        new Func("Runtime/printInt",
          new FunType(VOID, intArg)));
    sig.put("printDouble",
        new Func("Runtime/printDouble",
          new FunType(VOID, doubleArg)));
    sig.put("readInt",
        new Func("Runtime/readInt",
          new FunType(INT, new ListArg())));
    sig.put("readDouble",
        new Func("Runtime/readDouble",
          new FunType(DOUBLE, new ListArg())));

    // Add user-defined functions
    for(Def d : ((Prg)p).listdef_) {
      DFunc def = (DFunc)d;
      sig.put(def.id_,
          new Func(String.format("%s/%s", name, def.id_),
            new FunType(def.type_, def.listarg_)));
    }

    // Start compiling program
    compile(p);

    // Concatenate strings in output to .j file content.
    StringBuilder jtext = new StringBuilder();
    for (String s: output) {
      jtext.append(s);
    }
    return jtext.toString();
  }

  /*==== Compilation and helper functions ====*/

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

  public void compile(Exp e) {
    e.accept(this, null);
  }

  public void compile(Exp e1, Exp e2) {
    compile(e1);
    if(e1.isSubType)
      emit(new I2D());
    compile(e2);
    if(e2.isSubType)
      emit(new I2D());
  }

  public void emit(Code c) {
    output.add(c.accept(new CodeToJVM(stack)));
  }
  public void pushBlock() {
    ctx.push(new TreeMap());
  }
  public void popBlock() {
    ctx.pop();
  }
  public void extend(String x, Type t) {
  }

  public void extend(Def d) {
  }

  /* Returns variable entry from ctx map */
  public CtxEntry lookupVar(String id) {
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
    ctx.peek().put(id, new CtxEntry(t, nextLocal));
    nextLocal++;
    limitLocals++;
    if(t.equals(DOUBLE)){
      nextLocal++;
      limitLocals++;
    }
  }

  public Label newLabel() {
    return new Label(nextLabel++);
  }

  public Type arithExp(Exp e1, Exp e2) {
    if(e1.isSubType || e2.isSubType)
      return DOUBLE;
    if(e1.getType().equals(INT) && e1.getType().equals(INT))
      return INT;
    return DOUBLE;
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
    stack.stackc = 0;
    stack.stacklim = 0;

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
    output.add(String.format("\n.method public static %s\n", f.toJVM()));
    output.add(String.format("\t.limit locals %d\n", limitLocals));
    output.add(String.format("\t.limit stack %d\n\n", stack.stacklim));
    for(String s : newOutput)
      output.add("\t" + s);

    if(p.type_.equals(VOID))
      emit(new Return(VOID));

    output.add("\n.end method\n");
    return null;
  }

  /*================== Function Args =================== */
  public Void visit(ADecl p, Void arg) {
    addVar(p.id_, p.type_);
    CtxEntry entry = lookupVar(p.id_);
    emit(new Comment(PrettyPrinter.print(p)));
    emit(new Load(entry.type, entry.addr));
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
    compile(p.exp_);
    if(p.exp_.isSubType)
      emit(new I2D());
    addVar(p.id_, p.type_);
    CtxEntry entry = lookupVar(p.id_);
    emit(new Store(entry.type, entry.addr));
    return null;
  }

  public Void visit(SExp p, Void arg) {
    emit(new Comment(PrettyPrinter.print(p.exp_)));
    compile(p.exp_);
    if(!p.exp_.getType().equals(VOID))
      emit(new Pop(p.exp_.getType()));
    return null;
  }

  public Void visit(SIfElse p, Void arg) {
    Label lfalse = newLabel();
    Label end = newLabel();

    emit(new Comment("test if-condition (" + PrettyPrinter.print(p.exp_) + ")\n"));
    compile(p.exp_);
    emit(new IfEq(lfalse));
    emit(new Comment("when (" + PrettyPrinter.print(p.exp_) + ") do: \n"));
    pushBlock();
    compile(p.stm_1);
    popBlock();
    emit(new Goto(end));
    emit(new Comment("unless (" + PrettyPrinter.print(p.exp_) + ") do: \n"));
    emit(new Target(lfalse));
    pushBlock();
    compile(p.stm_2);
    popBlock();
    emit(new Target(end));
    output.add("\n nop");
    return null;
  }

  /**
   * Any list of statements (including empty list) between
   * curly brackets.
   */
  public Void visit(SBlock p, Void arg) {
    pushBlock();
    emit(new Comment("New block"));
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
    compile(p.exp_);
    // Compare and jump to "done" if equal
    emit(new IfEq(done));
    // newblock with more work
    emit(new Comment("while (" + PrettyPrinter.print(p.exp_) + ") do:\n"));
    pushBlock();
    compile(p.stm_);
    popBlock();
    // Loop more
    emit(new Goto(start));

    // You done now
    emit(new Target(done));
    return null;
  }

  public Void visit(SReturn p, Void arg) {
    emit(new Comment(PrettyPrinter.print(p)));
    compile(p.exp_);
    if (p.exp_.isSubType) {
      emit(new I2D());
      emit(new Return(DOUBLE));
    } else
      emit(new Return(p.exp_.getType()));
    return null;
  }

  /* ==================== Expressions ==================== */
  /* Literals */
  public Void visit(EInt p, Void arg) {
  	emit(new Ldc(INT, p.integer_));
    return null;
  }
  public Void visit(EDouble p, Void arg) {
    emit(new Ldc(DOUBLE, p.double_));
    return null;
  }
  public Void visit(ETrue p, Void arg) {
    emit(new IConst(1));
		return null;
  }
  public Void visit(EFalse p, Void arg) {
		emit(new IConst(0));
    return null;
  }

  /* Variable */
  public Void visit(EId p, Void arg) {
    emit(new Comment(PrettyPrinter.print(p)));
		CtxEntry entry = lookupVar(p.id_);
    emit(new Load(entry.type, entry.addr));
    return null;
  }

  /* Arithmetic operations */
  public Void visit(EAdd p, Void arg) {
    compile(p.exp_1, p.exp_2);
    emit(new Add(arithExp(p.exp_1, p.exp_2)));
		return null;
  }
  public Void visit(ESub p, Void arg) {
    emit(new Comment(PrettyPrinter.print(p)));
    compile(p.exp_1, p.exp_2);
    emit(new Sub(arithExp(p.exp_1, p.exp_2)));
		return null;
  }
  public Void visit(EMul p, Void arg) {
    emit(new Comment(PrettyPrinter.print(p)));
    compile(p.exp_1, p.exp_2);
    emit(new Mul(arithExp(p.exp_1, p.exp_2)));
    return null;
  }
  public Void visit(EDiv p, Void arg) {
    emit(new Comment(PrettyPrinter.print(p)));
    compile(p.exp_1, p.exp_2);
    emit(new Div(arithExp(p.exp_1, p.exp_2)));
    return null;
  }

  /* Logic operations */

  public Void visit(EOr p, Void arg) {
    Label ltrue = newLabel();
    emit(new Comment(PrettyPrinter.print(p)));
    emit(new Push(INT, 1));
    // Lazy eval
    compile(p.exp_1);
    emit(new IfNEq(ltrue));
    // Eval next exp
    compile(p.exp_2);
    emit(new IfNEq(ltrue));
    emit(new Pop(INT));
    //FALSE
    emit(new Push(INT, 0));
    emit(new Target(ltrue));
		return null;
  }
  public Void visit(EAnd p, Void arg) {
    Label lfalse = newLabel();
    emit(new Comment(PrettyPrinter.print(p)));
    emit(new Push(INT, 0));
    // Lazy eval
    compile(p.exp_1);
    emit(new IfEq(lfalse));
    // Eval next exp
    compile(p.exp_2);
    emit(new IfEq(lfalse));
    emit(new Pop(INT));
    //FALSE
    emit(new Push(INT, 1));
    emit(new Target(lfalse));
		return null;
  }

   public Void visit(ELt p, Void arg) {
    Label ltrue = newLabel();
    emit(new Comment(PrettyPrinter.print(p)));
    Type t1 = p.exp_1.getType();
    Type t2 = p.exp_2.getType();

    emit(new Push(INT, 1));
    compile(p.exp_1, p.exp_2);
    if(t1.equals(INT) && t2.equals(INT))
      emit(new IfCmpLt(ltrue));
    else {
      emit(new Dcmpg());
      emit(new IfLt(ltrue));
    }
    emit(new Pop(INT));
    emit(new Push(INT, 0));
    emit(new Target(ltrue));
   	return null;
  }

  public Void visit(EGt p, Void arg) {
    Label ltrue = newLabel();
    emit(new Comment(PrettyPrinter.print(p)));
    Type t1 = p.exp_1.getType();
    Type t2 = p.exp_2.getType();

    emit(new Push(INT, 1));
    compile(p.exp_1, p.exp_2);
    if(t1.equals(INT) && t2.equals(INT))
      emit(new IfCmpGt(ltrue));
    else {
      emit(new Dcmpg());
      emit(new IfGt(ltrue));
    }
    emit(new Pop(INT));
    emit(new Push(INT, 0));
    emit(new Target(ltrue));
   	return null;
  }
  public Void visit(ENeq p, Void arg) {
    Label ltrue = newLabel();
    emit(new Comment(PrettyPrinter.print(p)));
    Type t1 = p.exp_1.getType();
    Type t2 = p.exp_2.getType();

    emit(new Push(INT, 1));
    compile(p.exp_1, p.exp_2);
    if(t1.equals(INT) && t2.equals(INT) || t1.equals(BOOL))
      emit(new IfCmpNEq(ltrue));
    else {
      emit(new Dcmpg());
      emit(new IfNEq(ltrue));
    }
    emit(new Pop(INT));
    emit(new Push(INT, 0));
    emit(new Target(ltrue));
   	return null;
  }

  public Void visit(EEq p, Void arg) {
    Label ltrue = newLabel();
    emit(new Comment(PrettyPrinter.print(p)));
    Type t1 = p.exp_1.getType();
    Type t2 = p.exp_2.getType();

    emit(new Push(INT, 1));
    compile(p.exp_1, p.exp_2);
    if(t1.equals(INT) && t2.equals(INT) || t1.equals(BOOL))
      emit(new IfCmpEq(ltrue));
    else {
      emit(new Dcmpg());
      emit(new IfEq(ltrue));
    }
    emit(new Pop(INT));
    emit(new Push(INT, 0));
    emit(new Target(ltrue));
   	return null;
  }
  public Void visit(EGeq p, Void arg) {
    Label ltrue = newLabel();
    emit(new Comment(PrettyPrinter.print(p)));
    Type t1 = p.exp_1.getType();
    Type t2 = p.exp_2.getType();

    emit(new Push(INT, 1));
    compile(p.exp_1, p.exp_2);
    if(t1.equals(INT) && t2.equals(INT))
      emit(new IfCmpGe(ltrue));
    else {
      emit(new Dcmpg());
      emit(new IfGe(ltrue));
    }
    emit(new Pop(INT));
    emit(new Push(INT, 0));
    emit(new Target(ltrue));
   	return null;
  }
  public Void visit(ELEq p, Void arg) {
    Label ltrue = newLabel();
    emit(new Comment(PrettyPrinter.print(p)));
    Type t1 = p.exp_1.getType();
    Type t2 = p.exp_2.getType();

    emit(new Push(INT, 1));
    compile(p.exp_1, p.exp_2);
    if(t1.equals(INT) && t2.equals(INT))
      emit(new IfCmpLe(ltrue));
    else {
      emit(new Dcmpg());
      emit(new IfLe(ltrue));
    }
    emit(new Pop(INT));
    emit(new Push(INT, 0));
    emit(new Target(ltrue));
   	return null;
  }
  public Void visit(EDecr p, Void arg) {
		CtxEntry entry = lookupVar(p.id_);
    emit(new Comment(PrettyPrinter.print(p)));
    emit(new Load(entry.type, entry.addr));
    if(entry.type.equals(INT))
      emit(new Ldc(INT, 1));
    else
      emit(new Ldc(DOUBLE, 1.0));
    emit(new Sub(entry.type));
    emit(new Store(entry.type, entry.addr));
    emit(new Load(entry.type, entry.addr));
    return null;
  }
  public Void visit(EIncr p, Void arg) {
  	CtxEntry entry = lookupVar(p.id_);
    emit(new Comment(PrettyPrinter.print(p)));
    emit(new Load(entry.type, entry.addr));
    if(entry.type.equals(INT))
      emit(new Ldc(INT, 1));
    else
      emit(new Ldc(DOUBLE, 1.0));
    emit(new Add(entry.type));
    emit(new Store(entry.type, entry.addr));
    emit(new Load(entry.type, entry.addr));
    return null;
  }
  public Void visit(EPDecr p, Void arg) {
		CtxEntry entry = lookupVar(p.id_);
    emit(new Comment(PrettyPrinter.print(p)));
    emit(new Load(entry.type, entry.addr));
    emit(new Load(entry.type, entry.addr));
    if(entry.type.equals(INT))
      emit(new Ldc(INT, 1));
    else
      emit(new Ldc(DOUBLE, 1.0));
    emit(new Sub(entry.type));
    emit(new Store(entry.type, entry.addr));
    return null;
  }
  public Void visit(EPIncr p, Void arg) {
		CtxEntry entry = lookupVar(p.id_);
    emit(new Comment(PrettyPrinter.print(p)));
    emit(new Load(entry.type, entry.addr));
    emit(new Load(entry.type, entry.addr));
    if(entry.type.equals(INT))
      emit(new Ldc(INT, 1));
    else
      emit(new Ldc(DOUBLE, 1.0));
    emit(new Add(entry.type));
    emit(new Store(entry.type, entry.addr));
    return null;
  }

  public Void visit(ECall p, Void arg) {
    emit(new Comment(PrettyPrinter.print(p)));
    Func f = lookupFunc(p.id_);
    for(Exp e : p.listexp_) {
      compile(e);
      if(e.isSubType)
        emit(new I2D());
    }
    emit(new Call(f));
		return null;
  }

  /* Assign */
  public Void visit(EAss p, Void arg) {
  	CtxEntry entry = lookupVar(p.id_);
    emit(new Comment(PrettyPrinter.print(p)));
    compile(p.exp_);
    if(p.exp_.isSubType)
      emit(new I2D());
    emit(new Store(entry.type, entry.addr));
    emit(new Load(entry.type, entry.addr));
		return null;
  }
}

// Code.java
// Originally created by github.com/andreasabel/java-adt
// Edit: Tobias Campbell, Karl Strålman
package compiler;

import compiler.FunType;
import C.Absyn.*;

class Stack {
  public Integer stackc;
  public Integer stacklim;
  public Stack(Integer stackc, Integer stacklim) {
    this.stackc = stackc;
    this.stacklim = stacklim;
  }
}

// Variable information (in context)
class CtxEntry {
  final Type type;
  final Integer addr;

  CtxEntry(Type t, Integer i) {
    type = t;
    addr = i;
  }
}

class Func {
  public String id;
  public FunType funcType;
  public Func (String id, FunType funcType) {
    this.id = id;
    this.funcType = funcType;
  }
  public String toJVM() {
    return id + funcType.toJVM();
  }
}

class Label {
  public int label;
  public Label (int label) {
    this.label = label;
  }
  public String toJVM() {
    return "L" + label;
  }
}

/* ======= CODE ======= */
abstract class Code {
    public abstract <R> R accept (CodeVisitor<R> v);
}

class Comment extends Code {
  public String comment;
  public Comment (String c) { comment = c; }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class I2D extends Code {
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class Store extends Code {
  public Type type;
  public Integer addr;
  public Store (Type type, Integer addr) {
    this.type = type;
    this.addr = addr;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class Load extends Code {
  public Type type;
  public Integer addr;
  public Load (Type type, Integer addr) {
    this.type = type;
    this.addr = addr;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class IConst extends Code {
  public Integer immed;
  public IConst (Integer immed) {
    this.immed = immed;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class Ldc extends Code {
  public Integer ival;
  public Double dval;
  public Type type;

  public Ldc (Type t, Integer i) {
    ival = i;
    type = t;
  }

  public Ldc (Type t, Double d) {
    dval = d;
    type = t;
  }

  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class Pop extends Code {
  public Type type;
  public Pop (Type type) {
    this.type = type;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}
class Push extends Code {
  public Type type;
  public Integer val;
  public Push (Type type, Integer val) {
    this.type = type;
    this.val = val;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class Return extends Code {
  public Type type;
  public Return (Type type) {
    this.type = type;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class Call extends Code {
  public Func func;
  public Call (Func func) {
    this.func = func;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class Target extends Code {
  public Label label;
  public Target (Label label) {
    this.label = label;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class Goto extends Code {
  public Label label;
  public Goto (Label label) {
    this.label = label;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}


/* ===== Logic =====  */

class Dcmpg extends Code {
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}


class IfEq extends Code {
  public Label label;
  public IfEq (Label label) {
    this.label = label;
  }
  public <R> R accept (CodeVisitor<R> v) {
      return v.visit (this);
  }
}

class IfNEq extends Code {
  public Label label;
  public IfNEq (Label label) {
    this.label = label;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class IfGt extends Code {
  public Label label;
  public IfGt (Label label) {
    this.label = label;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class IfGe extends Code {
  public Label label;
  public IfGe (Label label) {
    this.label = label;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class IfLt extends Code {
  public Label label;
  public IfLt (Label label) {
    this.label = label;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class IfLe extends Code {
  public Label label;
  public IfLe (Label label) {
    this.label = label;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class IfCmpEq extends Code {
  public Label label;
  public IfCmpEq (Label label) {
    this.label = label;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class IfCmpNEq extends Code {
  public Label label;
  public IfCmpNEq (Label label) {
    this.label = label;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}
class IfCmpGt extends Code {
  public Label label;
  public IfCmpGt (Label label) {
    this.label = label;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class IfCmpGe extends Code {
  public Label label;
  public IfCmpGe (Label label) {
    this.label = label;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class IfCmpLt extends Code {
  public Label label;
  public IfCmpLt (Label label) {
    this.label = label;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class IfCmpLe extends Code {
  public Label label;
  public IfCmpLe (Label label) {
    this.label = label;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

/* Arithmetic */
class Add extends Code {
  public Type type;
  public Add (Type type) {
    this.type = type;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class Sub extends Code {
  public Type type;
  public Sub (Type type) {
    this.type = type;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class Div extends Code {
  public Type type;
  public Div (Type type) {
    this.type = type;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}

class Mul extends Code {
  public Type type;
  public Mul (Type type) {
    this.type = type;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit (this);
  }
}


interface CodeVisitor<R> {
  public R visit (Comment c);
  public R visit (I2D c);
  public R visit (Store c);
  public R visit (Load c);
  public R visit (IConst c);
  public R visit (Ldc c);
  public R visit (Pop c);
  public R visit (Push c);
  public R visit (Return c);
  public R visit (Call c);
  public R visit (Target c);
  public R visit (Goto c);
  public R visit (Dcmpg c);
  public R visit (IfEq c);
  public R visit (IfNEq c);
  public R visit (IfLt c);
  public R visit (IfLe c);
  public R visit (IfGt c);
  public R visit (IfGe c);
  public R visit (IfCmpEq c);
  public R visit (IfCmpNEq c);
  public R visit (IfCmpLt c);
  public R visit (IfCmpLe c);
  public R visit (IfCmpGt c);
  public R visit (IfCmpGe c);
  public R visit (Add c);
  public R visit (Sub c);
  public R visit (Div c);
  public R visit (Mul c);
}

class CodeToJVM implements CodeVisitor<String> {
  // Stack counter: current, lim
  Stack s;

  public CodeToJVM(Stack s) {
    this.s = s;
  }

  /*
   * Decreases stack depending on type.
   * Int and bool = 1 unit
   * Double = 2 units
   */
  void decStack(Type t) {
    if(t instanceof TVoid)
      return;

    s.stackc--;
    if(t instanceof TDouble)
      s.stackc--;
  }

  /*
   * Increases stack depending on type.
   * Int and bool = 1 unit
   * Double = 2 units
   */
  void incStack(Type t) {
    if(t instanceof TVoid)
      return;

    s.stackc++;
    if(t instanceof TDouble)
      s.stackc++;
    check();
  }

  /* Sets stack limit to the highest current value */
  void check() {
    if(s.stackc > s.stacklim)
      s.stacklim = s.stackc;
  }

  /* Comment */
  public String visit(Comment c) {
    return "\n  ;; " + c.comment;
  }

  /*
   * Convert integer to double on stack
   * .i -> .dd
   */
  public String visit(I2D c) {
    s.stackc++;
    check();
    return "\n  i2d";
  }

  /*
   * Store int or double from stack to variable
   * .dd -> .
   * .i  -> .
   * */
  public String visit(Store c) {
    decStack(c.type);
    if(c.type instanceof TInt || c.type instanceof TBool)
      return "\n  istore " + c.addr;
    return "\n  dstore " + c.addr;
  }

  /*
   * Loads int or double variable onto stack
   * . -> V(i)
   */
  public String visit(Load c) {
    incStack(c.type);
    if(c.type instanceof TInt || c.type instanceof TBool)
      return "\n  iload " + c.addr;
    return "\n  dload " + c.addr;
  }

  public String visit(IConst c) {
    s.stackc++;
    check();
    return "\n  iconst_" + c.immed;
  }

  public String visit(Ldc c) {
    incStack(c.type);
    if(c.type instanceof TInt || c.type instanceof TBool)
      return "\n  ldc " + c.ival;
    return "\n  ldc2_w " + c.dval;
  }

  public String visit(Pop c) {
    decStack(c.type);
    if(c.type instanceof TInt || c.type instanceof TBool)
      return "\n  pop";
    return   "\n  pop2";
  }

  public String visit(Push c) {
    s.stackc++;
    check();
    return "\n  bipush " + c.val;
  }

  public String visit(Return c) {
    if(c.type instanceof TInt || c.type instanceof TBool)
      return "\n  ireturn";
    else if(c.type instanceof TDouble)
      return "\n  dreturn";
    else
      return "\n  return";
  }

  public String visit(Call c) {
    incStack(c.func.funcType.returnType);
    return String.format("\n  invokestatic %s", c.func.toJVM());
  }

  public String visit(Target c) {
    return String.format("\n%s:", c.label.toJVM());
  }

  public String visit(Goto c) {
    return "\n  goto " + c.label.toJVM();
  }

  /*
   * Takes two values on stack and
   * puts 1 if inequality, 0 if equal and
   * -1 otherwise
   */
  public String visit(Dcmpg c) {
    decStack(FunType.INT);
    return "\n  dcmpg";
  }

  /* ==== Compare and pop 1 int on stack ==== */
  public String visit(IfEq c) {
    decStack(FunType.INT);
    return "\n  ifeq " + c.label.toJVM();
  }

  public String visit(IfNEq c) {
    decStack(FunType.INT);
    return "\n  ifne " + c.label.toJVM();
  }
  public String visit(IfLt c) {
    decStack(FunType.INT);
    return "\n  iflt " + c.label.toJVM();
  }
  public String visit(IfGt c) {
    decStack(FunType.INT);
    return "\n  ifgt " + c.label.toJVM();
  }
  public String visit(IfLe c) {
    decStack(FunType.INT);
    return "\n  ifle " + c.label.toJVM();
  }
  public String visit(IfGe c) {
    decStack(FunType.INT);
    return "\n  ifge " + c.label.toJVM();
  }
/* === Compare pop 2 int on stack === */
  public String visit(IfCmpEq c) {
    decStack(FunType.DOUBLE);
    return "\n  if_icmpeq " + c.label.toJVM();
  }

  public String visit(IfCmpNEq c) {
    decStack(FunType.DOUBLE);
    return "\n  if_icmpne " + c.label.toJVM();
  }

  public String visit(IfCmpLt c) {
    decStack(FunType.DOUBLE);
    return "\n  if_icmplt " + c.label.toJVM();
  }

  public String visit(IfCmpLe c) {
    decStack(FunType.DOUBLE);
    return "\n  if_icmple " + c.label.toJVM();
  }

  public String visit(IfCmpGt c) {
    decStack(FunType.DOUBLE);
    return "\n  if_icmpgt " + c.label.toJVM();
  }

  public String visit(IfCmpGe c) {
    decStack(FunType.DOUBLE);
    return "\n  if_icmpge " + c.label.toJVM();
  }
  /* ==== Arithmetic operations ====*/
  public String visit(Add c) {
    decStack(c.type);
    if(c.type instanceof TInt)
      return "\n  iadd";
    return "\n  dadd";
  }

  public String visit(Sub c) {
    decStack(c.type);
    if(c.type instanceof TInt)
      return "\n  isub";
    return "\n  dsub";
  }

  public String visit(Div c) {
    decStack(c.type);
    if(c.type instanceof TInt)
      return "\n  idiv";
    return "\n  ddiv";
  }

  public String visit(Mul c) {
    decStack(c.type);
    if(c.type instanceof TInt)
      return "\n  imul";
    return "\n  dmul";
  }
}

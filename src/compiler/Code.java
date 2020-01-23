// FLISP instructions are converted into Assembly code
package compiler;

import compiler.FunType;
import C.Absyn.*;

class Stack {
  public Integer count;
  public Integer limit;
  
  public Stack(Integer count, Integer limit) {
    this.count = count;
    this.limit = limit;
  }
}

// Variable information (in context)
class CtxEntry {
  final Type type;
  final Integer addr;
  final boolean global;

  CtxEntry(Type type, Integer addr, boolean global) {
    this.type   = type;
    this.addr   = addr;
    this.global = global;
  }
}

class Func {
  public String id;
  public FunType funcType;
  public Func (String id, FunType funcType) {
    this.id = id;
    this.funcType = funcType;
  }
}

class Label {
  public int label;
  public Label (int label) {
    this.label = label;
  }
  public String toString() {
    return "L" + label;
  }
}

/* ======= CODE ======= */
/* Addressing methods that may be used:
/* Immediate - LDA #10
 * Absolute  - LDA Adr or Load Label
 * Ns        - LDA N,SP
 * Inherent  - INCA
 * */
enum AddrMethod { IMMEDIATE, ABSOLUTE, NS, INHERENT }

abstract class Code {
  public <R> R accept (CodeVisitor<R> v) {
    return null;
  }
}

class Addressable extends Code {
 public AddrMethod m;
  public int address;
  public int data;
  public int index;
  public String label;

  public Addressable(AddrMethod m, int x) {
    this.m = m;
     switch(m) {
      case IMMEDIATE:
        data = x;
        break;
      case ABSOLUTE:
        address = x;
        break;
      case NS:
        index = x;
        break;
    }
  }

  public Addressable(AddrMethod m, String id) {
    this.m = m;
    label = id;
  }

  public Addressable(AddrMethod m) {
    this.m = m;
  }

  /* Will correctly format instructions
   * and make them easier to use in visitor.
   *
   * postfix is used for instructions e.g.
   * INC Adr can be used as INCA
   * Here the letter 'A' is the postfix.
   * */
  public String toString(String ins, String postfix) {
     switch(m) {
      case INHERENT: 
        return String.format("%s%s", ins, postfix);
      case IMMEDIATE:
        return String.format("%s\t\t#%d \n", ins,  data);
      case ABSOLUTE:
        // Address can both be label and addr
        if(label.isEmpty())
          return String.format("%s\t\t%d \n", ins, address);
        else
          return String.format("%s\t\t%s \n", ins, label);
      case NS:
        return String.format("%s\t\t%d,SP\n", ins, index);
    }
    return null;
  }
}

class Add extends Addressable {
  public Add(AddrMethod m, int x) {
   super(m, x);
  }

  public Add(AddrMethod m, String s) {
    super(m, s);
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Sub extends Addressable {
  public Sub(AddrMethod m, int x) {
   super(m, x);
  }

  public Sub(AddrMethod m, String s) {
    super(m, s);
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Inc extends Addressable {
  public Inc(AddrMethod m, int x) {
    super(m, x);
  }
  public Inc(AddrMethod m, String s) {
    super(m, s);
  }
  public Inc(AddrMethod m) {
    super(m);
  } 
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Dec extends Addressable {
  public Dec(AddrMethod m, int x) {
    super(m, x);
  }
  public Dec(AddrMethod m, String s) {
    super(m, s);
  } 
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Store extends Addressable {
  public Store(AddrMethod m, int x) {
   super(m, x);
  }
  public Store(AddrMethod m, String s) {
    super(m, s);
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Load extends Addressable {
  public Load(AddrMethod m, int x) {
    super(m, x);
  }
  public Load(AddrMethod m, String s) {
    super(m, s);
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Cmp extends Addressable {
  public Cmp(AddrMethod m, int x) {
    super(m, x);
  }
  public Cmp(AddrMethod m, String s) {
    super(m, s);
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

/* ===========================*/

class Target extends Code {
  public Label label;
  public Target(Label label) {
    this.label = label;
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class VarTarget extends Code {
  public String id;
  public Code code;
  public VarTarget(String id, Code code) {
    this.id = id;
    this.code = code;
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Comment extends Code {
  public String comment;
  public Comment(String c) { comment = c; }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Push extends Code {
  public String reg;
  public Push(String reg) {
    this.reg = reg;
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Pull extends Code {
  public String reg;
  public Pull(String reg) {
    this.reg = reg;
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Return extends Code {
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Org extends Code {
  public int address;
  public Org(int address) {
    this.address = address;
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Leasp extends Code {
  public int index;
  public Leasp(int index) {
    this.index = index;
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Rmb extends Code {
  public int bytes;
  public Rmb(int bytes) {
    this.bytes = bytes;
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Test extends Code {
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

/* All branches here */
class Beq extends Code {
  public Label label;
  public Beq(Label label) {
    this.label = label;
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}
class Bge extends Code {
  public Label label;
  public Bge(Label label) {
    this.label = label;
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}
class Bgt extends Code {
  public Label label;
  public Bgt(Label label) {
    this.label = label;
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}
class Ble extends Code {
  public Label label;
  public Ble(Label label) {
    this.label = label;
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}
class Blt extends Code {
  public Label label;
  public Blt(Label label) {
    this.label = label;
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}
class Bne extends Code {
  public Label label;
  public Bne(Label label) {
    this.label = label;
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}
class Bra extends Code {
  public Label label;
  public Bra(Label label) {
    this.label = label;
  }
  public <R> R accept(CodeVisitor<R> v) {
    return v.visit(this);
  }
}

interface CodeVisitor<R> { 
  public R visit(Comment c);
  public R visit(Pull c);
  public R visit(Push c);
  public R visit(Add c);
  public R visit(Sub c);
  public R visit(Target c);
  public R visit(VarTarget c);
  public R visit(Return c);
  public R visit(Load c);
  public R visit(Store c);
  public R visit(Org c);
  public R visit(Leasp c);
  public R visit(Rmb c);
  public R visit(Test c);
  public R visit(Cmp c);
  public R visit(Inc c);
  public R visit(Dec c);
  public R visit(Beq c);
  public R visit(Bge c);
  public R visit(Bgt c);
  public R visit(Ble c);
  public R visit(Blt c);
  public R visit(Bne c);
  public R visit(Bra c);
}

class CodeToAssembler implements CodeVisitor<String> {
  public final int MAX_STACK_SIZE = 15; // Address 1F-10
  Stack s;
  public CodeToAssembler(Stack s) {
    this.s = s;
  }

  void incStack() { s.count++; checkLimit(); }
  void decStack() { s.count--; checkLimit(); }

  void checkLimit() {
    if(s.count > s.limit) {
      s.limit = s.count;
    }

    if(s.limit > MAX_STACK_SIZE) 
      throw new RuntimeException("STACK OVERFLOW.. Exiting compiler.");
  }
  
  /* ===== Comment ===== */
  public String visit(Comment c) {
    return String.format(";; %s", c.comment);
  }

  public String visit(Store c) {
    return c.toString("STA", null);
  }

  public String visit(Load c) {
    return c.toString("LDA", null);
  }

  /* ===== Integer arithmetic ===== */
  public String visit(Add c) {
    return c.toString("ADDA", null);
  }

  public String visit(Sub c) {
    return c.toString("SUBA", null);
  }

  /* ===== Stack operations ====== */
  public String visit(Push c) {
    incStack();
    return String.format("PSH%s \n", c.reg);
  }

  public String visit(Pull c) {
    decStack();
    return String.format("PUL%s \n", c.reg);
  }

  public String visit(Leasp c) {
    return String.format("LEASP\t%d,SP\n", c.index);
  }

  public String visit(Inc c) {
    return c.toString("INC", "A");
  }

  public String visit(Dec c) {
    return c.toString("DEC", "A");
  }

  public String visit(Target c) {
    return String.format("%s:\n", c.label.toString());
  }

  // Label targetting a code instruction
  public String visit(VarTarget c) {
    return String.format("%s:\t%s", c.id, c.code.accept(this));
  }

  /* Return from subroutine */
  public String visit(Return c) {
    return "RTS \n";
  }

  public String visit(Org c) {
    return String.format("ORG\t\t%d \n", c.address);
  }

  public String visit(Rmb c) {
    return String.format("RMB\t\t%d\n", c.bytes);
  }

  public String visit(Test c) {
    return "TSTA\n";
  }
  
  public String visit(Cmp c) {
    return c.toString("CMPA", null);
  }

  public String visit(Beq c) {
    return String.format("BEQ\t\t%s\n", c.label.toString());
  }

  public String visit(Bge c) {
    return String.format("BGE\t\t%s\n", c.label.toString());
  }

  public String visit(Bgt c) {
    return String.format("BGT\t\t%s\n", c.label.toString());
  } 
  
  public String visit(Ble c) {
    return String.format("BLE\t\t%s\n", c.label.toString());
  } 
  
  public String visit(Blt c) {
    return String.format("BLT\t\t%s\n", c.label.toString());
  }

  public String visit(Bne c) {
    return String.format("BNE\t\t%s\n", c.label.toString());
  }

  public String visit(Bra c) {
    return String.format("BRA\t\t%s\n", c.label.toString());
  }
}

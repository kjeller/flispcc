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
// Addressing methods that may be used
enum AddrMethod { IMMEDIATE, ABSOLUTE, NS }

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
}

class Add extends Addressable {
  public Add(AddrMethod m, int x) {
   super(m, x);
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Store extends Addressable {
  public Store(AddrMethod m, int x) {
   super(m, x);
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Load extends Addressable {
  public Load(AddrMethod m, int x) {
   super(m, x);
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Target extends Code {
  public Label label;
  public Target(Label label) {
    this.label = label;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Comment extends Code {
  public String comment;
  public Comment(String c) { comment = c; }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Push extends Code {
  public String reg;
  public Push(String reg) {
    this.reg = reg;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Pull extends Code {
  public String reg;
  public Pull(String reg) {
    this.reg = reg;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Return extends Code {
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Org extends Code {
  public int address;
  public Org(int address) {
    this.address = address;
  }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit(this);
  }
}

interface CodeVisitor<R> { 
  public R visit(Comment c);
  public R visit(Pull c);
  public R visit(Push c);
  public R visit(Add c);
  public R visit(Target c);
  public R visit(Return c);
  public R visit(Load c);
  public R visit(Store c);
  public R visit(Org c);
}

class CodeToAssembler implements CodeVisitor<String> {
  public final int MAX_STACK_SIZE = 20;
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
      throw new RuntimeException("STACK OVERFLOW.. Exciting compiler.");
  }
  
  /* ===== Comment ===== */
  public String visit(Comment c) {
    return String.format(";; %s\n", c.comment);
  }

 public String visit(Store c) {
    switch(c.m) {
      case IMMEDIATE:
        return String.format("STA #% \n", c.data);
      case ABSOLUTE:
        return String.format("STA %d \n", c.address);
      case NS:
        return String.format("STA %d, SP\n", c.index);
    }
    return null;
  }

  public String visit(Load c) {
    switch(c.m) {
      case IMMEDIATE:
        return String.format("LDA #% \n", c.data);
      case ABSOLUTE:
        return String.format("LDA %d \n", c.address);
      case NS:
        return String.format("LDA %d, SP\n", c.index);
    }
    return null;
  }

  /* ===== Integer arithmetic ===== */
  public String visit(Add c) {
    switch(c.m) {
      case IMMEDIATE:
        return String.format("ADDA #% \n", c.data);
      case ABSOLUTE:
        return String.format("ADDA %d \n", c.address);
      case NS:
        return String.format("ADDA %d, SP\n", c.index);
    }
    return null;
  }

  public String visit(Push c) {
    incStack();
    return String.format("PSH%s \n", c.reg);
  }

  public String visit(Pull c) {
    decStack();
    return String.format("PUL%s \n", c.reg);
  }

  public String visit(Target c) {
    return String.format("%s:\n", c.label.toString());
  }

  /* Return from subroutine */
  public String visit(Return c) {
    return "RTS \n";
  }

  public String visit(Org c) {
    return String.format("ORG %d \n", c.address);
  }
}

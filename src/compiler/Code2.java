// FLISP instructions are converted into Assembly code
package compiler;

import compiler.FunType;
import C.Absyn.*;

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
// Addressing methods that may be used
enum AddrMethod {
  IMMEDIATE, ABSOLUTE, NS 
}

class Code {
  public AddrMethod m;
  public int adr;
  public int data;
  public int index;

  public Code(AddrMethod m, int x) {
    this.m = m;
     switch(m) {
      case AddrMethod.IMMEDIATE:
        data = x;
        break;
      case AddrMethod.ABSOLUTE:
        adr = x;
        break;
      case AddrMethod.NS:
        index = x;
        break;
    }
  }

  public <R> R accept (CodeVisitor<R> v) {
    return null;
  }
}

class Comment extends Code {
  public String comment;
  public Comment(String c) { comment = c; }
  public <R> R accept (CodeVisitor<R> v) {
    return v.visit(this);
  }
}

class Add extends Code {
  public Add(AddrMethod m, int x) {
   super(m, x);
  }

  public <R> R accept (CodeVisitor<R> v) {
    return v.visit(this);
  }
}


interface CodeVisitor<R> { public R visit (Comment c);}
class CodeToAssembler implements CodeVisitor<String> {
  /* ===== Comment ===== */
  public String visit(Comment c) {
    return String.format(";; %s\n", c.comment);
  }

  /* ===== Integer arithmetic ===== */
  public String visit(Add c) {
    switch(m) {
      case AddrMethod.IMMEDIATE:
        return String.format("ADDA #% \n", c.data);
        break;
      case AddrMethod.ABSOLUTE:
        return String.format("ADDA %d \n", c.adr);
        break;
      case AddrMethod.NS:
        return String.format("ADDA %d, SP\n");
        break;
    }
  }
}

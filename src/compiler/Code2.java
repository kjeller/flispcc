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

interface CodeVisitor<R> {
  public R visit (Comment c);

}

class CodeToAssembler implements CodeVisitor<String> {
  /* ===== Comment ===== */
  public String visit(Comment c) {
    return String.format(";; %s\n", c.comment);
  }

  /* ===== Integer arithmetic ===== */
  public String visit(Add c) {
    return "ADDA \n";
  }
}

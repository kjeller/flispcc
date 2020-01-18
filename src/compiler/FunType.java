
package compiler;

import java.util.*;
import C.Absyn.*;

public class FunType {

  // Share type constants
  public static final Type BOOL   = new TBool();
  public static final Type INT    = new TInt();
  public static final Type VOID   = new TVoid();

  final Type returnType;
  final ListArg args;
  public FunType (Type t, ListArg l) {
    returnType = t;
    args = l;
  }
/*class TypeVisitor implements Type.Visitor<String,Void> {
  public String visit(TBool p, Void arg)    { return "Z"; }
  public String visit(TInt p, Void arg)     { return "I"; }
  public String visit(TDouble p, Void arg)  { return "D"; }
  public String visit(TVoid p, Void arg)    { return "V"; }
}*/
}

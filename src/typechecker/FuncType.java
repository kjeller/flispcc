package typechecker;

import C.*;
import C.Absyn.*;

/**
 * A function signature consists of a list
 * of arguments (types) and a return type.
 * Author: Karl Strålman, Tobias Campbell
 **/
public class FuncType {
  public final ListArg args; // Arguments
  public final Type rt;    // Return type
 
  public FuncType(Type rt, ListArg args) {
    this.args = args;
    this.rt   = rt;
  }
}

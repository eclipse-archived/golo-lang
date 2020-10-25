package gololang.ir;

import gololang.FunctionReference;
import java.util.*;
import java.util.function.BiFunction;

// TODO: tests

public final class Visitors {
  private Visitors() {
    throw new UnsupportedOperationException();
  }

  /**
   * Define how to walk the tree.
   */
  public enum Walk {
    /**
     * Walk the tree <em>before</em> dealing with the current element.
     */
    PREFIX,

    /**
     * Walk the tree <em>after</em> dealing with the current element.
     */
    POSTFIX,

    /**
     * Walk the tree twice, <em>before and after</em>dealing with the current element.
     */
    BOTH,

    /**
     * Don't walk the tree.
     * <p>
     * This allows the user to choose if and when to walk the tree.
     */
    NONE
  }

  /**
   * A special adapter class to ease the creation of IR visitors in Golo.
   * <p>
   * The visitor will apply a given function to elements in the IR tree, accumulating returned values.
   * <p>
   * Three kinds of functions are valid:<ul>
   * <li>unary functions will be called with the current element only, the returned value is stored in the accumulator;
   * <li>binary functions will be called with the current accumulator and the current element, the returned value is
   * stored in the accumulator; this is the more natural version, similar to a <em>reduce</em>;
   * <li>ternary functions will be called with the visitor itself, the accumulator, and the current element, the
   * returned value is stored in the accumulator if not {@code null}.
   * </ul>
   *
   * This last version is more complicated, but gives full access to the user on how to walk the tree and change the
   * accumulator.
   */
  public abstract static class DispatchIrVisitor implements GoloIrVisitor, BiFunction<Object, GoloElement<?>, Object> {

    private Object accumulator;
    private Walk walk;

    protected abstract FunctionReference dispatchFunction(GoloElement<?> elt);

    private void dispatch(GoloElement<?> elt) {
      if (walk == Walk.PREFIX || walk == Walk.BOTH) {
        elt.walk(this);
      }
      FunctionReference f = dispatchFunction(elt);
      if (f != null) {
        try {
          switch (f.arity()) {
            case 0:
              accumulator = f.invoke();
              break;
            case 1:
              accumulator = f.invoke(elt);
              break;
            case 2:
              accumulator = f.invoke(accumulator, elt);
              break;
            case 3:
              Object result = f.invoke(this, accumulator, elt);
              if (result != null) {
                accumulator = result;
              }
              break;
            default:
              throw new IllegalArgumentException("The dispatched function must have an arity of 0, 1, 2 or 3");
          }
        } catch (RuntimeException e) {
          throw e;
        } catch (Throwable t) {
          throw new RuntimeException(t);
        }
      }
      if (walk == Walk.POSTFIX || walk == Walk.BOTH) {
        elt.walk(this);
      }
    }

    private DispatchIrVisitor(Walk walk) {
      this.walk = walk;
    }

    /**
     * @return the value of the accumulator.
     */
    public Object accumulator() {
      return accumulator;
    }

    /**
     * Initialize the accumulator.
     */
    public DispatchIrVisitor accumulator(Object acc) {
      this.accumulator = acc;
      return this;
    }

    /**
     * Function-like use of the visitor.
     * <p>
     * Initialize the accumulator with the given value, visit the given element, and returns the final value of the
     * accumulator.
     *
     * @param acc the initial value for the accumulator
     * @param elt the IR element to visit
     * @return the final value of the accumulator
     */
    @Override
    public Object apply(Object acc, GoloElement<?> elt) {
      accumulator(acc);
      elt.accept(this);
      return accumulator();
    }

    @Override
    public void visitModule(GoloModule elt) {
      dispatch(elt);
    }

    @Override
    public void visitModuleImport(ModuleImport elt) {
      dispatch(elt);
    }

    @Override
    public void visitStruct(Struct elt) {
      dispatch(elt);
    }

    @Override
    public void visitUnion(Union elt) {
      dispatch(elt);
    }

    @Override
    public void visitUnionValue(UnionValue elt) {
      dispatch(elt);
    }

    @Override
    public void visitAugmentation(Augmentation elt) {
      dispatch(elt);
    }

    @Override
    public void visitNamedAugmentation(NamedAugmentation elt) {
      dispatch(elt);
    }

    @Override
    public void visitFunction(GoloFunction elt) {
      dispatch(elt);
    }

    @Override
    public void visitDecorator(Decorator elt) {
      dispatch(elt);
    }

    @Override
    public void visitBlock(Block elt) {
      dispatch(elt);
    }

    @Override
    public void visitConstantStatement(ConstantStatement elt) {
      dispatch(elt);
    }

    @Override
    public void visitReturnStatement(ReturnStatement elt) {
      dispatch(elt);
    }

    @Override
    public void visitFunctionInvocation(FunctionInvocation elt) {
      dispatch(elt);
    }

    @Override
    public void visitMethodInvocation(MethodInvocation elt) {
      dispatch(elt);
    }

    @Override
    public void visitAssignmentStatement(AssignmentStatement elt) {
      dispatch(elt);
    }

    @Override
    public void visitDestructuringAssignment(DestructuringAssignment elt) {
      dispatch(elt);
    }

    @Override
    public void visitReferenceLookup(ReferenceLookup elt) {
      dispatch(elt);
    }

    @Override
    public void visitConditionalBranching(ConditionalBranching elt) {
      dispatch(elt);
    }

    @Override
    public void visitBinaryOperation(BinaryOperation elt) {
      dispatch(elt);
    }

    @Override
    public void visitUnaryOperation(UnaryOperation elt) {
      dispatch(elt);
    }

    @Override
    public void visitLoopStatement(LoopStatement elt) {
      dispatch(elt);
    }

    @Override
    public void visitForEachLoopStatement(ForEachLoopStatement elt) {
      dispatch(elt);
    }

    @Override
    public void visitCaseStatement(CaseStatement elt) {
      dispatch(elt);
    }

    @Override
    public void visitMatchExpression(MatchExpression elt) {
      dispatch(elt);
    }

    @Override
    public void visitWhenClause(WhenClause<?> elt) {
      dispatch(elt);
    }

    @Override
    public void visitThrowStatement(ThrowStatement elt) {
      dispatch(elt);
    }

    @Override
    public void visitTryCatchFinally(TryCatchFinally elt) {
      dispatch(elt);
    }

    @Override
    public void visitClosureReference(ClosureReference elt) {
      dispatch(elt);
    }

    @Override
    public void visitLoopBreakFlowStatement(LoopBreakFlowStatement elt) {
      dispatch(elt);
    }

    @Override
    public void visitCollectionLiteral(CollectionLiteral elt) {
      dispatch(elt);
    }

    @Override
    public void visitCollectionComprehension(CollectionComprehension elt) {
      dispatch(elt);
    }

    @Override
    public void visitNamedArgument(NamedArgument elt) {
      dispatch(elt);
    }

    @Override
    public void visitLocalReference(LocalReference elt) {
      dispatch(elt);
    }

    @Override
    public void visitMember(Member elt) {
      dispatch(elt);
    }

    @Override
    public void visitMacroInvocation(MacroInvocation elt) {
      dispatch(elt);
    }

    @Override
    public void visitNoop(Noop elt) {
      dispatch(elt);
    }

    @Override
    public void visitToplevelElements(ToplevelElements elt) {
      dispatch(elt);
    }
  }

  /**
   * Creates a visitor from a map of functions.
   * <p>
   * This is the same as {@code visitor(functions, null, Walk.PREFIX)}.
   */
  public static GoloIrVisitor visitor(Map<Class<? extends GoloElement<?>>, FunctionReference> functions) {
    return visitor(functions, null, Walk.PREFIX);
  }


  /**
   * Creates a visitor from a map of functions.
   * <p>
   * The resulting visitor will use the function corresponding to the class of the element to visit. If the class is
   * not in the map, the default function will be used. If the function is {@code null}, no action will be taken for
   * the node, but the subtree will be walked if required, according to the {@code walk} parameter.
   * <p>
   * For instance, the function:
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * function numberOfAssignement = |irTree| -> Visitors.visitor!(
   *   map[
   *     [AssignmentStatement.class, |acc, elt| -> acc + 1],
   *     [DestructuringAssignment.class, |acc, elt| -> acc + elt: referencesCount()]
   *   ],
   *   |acc, elt| -> acc,
   *   Visitors$Walk.PREFIX())
   *   : apply(0, irTree)
   * </code></pre>
   * counts the number of assignment in an IR tree.
   *
   * @param functions a map from IR classes to the golo functions
   * @param defaultFunction the function to apply if the class of the element is not in the map.
   * @param walk when to walk the tree
   */
  public static DispatchIrVisitor visitor(Map<Class<? extends GoloElement<?>>, FunctionReference> functions, FunctionReference defaultFunction, Walk walk) {
    return new DispatchIrVisitor(walk) {
      @Override
      protected FunctionReference dispatchFunction(GoloElement<?> elt) {
        return functions.getOrDefault(elt.getClass(), defaultFunction);
      }
    };
  }

  /**
   * Creates a visitor from a unique function.
   * <p>
   * The resulting visitor will use the given function on each element. It is therefore the function responsibility to match on the element type if necessary.
   * <p>
   * For instance, the function:
   * <pre class="listing"><code class="lang-golo" data-lang="golo">
   * function numberOfAssignement = |irTree| -> Visitors.visitor!(|acc, elt| -> match {
   *     when elt oftype AssignmentStatement.class then acc + 1
   *     when elt oftype DestructuringAssignment.class then acc + elt: referencesCount()
   *     otherwise acc
   *   }, Visitors$Walk.PREFIX())
   *   : apply(0, irTree)
   * </code></pre>
   * counts the number of assignment in an IR tree.
   *
   * @param fun the golo function to apply to elements in the tree
   * @param walk when to walk the tree
   */
  public static DispatchIrVisitor visitor(FunctionReference fun, Walk walk) {
    return new DispatchIrVisitor(walk) {
      @Override
      protected FunctionReference dispatchFunction(GoloElement<?> elt) {
        return fun;
      }
    };
  }

  /**
   * Creates a visitor from a unique function.
   * <p>
   * This is the same as {@code visitor(function, Walk.PREFIX)}.
   */
  public static DispatchIrVisitor visitor(FunctionReference function) {
    return visitor(function, Walk.PREFIX);
  }


}

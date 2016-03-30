package som.primitives.reflection;

import som.interpreter.nodes.nary.TernaryExpressionNode;
import som.vmobjects.SArray;
import som.vmobjects.SSymbol;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.source.SourceSection;


@GenerateNodeFactory
public abstract class PerformWithArgumentsPrim extends TernaryExpressionNode {

  @Child protected AbstractSymbolDispatch dispatch;

  public PerformWithArgumentsPrim(final SourceSection source) {
    super(source);
    dispatch = AbstractSymbolDispatchNodeGen.create(source);
  }

  @Specialization
  public final Object doObject(final VirtualFrame frame,
      final Object receiver, final SSymbol selector, final SArray  argsArr) {
    return dispatch.executeDispatch(frame, receiver, selector, argsArr);
  }

  @Override
  public NodeCost getCost() {
    return dispatch.getCost();
  }
}

package som.interpreter.nodes.dispatch;

import som.compiler.Tags;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.InvalidAssumptionException;

import dym.Tagging;


public final class CachedDispatchNode extends AbstractDispatchNode {

  private static final String[] VIRTUAL_INVOKE = new String[] {Tags.CACHED_VIRTUAL_INVOKE};
  private static final String[] NOT_A = new String[] {Tags.LOOP_BODY, Tags.LOOP_NODE, Tags.UNSPECIFIED_INVOKE};

  @Child private DirectCallNode       cachedMethod;
  @Child private AbstractDispatchNode nextInCache;

  private final DispatchGuard         guard;

  public CachedDispatchNode(final CallTarget methodCallTarget,
      final DispatchGuard guard, final AbstractDispatchNode nextInCache) {
    super(Tagging.cloneAndUpdateTags(nextInCache.getSourceSection(), VIRTUAL_INVOKE, NOT_A));
    this.cachedMethod = Truffle.getRuntime().createDirectCallNode(methodCallTarget);
    this.guard        = guard;
    this.nextInCache  = nextInCache;
  }

  @Override
  public Object executeDispatch(final VirtualFrame frame, final Object[] arguments) {
    try {
      if (guard.entryMatches(arguments[0])) {
        return cachedMethod.call(frame, arguments);
      } else {
        return nextInCache.executeDispatch(frame, arguments);
      }
    } catch (InvalidAssumptionException e) {
      CompilerDirectives.transferToInterpreter();
      return replace(nextInCache).
          executeDispatch(frame, arguments);
    }
  }

  @Override
  public int lengthOfDispatchChain() {
    return 1 + nextInCache.lengthOfDispatchChain();
  }
}

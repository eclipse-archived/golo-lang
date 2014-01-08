package org.gololang.microbenchmarks.support;

import org.jruby.embed.ScriptingContainer;

public final class JRubyContainerAndReceiver {

  private final ScriptingContainer container;
  private final Object receiver;

  public JRubyContainerAndReceiver(ScriptingContainer container, Object receiver) {
    this.container = container;
    this.receiver = receiver;
  }

  public ScriptingContainer container() {
    return container;
  }

  public Object receiver() {
    return receiver;
  }
}

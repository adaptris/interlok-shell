package com.adaptris.crash.commands.completion;

import com.adaptris.crash.commands.actions.NamedCommandAction;
import org.crsh.cli.spi.Completer;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;
import org.junit.Test;

import javax.management.MBeanServerConnection;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class NamedCommandCompletionTest {

  @Test
  public void complete() throws Exception {
    Completer completer = new NamedCommandCompletion(TestNamedCommand.class);
    Set<String> s = completer.complete(null, "send").getValues();
    assertEquals(2, s.size());
    s = completer.complete(null, "send-").getValues();
    assertEquals(1, s.size());
    assertEquals("async", s.iterator().next());
  }

  private enum TestNamedCommand implements NamedCommandAction{

    send{
      @Override
      public String commandName() {
        return "send";
      }
    },
    sendAsync{
      @Override
      public String commandName() {
        return "send-async";
      }
    };

    @Override
    public String execute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException {
      return null;
    }

    @Override
    public String execute(MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException {
      return null;
    }
  }

}
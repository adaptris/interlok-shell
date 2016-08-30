package com.adaptris.crash.commands.actions;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AdapterCommandActionTest {

  @Test
  public void testStatusValidArguments(){
    assertFalse(AdapterCommandAction.Commands.status.validateArguments(new HashMap<String, Object>()));
    Map<String, Object> arguments = new HashMap<String, Object>();
    arguments.put("showJMXDetails", null);
    assertTrue(AdapterCommandAction.Commands.status.validateArguments(arguments));
    arguments.put("showJMXDetails", new Boolean(true));
    assertTrue(AdapterCommandAction.Commands.status.validateArguments(arguments));
    arguments.put("showJMXDetails", new Object());
    assertFalse(AdapterCommandAction.Commands.status.validateArguments(arguments));
  }

}
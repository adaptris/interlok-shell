package com.adaptris.crash.commands.actions;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ChannelCommandActionTest {

  @Test
  public void validateArguments() throws Exception {
    assertFalse(ChannelCommandAction.start.validateArguments(new HashMap<String, Object>()));
    Map<String, Object> arguments = new HashMap<String, Object>();
    arguments.put(ChannelCommandAction.CHANNEL_NAME_KEY, null);
    assertFalse(ChannelCommandAction.start.validateArguments(arguments));
    arguments.put(ChannelCommandAction.CHANNEL_NAME_KEY, "Channel");
    assertTrue(ChannelCommandAction.start.validateArguments(arguments));
    arguments.put(ChannelCommandAction.CHANNEL_NAME_KEY, new Object());
    assertFalse(ChannelCommandAction.start.validateArguments(arguments));
  }

}
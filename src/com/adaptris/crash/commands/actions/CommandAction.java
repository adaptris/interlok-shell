package com.adaptris.crash.commands.actions;

import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;

import javax.management.MBeanServerConnection;
import java.util.Map;

interface CommandAction {
  String execute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException;
  boolean validateArguments(Map<String, Object> arguments);
}

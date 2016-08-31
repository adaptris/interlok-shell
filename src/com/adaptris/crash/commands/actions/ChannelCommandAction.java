package com.adaptris.crash.commands.actions;

import com.adaptris.crash.commands.InterlokCommandUtils;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;

import javax.management.MBeanServerConnection;
import java.util.Map;

public enum ChannelCommandAction implements CommandAction {

  start{
    @Override
    public String doExecute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException {
      try {
        if (!InterlokCommandUtils.isStarted(InterlokCommandUtils.getAdapter(connection))) {
          throw new ScriptException("Can't start any channels while the adapter is stopped");
        }
        String channelName = channelName(arguments);
        InterlokCommandUtils.getChannel(connection, channelName).requestStart(TIMEOUT);
        return "Channel (" + channelName + ") started\n";
      } catch (Exception e) {
        throw new ScriptException("Could not start the channel: " + e.getMessage());
      }
    }
  },
  stop{
    @Override
    public String doExecute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException {
      try {
        String channelName = channelName(arguments);
        InterlokCommandUtils.getChannel(connection, channelName).requestClose(TIMEOUT);
        return "Channel (" + channelName + ") stopped/closed\n";
      } catch (Exception e) {
        throw new ScriptException("Could not stop the channel: " + e.getMessage());
      }
    }
  },
  restart{
    @Override
    public String doExecute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException {
      try {
        String channelName = channelName(arguments);
        InterlokCommandUtils.getChannel(connection, channelName).requestRestart(TIMEOUT);
        return "Channel (" + channelName + ") restarted\n";
      } catch (Exception e) {
        throw new ScriptException("Could not restart the channel: " + e.getMessage());
      }
    }
  };

  private static final long TIMEOUT = 60000L;
  public static final String CHANNEL_NAME_KEY = "channelName";

  @Override
  public final String execute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException{
    if(!validateArguments(arguments)){
      throw new ScriptException(argumentWarning());
    }
    return doExecute(context, connection, arguments);
  }

  public boolean validateArguments(Map<String, Object> arguments) {
    return arguments.containsKey(CHANNEL_NAME_KEY) && arguments.get(CHANNEL_NAME_KEY) != null && arguments.get(CHANNEL_NAME_KEY) instanceof String;
  }

  public abstract String doExecute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException;

  public String argumentWarning(){
    return CHANNEL_NAME_KEY + " argument is not set.";
  }

  public String channelName(Map<String, Object> arguments){
    return (String)arguments.get(CHANNEL_NAME_KEY);
  }
}

package com.adaptris.crash.commands.actions;

import com.adaptris.crash.commands.InterlokCommandUtils;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;

import javax.management.MBeanServerConnection;
import java.util.Map;

public enum WorkflowCommandAction implements CommandAction {
  start{
    @Override
    public String doExecute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException {
      try {
        if (!InterlokCommandUtils.isStarted(InterlokCommandUtils.getAdapter(connection))) {
          throw new ScriptException("Can't start any workflow while the adapter is stopped");
        }
        String channelName = channelName(arguments);
        if (!InterlokCommandUtils.isStarted(InterlokCommandUtils.getChannel(connection, channelName))) {
          throw new ScriptException("Can't start any workflow while the adapter is stopped");
        }
        String workflowName = workflowName(arguments);
        InterlokCommandUtils.getWorkflow(connection, channelName, workflowName).requestStart(TIMEOUT);
        return "Workflow (" + workflowName + ") started\n";
      } catch (Exception e) {
        throw new ScriptException("Could not start the workflow: " + e.getMessage(), e);
      }
    }
  },
  stop{
    @Override
    public String doExecute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException {
      try {
        String channelName = channelName(arguments);
        String workflowName = workflowName(arguments);
        InterlokCommandUtils.getWorkflow(connection, channelName, workflowName).requestStop(TIMEOUT);
        return "Workflow (" + workflowName + ") stopped\n";
      } catch (Exception e) {
        throw new ScriptException("Could not stop the workflow: " + e.getMessage(), e);
      }
    }
  },
  restart{
    @Override
    public String doExecute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException {
      try {
        if (!InterlokCommandUtils.isStarted(InterlokCommandUtils.getAdapter(connection))) {
          throw new ScriptException("Can't start any workflow while the adapter is stopped");
        }
        String channelName = channelName(arguments);
        if (!InterlokCommandUtils.isStarted(InterlokCommandUtils.getChannel(connection, channelName))) {
          throw new ScriptException("Can't start any workflow while the adapter is stopped");
        }
        String workflowName = workflowName(arguments);
        InterlokCommandUtils.getWorkflow(connection, channelName, workflowName).requestRestart(TIMEOUT);
        return "Workflow (" + workflowName + ") restarted\n";
      } catch (Exception e) {
        throw new ScriptException("Could not restart the workflow: " + e.getMessage(), e);
      }
    }
  };

  private static final long TIMEOUT = 60000L;
  public static final String CHANNEL_NAME_KEY = "channelName";
  public static final String WORKFLOW_NAME_KEY = "workflowName";

  @Override
  public final String execute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException{
    if(!validateArguments(arguments)){
      throw new ScriptException(argumentWarning());
    }
    return doExecute(context, connection, arguments);
  }

  public boolean validateArguments(Map<String, Object> arguments) {
    return arguments.containsKey(WORKFLOW_NAME_KEY) && arguments.get(WORKFLOW_NAME_KEY) != null && arguments.get(WORKFLOW_NAME_KEY) instanceof String
        && arguments.containsKey(CHANNEL_NAME_KEY) && arguments.get(CHANNEL_NAME_KEY) != null && arguments.get(CHANNEL_NAME_KEY) instanceof String;
  }

  public abstract String doExecute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException;

  public String argumentWarning(){
    return WORKFLOW_NAME_KEY + " argument is not set.";
  }

  public String workflowName(Map<String, Object> arguments){
    return (String)arguments.get(WORKFLOW_NAME_KEY);
  }

  public String channelName(Map<String, Object> arguments){
    return (String)arguments.get(CHANNEL_NAME_KEY);
  }
}

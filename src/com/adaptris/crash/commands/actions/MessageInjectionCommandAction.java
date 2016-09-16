package com.adaptris.crash.commands.actions;

import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.crash.commands.InterlokCommandUtils;
import com.adaptris.util.GuidGenerator;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;

import javax.management.MBeanServerConnection;
import java.util.Map;

public enum MessageInjectionCommandAction implements CommandAction {

  send {
    @Override
    String doExecute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException {
      try {
        if (!InterlokCommandUtils.isStarted(InterlokCommandUtils.getAdapter(connection))) {
          throw new ScriptException("Can't send a message to a workflow while the adapter is stopped");
        }
        String channelName = channelName(arguments);
        if (!InterlokCommandUtils.isStarted(InterlokCommandUtils.getChannel(connection, channelName))) {
          throw new ScriptException("Can't send a message to a workflow while the channel is stopped");
        }
        String workflowName = workflowName(arguments);

        InterlokCommandUtils.getWorkflow(connection, channelName, workflowName).process(createMessage());
        return "Message Injected into Workflow (" + workflowName + ")\n";
      } catch (Exception e) {
        throw new ScriptException("Could not inject message into the workflow: " + e.getMessage(), e);
      }
    }
  };

  public static final String CHANNEL_NAME_KEY = "channelName";
  public static final String WORKFLOW_NAME_KEY = "workflowName";

  @Override
  public String execute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException {
    if(!validateArguments(arguments)){
      throw new ScriptException(argumentWarning());
    }
    return doExecute(context, connection, arguments);
  }

  @Override
  public String execute(MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException {
    if(!validateArguments(arguments)){
      throw new ScriptException(argumentWarning());
    }
    return doExecute(null, connection, arguments);
  }

  boolean validateArguments(Map<String, Object> arguments) {
    return arguments.containsKey(WORKFLOW_NAME_KEY) && arguments.get(WORKFLOW_NAME_KEY) != null && arguments.get(WORKFLOW_NAME_KEY) instanceof String
        && arguments.containsKey(CHANNEL_NAME_KEY) && arguments.get(CHANNEL_NAME_KEY) != null && arguments.get(CHANNEL_NAME_KEY) instanceof String;
  }

  abstract String doExecute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException;

  String argumentWarning(){
    return "";
  }

  String workflowName(Map<String, Object> arguments){
    return (String)arguments.get(WORKFLOW_NAME_KEY);
  }

  String channelName(Map<String, Object> arguments){
    return (String)arguments.get(CHANNEL_NAME_KEY);
  }


  SerializableAdaptrisMessage createMessage(){
    SerializableAdaptrisMessage msg = new SerializableAdaptrisMessage();
    GuidGenerator guid = new GuidGenerator();
    msg.setUniqueId(guid.getUUID());
    msg.setContent("hello world");
    msg.setContentEncoding("UTF-8");
    msg.addMetadata("my-key", "myvalue");
    return msg;
  }
}

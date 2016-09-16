package com.adaptris.crash.commands.actions;

import com.adaptris.core.SerializableAdaptrisMessage;
import com.adaptris.crash.commands.InterlokCommandUtils;
import com.adaptris.interlok.types.SerializableMessage;
import com.adaptris.util.GuidGenerator;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;

import javax.management.MBeanServerConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public enum MessageInjectionCommandAction implements NamedCommandAction {

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

        SerializableMessage message = InterlokCommandUtils.getWorkflow(connection, channelName, workflowName).process(createMessage(headers(arguments), payload(arguments), contentEncoding(arguments)));
        StringBuilder sb = new StringBuilder();
        sb.append("Message Injected into Workflow (");
        sb.append(workflowName);
        sb.append(")\n");
        sb.append("\n");
        sb.append("Headers:\n");
        sb.append("\n");
        for(Map.Entry<String,String> headers : message.getMessageHeaders().entrySet()){
          sb.append(headers.getKey());
          sb.append(" : ");
          sb.append(headers.getValue());
          sb.append("\n");
        }
        sb.append("\n");
        sb.append("Payload:\n");
        sb.append("\n");
        sb.append(message.getContent());
        return sb.toString();
      } catch (Exception e) {
        throw new ScriptException("Could not inject message into the workflow: " + e.getMessage(), e);
      }
    }
  },

  sendAsync{
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

        InterlokCommandUtils.getWorkflow(connection, channelName, workflowName).processAsync(createMessage(headers(arguments), payload(arguments), contentEncoding(arguments)));
        return "Message Injected into Workflow (" + workflowName + ")\n";
      } catch (Exception e) {
        throw new ScriptException("Could not inject message into the workflow: " + e.getMessage(), e);
      }
    }

    public String commandName(){
      return "send-async";
    }
  };

  public static final String CHANNEL_NAME_KEY = "channelName";
  public static final String WORKFLOW_NAME_KEY = "workflowName";
  public static final String HEADERS_KEY = "headers";
  public static final String PAYLOAD_KEY = "payload";
  public static final String CONTENT_ENCODING_KEY = "contentType";

  private static Map<String, MessageInjectionCommandAction> map = new HashMap<String, MessageInjectionCommandAction>();

  static {
    for (MessageInjectionCommandAction e : MessageInjectionCommandAction.values()) {
      map.put(e.commandName(), e);
    }
  }

  public static MessageInjectionCommandAction valueOfFromCommandName(String commandName) {
    return map.get(commandName);
  }

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
    return WORKFLOW_NAME_KEY + " and/or " + CHANNEL_NAME_KEY + " arguments are not set.";
  }

  String workflowName(Map<String, Object> arguments){
    return (String)arguments.get(WORKFLOW_NAME_KEY);
  }

  String channelName(Map<String, Object> arguments){
    return (String)arguments.get(CHANNEL_NAME_KEY);
  }

  Properties headers(Map<String, Object> arguments){
    return (Properties)arguments.get(HEADERS_KEY);
  }

  String payload(Map<String, Object> arguments){
    return (String)arguments.get(PAYLOAD_KEY);
  }

  String contentEncoding(Map<String, Object> arguments){
    return (String)arguments.get(CONTENT_ENCODING_KEY);
  }

  public String commandName(){
    return name();
  }

  SerializableAdaptrisMessage createMessage(Properties headers, String payload, String contentEncoding){
    SerializableAdaptrisMessage msg = new SerializableAdaptrisMessage();
    GuidGenerator guid = new GuidGenerator();
    msg.setUniqueId(guid.getUUID());
    if(headers != null){
      Enumeration e = headers.propertyNames();
      while (e.hasMoreElements()) {
        String key = (String) e.nextElement();
        msg.addMetadata(key, headers.getProperty(key));
      }
    }
    if(payload != null) {
      msg.setContent(payload);
    }
    if(contentEncoding != null){
      msg.setContentEncoding(contentEncoding);
    }
    return msg;
  }
}

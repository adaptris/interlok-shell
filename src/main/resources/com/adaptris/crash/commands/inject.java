package com.adaptris.crash.commands;

import com.adaptris.crash.commands.actions.MessageInjectionCommandAction;
import org.crsh.cli.*;
import org.crsh.command.BaseCommand;
import org.crsh.command.Pipe;

import javax.management.MBeanServerConnection;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Usage("Interlok Workflow Message Injection")
public class inject extends BaseCommand {

  private static final String PAYLOAD_NOTE = "NOTE: Payload options are read in the following order --payload-file, --payload-random, --payload-string, if one is found the others will be ignored.";


  @Retention(RetentionPolicy.RUNTIME)
  @Option(names = {"c", "channel"})
  @Usage("channel name")
  @Required
  private @interface ChannelOption{
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Option(names = {"w", "workflow"})
  @Usage("workflow name")
  @Required
  private @interface WorkflowOption{
  }


  @Retention(RetentionPolicy.RUNTIME)
  @Option(names = {"p", "payload-string"})
  @Usage("message payload\n" +
      PAYLOAD_NOTE)
  private @interface PayloadOption{
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Option(names = {"f", "payload-file"})
  @Usage("file to be used for message payload\n" +
      PAYLOAD_NOTE)
  private @interface PayloadFileOption{
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Option(names = {"r", "payload-random"})
  @Usage("generated payload with random bytes\n" +
      PAYLOAD_NOTE)
  private @interface PayloadRandomOption{
  }


  @Retention(RetentionPolicy.RUNTIME)
  @Option(names = {"h", "headers"})
  @Usage("message headers (\"key1=value1;key2=value2\")")
  private @interface HeadersOption{
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Option(names = {"e", "content-encoding"})
  @Usage("message content encoding")
  private @interface ContentEncodingOption{
  }

  @Usage("Interlok Workflow Message Injection")
  @Man("Send an asynchronous message to Interlok workflow:\n" +
      "% local connection | inject --channel <channel name> --workflow <workflow name> send\n" +
      "...\n"
  )
  @Command
  public Pipe<MBeanServerConnection, Object> send(@WorkflowOption String workflowName, @ChannelOption String channelName
      , @PayloadOption String payload, @HeadersOption Properties headers, @ContentEncodingOption String contentEncoding
      , @PayloadFileOption File payloadFile, @PayloadRandomOption Integer payloadRandomBytes) throws Exception {
    return new Pipe<MBeanServerConnection, Object>() {
      public void provide(MBeanServerConnection connection) throws Exception {
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put(MessageInjectionCommandAction.CHANNEL_NAME_KEY, channelName);
        arguments.put(MessageInjectionCommandAction.WORKFLOW_NAME_KEY, workflowName);
        arguments.put(MessageInjectionCommandAction.PAYLOAD_KEY, payload);
        arguments.put(MessageInjectionCommandAction.PAYLOAD_FILE_KEY, payloadFile);
        arguments.put(MessageInjectionCommandAction.PAYLOAD_RANDOM_KEY, payloadRandomBytes);
        arguments.put(MessageInjectionCommandAction.CONTENT_ENCODING_KEY, contentEncoding);
        arguments.put(MessageInjectionCommandAction.HEADERS_KEY, headers);
        context.provide(MessageInjectionCommandAction.send.execute(connection, arguments));
      }
    };
  }

  @Usage("Interlok Workflow Asynchronous Message Injection")
  @Man("Send an asynchronous message to Interlok workflow:\n" +
      "% local connection | inject --channel <channel name> --workflow <workflow name> send-async\n" +
      "...\n"
  )
  @Named("send-async")
  @Command
  public Pipe<MBeanServerConnection, Object> sendAsync(@WorkflowOption String workflowName, @ChannelOption String channelName
      , @PayloadOption String payload, @HeadersOption Properties headers, @ContentEncodingOption String contentEncoding
      , @PayloadFileOption File payloadFile, @PayloadRandomOption Integer payloadRandomBytes) throws Exception {
    return new Pipe<MBeanServerConnection, Object>() {
      public void provide(MBeanServerConnection connection) throws Exception {
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put(MessageInjectionCommandAction.CHANNEL_NAME_KEY, channelName);
        arguments.put(MessageInjectionCommandAction.WORKFLOW_NAME_KEY, workflowName);
        arguments.put(MessageInjectionCommandAction.PAYLOAD_KEY, payload);
        arguments.put(MessageInjectionCommandAction.PAYLOAD_FILE_KEY, payloadFile);
        arguments.put(MessageInjectionCommandAction.PAYLOAD_RANDOM_KEY, payloadRandomBytes);
        arguments.put(MessageInjectionCommandAction.CONTENT_ENCODING_KEY, contentEncoding);
        arguments.put(MessageInjectionCommandAction.HEADERS_KEY, headers);
        context.provide(MessageInjectionCommandAction.sendAsync.execute(connection, arguments));
      }
    };
  }
}

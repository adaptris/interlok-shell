package com.adaptris.crash.commands;

import java.lang.management.ManagementFactory;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Man;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.crsh.text.Color;
import org.crsh.text.RenderPrintWriter;

import com.adaptris.core.ComponentState;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.StartedState;

@Usage("Interlok Channel Management")
@Man("The channel commands allowing you to control Interlok channels (listing, starting, stopping etc).")
public class channel extends BaseCommand {

  @Usage("Stop an Interlok Channel")
  @Man(
      "Stops a running Interlok channel:\n" +
      "% channel stop <channel name>\n" +
      "...\n"
  )
  @Command
  public String stop(
      @Usage("The channel name to stop.")
      @Argument
      String channelName) throws Exception {

    ObjectName channelObject = null;
    try {
      channelObject = getChannelObject(channelName);
    } catch (Exception ex) {
      return "Could not find the channel: " + ex.getMessage();
    }
    
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    if(!server.isRegistered(channelObject))
      return "Channel not found.";
    else {
      try {
        server.invoke(channelObject, "requestStop", new Object[0], new String[0]);
        return "Channel (" + channelName + ") stopped.";
      } catch (Exception ex) {
        return "Could not stop the channel: " + ex.getMessage();
      }
    }
  }
  
  @Usage("Start an Interlok Channel")
  @Man(
      "Starts a stopped Interlok channel:\n" +
      "% channel start <channel name>\n" +
      "...\n"
  )
  @Command
  public String start(
      @Usage("The channel name to start.")
      @Argument
      String channelName) throws Exception {

    ObjectName channelObject = null;
    try {
      channelObject = getChannelObject(channelName);
    } catch (Exception ex) {
      return "Could not find the channel: " + ex.getMessage();
    }
    
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    if(!server.isRegistered(channelObject))
      return "Channel not found.";
    else {
      try {
        server.invoke(channelObject, "requestStart", new Object[0], new String[0]);
        return "Channel (" + channelName + ") started.";
      } catch (Exception ex) {
        return "Could not start the channel: " + ex.getMessage();
      }
    }
  }
  
  @Usage("channel listjmx")
  @Man(
      "Lists all available Interlok Channels MBean info:\n" +
      "% channel listjmx\n" +
      "...\n"
  )
  @Command
  public void listjmx(InvocationContext<ObjectName> context) throws Exception {
    Set<ObjectInstance> results = queryJmx("com.adaptris:type=Channel,*");
    for (ObjectInstance instance : results) {
      context.provide(instance.getObjectName());
    }
  }
  
  @Usage("channel list")
  @Man(
      "Lists all available Interlok Channels MBean info:\n" +
      "% channel listjmx\n" +
      "...\n"
  )
  @Command
  public void list(InvocationContext<ObjectName> context) throws Exception {
    Set<ObjectInstance> results = queryJmx("com.adaptris:type=Channel,*");
    
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    
    System.out.println("here");
    RenderPrintWriter writer = context.getWriter();
    for (ObjectInstance instance : results) {
      try {
        ComponentState state = (ComponentState) server.getAttribute(instance.getObjectName(), "ComponentState");
        if(state instanceof StartedState)
          writer.print(instance.getObjectName(), Color.green);
        else if(state instanceof InitialisedState)
          writer.print(instance.getObjectName(), Color.yellow);
        else
          writer.print(instance.getObjectName(), Color.red);
        
        writer.print("\n");
      } catch (Exception ex) {
        writer.print(ex.getMessage(), Color.red);
      }
    }
  }

  private ObjectName getChannelObject(String channelName) throws Exception, MalformedObjectNameException {
    ObjectInstance jmxInterlok = getJmxInterlok();
    String adapterId = jmxInterlok.getObjectName().getKeyProperty("id");
        
    String channelString = "com.adaptris:type=Channel,adapter=" + adapterId + ",id=" + channelName;
    
    ObjectName channelObject = ObjectName.getInstance(channelString);
    return channelObject;
  }
  
  private Set<ObjectInstance> queryJmx(
      @Usage("the object name pattern for the query")
      @Argument
      String pattern) throws Exception {

    //
    ObjectName patternName = pattern != null ? ObjectName.getInstance(pattern) : null;
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    return server.queryMBeans(patternName, null);
  }

  private ObjectInstance getJmxInterlok() throws Exception {
    String interlokBaseObject = "com.adaptris:type=Adapter,id=*";
    ObjectName patternName = ObjectName.getInstance(interlokBaseObject);
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    Set<ObjectInstance> instances = server.queryMBeans(patternName, null);
    
    if(instances.size() == 0)
      return null;
    else
      return instances.iterator().next();
  }
  
}

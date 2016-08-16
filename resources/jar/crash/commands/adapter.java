package com.adaptris.crash.commands;

import java.lang.management.ManagementFactory;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.JMX;
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
import com.adaptris.core.StoppedState;
import com.adaptris.core.ClosedState;
import com.adaptris.core.runtime.*;
import com.adaptris.core.util.JmxHelper;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;

@Usage("Interlok Adapter Management")
@Man("The channel commands allowing you to control the Interlok adapter instance.")
public class adapter extends AdapterBaseCommand {

  @Usage("Stop The Adapter")
  @Man("Stops a running Interlok:\n" +
       "% adapter stop\n" + 
       "...\n")
  @Command
  public String stop() throws Exception {
    try {
      getAdapter().requestClose();
      return "Adapter (" + getAdapterName() + ") stopped/closed";
    } catch (Exception e) {
      return "Could not stop the adapter: " + e.getMessage();
    }
  }

  @Usage("Start the adapter")
  @Man("Starts a stopped adapter:\n" + 
       "% adapter start\n" + 
       "...\n")
  @Command
  public String start() throws Exception {

    try {
      getAdapter().requestStart();
      return "Adapter (" + getAdapterName() + ") started";
    } catch (Exception e) {
      return "Could not start the adapter: " + e.getMessage();
    }
  }
  
  @Usage("Check the status of the adapter")
  @Man("Checks the status:\n" + 
       "% adapter status\n" + 
       "...\n")
  @Command
  public void status(InvocationContext<Object> context) throws Exception {
    try {
      logStatus(context, getAdapter());
    } catch (Exception ex) {
      context.provide(new LabelElement(ex.getMessage()).style(Style.style(Color.red)));
    }
  }

  @Usage("Restart the adapter")
  @Man("Restart the adapter:\n" +
      "% adapter restart\n" +
      "...\n")
  @Command
  public String restart() throws Exception {
    try {
      getAdapter().requestRestart();
      return "Adapter (" + getAdapterName() + ") restarted";
    } catch (Exception e) {
      return "Could not start the adapter: " + e.getMessage();
    }
  }
  
}

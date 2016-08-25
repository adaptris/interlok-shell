package com.adaptris.crash.commands;

import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.AdapterRegistryMBean;
import com.adaptris.crash.commands.parameters.ShowJMXDetailsOptions;
import org.crsh.cli.Command;
import org.crsh.cli.Man;
import org.crsh.cli.Usage;
import org.crsh.command.InvocationContext;
import org.crsh.command.Pipe;
import org.crsh.text.Color;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.TableElement;

import javax.management.MBeanServerConnection;

@Usage("Interlok Adapter Management")
@Man("The channel commands allowing you to control the Interlok adapter instance.")
public class adapter extends AdapterBaseCommand {

  private static final long TIMEOUT = 60000L;

  @Usage("Stop The Adapter")
  @Man("Stops a running Interlok:\n" +
       "% local connection | adapter stop\n" + 
       "...\n")
  @Command
  public Pipe<MBeanServerConnection, String> stop() throws Exception {
    return new Pipe<MBeanServerConnection, String>(){
      public void provide(MBeanServerConnection connection) throws Exception {
        try {
          AdapterManagerMBean adapter = getAdapter(connection);
          adapter.requestClose(TIMEOUT);
          context.provide("Adapter (" + adapter.getUniqueId() + ") stopped/closed");
        } catch (Exception e) {
          context.provide("Could not stop the adapter: " + e.getMessage());
        }
      }
    };

  }

  @Usage("Start the adapter")
  @Man("Starts a stopped adapter:\n" + 
       "% local connection | adapter start\n" + 
       "...\n")
  @Command
  public Pipe<MBeanServerConnection, String> start() throws Exception {
    return new Pipe<MBeanServerConnection, String>(){
      public void provide(MBeanServerConnection connection) throws Exception {
        try {
          AdapterManagerMBean adapter = getAdapter(connection);
          adapter.requestStart(TIMEOUT);
          context.provide("Adapter (" + adapter.getUniqueId() + ") started");
        } catch (Exception e) {
          context.provide("Could not start the adapter: " + e.getMessage());
        }
      }
    };
  }

  @Usage("List adapters")
  @Man("Lists all available Interlok Adapter MBean info:\n" +
       "% local connection | adapter list\n" +
       "...")
  @Command
  public Pipe<MBeanServerConnection, Object> list(@ShowJMXDetailsOptions final Boolean showJmxDetails) throws Exception {
    return new Pipe<MBeanServerConnection, Object>(){
      public void provide(MBeanServerConnection connection) throws Exception {
        try {
          TableElement table = new TableElement().rightCellPadding(1);
          table.add(listRow(getAdapter(connection), showJmxDetails));
          context.provide(table);
        } catch (Exception ex) {
          context.provide(new LabelElement(ex.getMessage()).style(Style.style(Color.red)));
        }
      }
    };

  }

  @Usage("Restart the adapter")
  @Man("Restart the adapter:\n" +
      "% local connection | adapter restart\n" +
      "...\n")
  @Command
  public Pipe<MBeanServerConnection, String> restart() throws Exception {
    return new Pipe<MBeanServerConnection, String>(){
      public void provide(MBeanServerConnection connection) throws Exception {
        try {
          AdapterManagerMBean adapter = getAdapter(connection);
          adapter.requestRestart(TIMEOUT);
          context.provide("Adapter (" + adapter.getUniqueId() + ") restarted");
        } catch (Exception e) {
          context.provide("Could not start the adapter: " + e.getMessage());
        }
      }
    };
  }
  
  @Usage("Reload Configuration")
  @Man("Reload the adapter from configuration (does not start)\n" + 
       "% local connection | adapter reload\n" + 
       "...\n")
  @Command
  public Pipe<MBeanServerConnection, MBeanServerConnection> reload() throws Exception {
    return new Pipe<MBeanServerConnection, MBeanServerConnection>(){
      public void provide(MBeanServerConnection connection) throws Exception {
        try {
          AdapterRegistryMBean registry = getRegistry(connection);
          registry.reloadFromConfig();
          AdapterManagerMBean adapter = getAdapter(connection);
          out.println("Adapter (" + adapter.getUniqueId() + ") reloaded.");         
          context.provide(connection);
        } catch (Exception e) {
          out.println("Could not reload the adapter: " + e.getMessage());
        }
      }
    };
  }
  
  @Usage("Reload Configuration from VCS (if available)")
  @Man("Reload the adapter from configuration after a VCS update\n" + 
       "% local connection | adapter reloadVCS\n" + 
       "...\n")
  @Command
  public Pipe<MBeanServerConnection, MBeanServerConnection> reloadVCS() throws Exception {
    return new Pipe<MBeanServerConnection, MBeanServerConnection>(){
      public void provide(MBeanServerConnection connection) throws Exception {
        try {
          AdapterRegistryMBean registry = getRegistry(connection);
          if (registry.getVersionControl() != null) {
            registry.reloadFromVersionControl();
            AdapterManagerMBean adapter = getAdapter(connection);
            out.println("Adapter (" + adapter.getUniqueId() + ") reloaded.");
          } else {
            out.println("No Version Control enabled");          
          }
          context.provide(connection);
        } catch (Exception e) {
          out.println("Could not reload the adapter: " + e.getMessage());
        }
      }
    };
  }  
}

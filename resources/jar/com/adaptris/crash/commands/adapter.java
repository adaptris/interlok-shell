package com.adaptris.crash.commands;

import com.adaptris.crash.commands.actions.AdapterCommandAction;
import com.adaptris.crash.commands.parameters.ShowJMXDetailsOptions;
import org.crsh.cli.Command;
import org.crsh.cli.Man;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.Pipe;
import org.crsh.text.Color;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.TableElement;

import javax.management.MBeanServerConnection;
import java.util.HashMap;

@Usage("Interlok Adapter Management")
@Man("The adapter commands allowing you to control the Interlok adapter instance.")
public class adapter extends BaseCommand {

  private static final long TIMEOUT = 60000L;

  @Usage("Stop The Adapter")
  @Man("Stops a running Interlok:\n" +
       "% local connection | adapter stop\n" + 
       "...\n")
  @Command
  public Pipe<MBeanServerConnection, String> stop() throws Exception {
    return new Pipe<MBeanServerConnection, String>(){
      public void provide(MBeanServerConnection connection) throws Exception {
        context.provide(AdapterCommandAction.stop.execute(connection, new HashMap<String, Object>()));
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
        context.provide(AdapterCommandAction.start.execute(connection, new HashMap<String, Object>()));
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
          table.add(InterlokCommandUtils.statusRow(InterlokCommandUtils.getAdapter(connection), showJmxDetails));
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
        context.provide(AdapterCommandAction.restart.execute(connection, new HashMap<String, Object>()));
      }
    };
  }
  
  @Usage("Reload Configuration")
  @Man("Reload the adapter from configuration\n" + 
       "% local connection | adapter reload | adapter start\n" + 
       "...\n")
  @Command
  public Pipe<MBeanServerConnection, MBeanServerConnection> reload() throws Exception {
    return new Pipe<MBeanServerConnection, MBeanServerConnection>(){
      public void provide(MBeanServerConnection connection) throws Exception {
        out.println(AdapterCommandAction.reload.execute(connection, new HashMap<String, Object>()));
        context.provide(connection);
      }
    };
  }
  
  @Usage("Reload Configuration from VCS")
  @Man("Reload the adapter from configuration after a VCS update\n" + 
       "% local connection | adapter reloadVCS\n" + 
       "...\n")
  @Command
  public Pipe<MBeanServerConnection, MBeanServerConnection> reloadVCS() throws Exception {
    return new Pipe<MBeanServerConnection, MBeanServerConnection>(){
      public void provide(MBeanServerConnection connection) throws Exception {
        out.println(AdapterCommandAction.reloadVCS.execute(connection, new HashMap<String, Object>()));
        context.provide(connection);
      }
    };
  }  
}

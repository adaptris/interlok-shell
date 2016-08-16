package com.adaptris.crash.commands;

import org.crsh.cli.Command;
import org.crsh.cli.Man;
import org.crsh.cli.Option;
import org.crsh.cli.Usage;
import org.crsh.command.InvocationContext;
import org.crsh.text.Color;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.TableElement;

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

  @Usage("List adapters")
  @Man("Lists all available Interlok Adapter MBean info:\n" +
       "% adapter list\n" +
       "...")
  @Command
  public void list(InvocationContext<Object> context,
                   @Usage("show jmx details")
                   @Man("Supplement results with JMX object names")
                   @Option(names = {"j","show-jmx-details"})
                   final Boolean showJmxDetails) throws Exception {
    try {
      TableElement table = new TableElement().rightCellPadding(1);
      table.add(listRow(getAdapter(), showJmxDetails));
      context.provide(table);
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

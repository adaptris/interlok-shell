package com.adaptris.crash.commands;

import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.ChannelManagerMBean;
import com.adaptris.core.runtime.WorkflowManagerMBean;
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
import java.util.Collection;

@Usage("Interlok Workflow Management")
@Man("The workflow commands allowing you to control Interlok worflows (listing, starting, stopping etc).")
public class workflow extends AdapterBaseCommand {

  @Usage("List workflows")
  @Man("Lists all available Interlok Workflow MBean info:\n" +
      "% workflow list\n" +
      "...")
  @Command
  public Pipe<MBeanServerConnection, Object> list(@ShowJMXDetailsOptions final Boolean showJmxDetails) throws Exception {
    return new Pipe<MBeanServerConnection, Object>() {
      public void provide(MBeanServerConnection connection) throws Exception {
        try {
          TableElement table = new TableElement().rightCellPadding(1);
          AdapterManagerMBean adapter = getAdapter(connection);
          table.add(listRow(adapter, showJmxDetails));
          Collection<ChannelManagerMBean> channels = getAllChannels(connection, adapter);
          for (ChannelManagerMBean channel : channels) {
            table.add(listRow(channel, showJmxDetails));
            Collection<WorkflowManagerMBean> workflows = getAllWorkflows(connection, channel);
            for (WorkflowManagerMBean workflow : workflows) {
              table.add(listRow(workflow, showJmxDetails));
            }
          }
          context.provide(table);
        } catch (Exception ex) {
          context.provide(new LabelElement(ex.getMessage()).style(Style.style(Color.red)));
        }
      }
    };
  }

}
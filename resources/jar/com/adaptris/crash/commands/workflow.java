package com.adaptris.crash.commands;

import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.ChannelManagerMBean;
import com.adaptris.core.runtime.WorkflowManagerMBean;
import com.adaptris.crash.commands.parameters.ShowJMXDetailsOptions;
import org.crsh.cli.Command;
import org.crsh.cli.Man;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
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
public class workflow extends BaseCommand {

  @Usage("List workflows")
  @Man("Lists all available Interlok Workflow MBean info:\n" +
      "% local connection | workflow list\n" +
      "...")
  @Command
  public Pipe<MBeanServerConnection, Object> list(@ShowJMXDetailsOptions final Boolean showJmxDetails) throws Exception {
    return new Pipe<MBeanServerConnection, Object>() {
      public void provide(MBeanServerConnection connection) throws Exception {
        try {
          TableElement table = new TableElement().rightCellPadding(1);
          AdapterManagerMBean adapter = InterlokCommandUtils.getAdapter(connection);
          table.add(InterlokCommandUtils.statusRow(adapter, showJmxDetails));
          Collection<ChannelManagerMBean> channels = InterlokCommandUtils.getAllChannels(connection, adapter);
          for (ChannelManagerMBean channel : channels) {
            table.add(InterlokCommandUtils.statusRow(channel, showJmxDetails));
            Collection<WorkflowManagerMBean> workflows = InterlokCommandUtils.getAllWorkflows(connection, channel);
            for (WorkflowManagerMBean workflow : workflows) {
              table.add(InterlokCommandUtils.statusRow(workflow, showJmxDetails));
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
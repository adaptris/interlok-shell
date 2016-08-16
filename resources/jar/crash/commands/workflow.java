package com.adaptris.crash.commands;

import java.lang.management.ManagementFactory;
import java.util.*;

import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.WorkflowManagerMBean;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Man;
import org.crsh.cli.Usage;
import org.crsh.cli.Option;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.crsh.text.Color;
import org.crsh.text.RenderPrintWriter;
import org.crsh.text.ui.Element;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.RowElement;
import org.crsh.text.ui.TableElement;
import org.crsh.text.Decoration;
import org.crsh.text.Style;

import com.adaptris.core.ComponentState;
import com.adaptris.core.InitialisedState;
import com.adaptris.core.StartedState;
import com.adaptris.core.runtime.ChannelManagerMBean;
import com.adaptris.core.util.JmxHelper;

@Usage("Interlok Workflow Management")
@Man("The workflow commands allowing you to control Interlok worflows (listing, starting, stopping etc).")
public class workflow extends AdapterBaseCommand {

  @Usage("List workflows")
  @Man("Lists all available Interlok Workflow MBean info:\n" +
      "% workflow list\n" +
      "...")
  @Command
  public void list(InvocationContext<Object> context,
                   @Usage("show jmx details")
                   @Man("Supplement results with JMX object names")
                   @Option(names = {"j","show-jmx-details"})
                   final Boolean showJmxDetails) throws Exception {


    try {
      TableElement table = new TableElement().rightCellPadding(1);
      AdapterManagerMBean adapter = getAdapter();
      table.add(listRow(adapter, showJmxDetails));
      Collection<ChannelManagerMBean> channels = getAllChannels(adapter);
      for (ChannelManagerMBean channel : channels) {
        table.add(listRow(channel, showJmxDetails));
        Collection<WorkflowManagerMBean> workflows = getAllWorkflows(channel);
        for (WorkflowManagerMBean workflow : workflows){
          table.add(listRow(workflow, showJmxDetails));
        }
      }
      context.provide(table);
    } catch (Exception ex) {
      context.provide(new LabelElement(ex.getMessage()).style(Style.style(Color.red)));
    }
  }

}
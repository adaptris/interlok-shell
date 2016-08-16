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

  @Usage("workflow list")
  @Man("Lists all available Interlok Workflow MBean info:\n" +
      "% workflow list\n" +
      "...\n" +
      "The results can be supplemented with JMX object names:\n" +
      "% workflow list --show-jmx-details\n" +
      "...")
  @Command
  public void list(InvocationContext<Object> context,
                   @Usage("show jmx details")
                   @Option(names = {"j","show-jmx-details"})
                   final Boolean showJmxDetails) throws Exception {


    try {
      TableElement table = new TableElement().rightCellPadding(1);
      AdapterManagerMBean adapter = getAdapter();
      RowElement adapterRow = new RowElement();
      adapterRow.add(new LabelElement("|-|"));
      adapterRow.add(new LabelElement(adapter.getUniqueId()).style(statusColor(adapter)));
      if (Boolean.TRUE.equals(showJmxDetails)) {
        adapterRow.add(adapter.createObjectName().toString());
      }
      table.add(adapterRow);
      Collection<ChannelManagerMBean> channels = getAllChannels();
      for (ChannelManagerMBean c : channels) {
        RowElement channelRow = new RowElement();
        channelRow.add(new LabelElement("|--|"));
        channelRow.add(new LabelElement(c.getUniqueId()).style(statusColor(c)));
        if (Boolean.TRUE.equals(showJmxDetails)) {
          channelRow.add(c.createObjectName().toString());
        }
        table.add(channelRow);
        Collection<WorkflowManagerMBean> workflows = getAllWorkflows(c);
        for (WorkflowManagerMBean w : workflows){
          RowElement workflowRow = new RowElement();
          workflowRow.add(new LabelElement("|---|"));
          workflowRow.add(new LabelElement(w.getUniqueId()).style(statusColor(w)));
          if (Boolean.TRUE.equals(showJmxDetails)) {
            workflowRow.add(w.createObjectName().toString());
          }
          table.add(workflowRow);
        }
      }
      context.provide(table);
    } catch (Exception ex) {
      context.provide(new LabelElement(ex.getMessage()).style(Style.style(Color.red)));
    }
  }

}
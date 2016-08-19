package com.adaptris.crash.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.management.*;

import com.adaptris.core.runtime.WorkflowManagerMBean;
import org.apache.commons.lang.StringUtils;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.crsh.text.Color;
import org.crsh.text.RenderPrintWriter;

import com.adaptris.core.ComponentState;
import com.adaptris.core.StartedState;
import com.adaptris.core.runtime.AdapterComponentMBean;
import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.ChannelManagerMBean;
import com.adaptris.core.util.JmxHelper;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.RowElement;

public abstract class AdapterBaseCommand extends BaseCommand {

  protected enum ComponentStateColour {
    InitialisedState() {
      Color colour() {
        return Color.yellow;
      }
    },
    StartedState() {
      Color colour() {
        return Color.green;
      }
    },
    StoppedState() {
      Color colour() {
        return Color.red;
      }
    },
    ClosedState {
      Color colour() {
        return Color.red;
      }
    };
    abstract Color colour();
  }

  protected enum ComponentTypeIndent{

    Adapter(1),
    Channel(2),
    Workflow(3);

    private final int indent;

    ComponentTypeIndent(int indent){
      this.indent = indent;
    }

    public int getIndent() {
      return indent;
    }
  }

  @Deprecated
  protected transient MBeanServer server;

  public AdapterBaseCommand() {
    server = JmxHelper.findMBeanServer();
  }

  @Deprecated
  protected WorkflowManagerMBean getWorkflow(String channelName, String workflowName) throws Exception {
    ObjectName workflowObject = getWorkflowObject(channelName, workflowName);
    if (!server.isRegistered(workflowObject)){
      throw new InstanceNotFoundException("[" + workflowName + "] in [" + channelName +"] not found");
    }
    WorkflowManagerMBean bean = JMX.newMBeanProxy(server, workflowObject, WorkflowManagerMBean.class);
    return bean;
  }

  @Deprecated
  protected ChannelManagerMBean getChannel(String channelName) throws Exception {
    ObjectName channelObject = getChannelObject(channelName);
    if (!server.isRegistered(channelObject)) {
      throw new InstanceNotFoundException("[" + channelName + "] not found");
    }
    ChannelManagerMBean bean = JMX.newMBeanProxy(server, channelObject, ChannelManagerMBean.class);
    return bean;
  }

  public AdapterManagerMBean getAdapter(MBeanServerConnection serverConnection) throws Exception {
    return JMX.newMBeanProxy(serverConnection, getAdapterObject(serverConnection), AdapterManagerMBean.class);
  }

  @Deprecated
  protected AdapterManagerMBean getAdapter() throws Exception {
    return JMX.newMBeanProxy(server, getAdapterObject(), AdapterManagerMBean.class);
  }

  @Deprecated
  protected Collection<WorkflowManagerMBean> getAllWorkflows(ChannelManagerMBean channelManagerMBean) throws Exception {
    Collection<ObjectName> children = channelManagerMBean.getChildren();
    Collection<WorkflowManagerMBean> result = new ArrayList<WorkflowManagerMBean>();
    for (ObjectName o : children) {
      result.add(JMX.newMBeanProxy(server, o, WorkflowManagerMBean.class));
    }
    return result;
  }


  @Deprecated
  protected Collection<ChannelManagerMBean> getAllChannels(AdapterManagerMBean adapter) throws Exception {
    Collection<ObjectName> children = adapter.getChildren();
    Collection<ChannelManagerMBean> result = new ArrayList<ChannelManagerMBean>();
    for (ObjectName o : children) {
      result.add(JMX.newMBeanProxy(server, o, ChannelManagerMBean.class));
    }
    return result;
  }

  @Deprecated
  protected ObjectName getWorkflowObject(String channelName, String workflowName) throws Exception {
    String channelString = "com.adaptris:type=Workflow,adapter=" + getAdapterName() + ",id=" + channelName + ", workflow=" + workflowName;
    ObjectName workflowObject = ObjectName.getInstance(channelString);
    return workflowObject;
  }

  @Deprecated
  protected ObjectName getChannelObject(String channelName) throws Exception {
    String channelString = "com.adaptris:type=Channel,adapter=" + getAdapterName() + ",id=" + channelName;
    ObjectName channelObject = ObjectName.getInstance(channelString);
    return channelObject;
  }

  @Deprecated
  protected String getAdapterName() throws Exception {
    return getAdapterObject().getKeyProperty("id");
  }

  @Deprecated
  protected Set<ObjectInstance> queryJmx(String pattern)
      throws Exception {
    ObjectName patternName = pattern != null ? ObjectName.getInstance(pattern) : null;
    return server.queryMBeans(patternName, null);
  }

  protected ObjectName getAdapterObject(MBeanServerConnection serverConnection) throws Exception {
    String interlokBaseObject = "com.adaptris:type=Adapter,id=*";
    ObjectName patternName = ObjectName.getInstance(interlokBaseObject);
    Set<ObjectInstance> instances = serverConnection.queryMBeans(patternName, null);

    if (instances.size() == 0)
      throw new InstanceNotFoundException("No configured Adapters");
    else
      return instances.iterator().next().getObjectName();
  }

  @Deprecated
  protected ObjectName getAdapterObject() throws Exception {
    String interlokBaseObject = "com.adaptris:type=Adapter,id=*";
    ObjectName patternName = ObjectName.getInstance(interlokBaseObject);
    Set<ObjectInstance> instances = server.queryMBeans(patternName, null);

    if (instances.size() == 0)
      throw new InstanceNotFoundException("No configured Adapters");
    else
      return instances.iterator().next().getObjectName();
  }

  protected void logStatus(InvocationContext<Object> context, AdapterComponentMBean instance) throws Exception {
    context.provide(new LabelElement(instance.getUniqueId()).style(statusColor(instance)));
  }

  protected Style.Composite statusColor(AdapterComponentMBean instance) throws Exception{
    ComponentState state = instance.getComponentState();
    return Style.style(ComponentStateColour.valueOf(state.toString()).colour());
  }

  public RowElement listRow(AdapterComponentMBean instance, Boolean showJmxDetails) throws Exception{
    RowElement row = new RowElement();
    int indent = ComponentTypeIndent.valueOf(instance.createObjectName().getKeyProperty("type")).getIndent();
    row.add(new LabelElement("|" + StringUtils.repeat("-", indent) + "|"));
    row.add(new LabelElement(instance.getUniqueId()).style(statusColor(instance)));
    if (Boolean.TRUE.equals(showJmxDetails)) {
      row.add(instance.createObjectName().toString());
    }
    return row;
  }

  protected boolean isStarted(AdapterComponentMBean instance) {
    ComponentState state = instance.getComponentState();
    return state.equals(StartedState.getInstance());
  }
}

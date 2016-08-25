package com.adaptris.crash.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.commons.lang.StringUtils;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.crsh.text.Color;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.RowElement;

import com.adaptris.core.ComponentState;
import com.adaptris.core.StartedState;
import com.adaptris.core.runtime.AdapterComponentMBean;
import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.AdapterRegistryMBean;
import com.adaptris.core.runtime.ChannelManagerMBean;
import com.adaptris.core.runtime.WorkflowManagerMBean;

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

  public ChannelManagerMBean getChannel(MBeanServerConnection serverConnection, String channelName) throws Exception {
    ObjectName channelObject = getChannelObject(serverConnection, channelName);
    if (!serverConnection.isRegistered(channelObject)) {
      throw new InstanceNotFoundException("[" + channelName + "] not found");
    }
    ChannelManagerMBean bean = JMX.newMBeanProxy(serverConnection, channelObject, ChannelManagerMBean.class);
    return bean;
  }

  public AdapterManagerMBean getAdapter(MBeanServerConnection serverConnection) throws Exception {
    return JMX.newMBeanProxy(serverConnection, getAdapterObject(serverConnection), AdapterManagerMBean.class);
  }

  public AdapterRegistryMBean getRegistry(MBeanServerConnection serverConnection) throws Exception {
    return JMX.newMBeanProxy(serverConnection, ObjectName.getInstance(AdapterRegistryMBean.STANDARD_REGISTRY_JMX_NAME),
        AdapterRegistryMBean.class);
  }

  public Collection<WorkflowManagerMBean> getAllWorkflows(MBeanServerConnection serverConnection, ChannelManagerMBean channelManagerMBean) throws Exception {
    Collection<ObjectName> children = channelManagerMBean.getChildren();
    Collection<WorkflowManagerMBean> result = new ArrayList<WorkflowManagerMBean>();
    for (ObjectName o : children) {
      result.add(JMX.newMBeanProxy(serverConnection, o, WorkflowManagerMBean.class));
    }
    return result;
  }

  public Collection<ChannelManagerMBean> getAllChannels(MBeanServerConnection serverConnection, AdapterManagerMBean adapter) throws Exception {
    Collection<ObjectName> children = adapter.getChildren();
    Collection<ChannelManagerMBean> result = new ArrayList<ChannelManagerMBean>();
    for (ObjectName o : children) {
      result.add(JMX.newMBeanProxy(serverConnection, o, ChannelManagerMBean.class));
    }
    return result;
  }

  public ObjectName getChannelObject(MBeanServerConnection serverConnection, String channelName) throws Exception {
    String channelString = "com.adaptris:type=Channel,adapter=" + getAdapterName(serverConnection) + ",id=" + channelName;
    ObjectName channelObject = ObjectName.getInstance(channelString);
    return channelObject;
  }

  protected String getAdapterName(MBeanServerConnection serverConnection) throws Exception {
    return getAdapterObject(serverConnection).getKeyProperty("id");
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

  public boolean isStarted(AdapterComponentMBean instance) {
    ComponentState state = instance.getComponentState();
    return state instanceof StartedState;
  }

}

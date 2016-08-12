package com.adaptris.crash.commands;

import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.crsh.command.BaseCommand;

import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.ChannelManagerMBean;
import com.adaptris.core.util.JmxHelper;

public abstract class AdapterBaseCommand extends BaseCommand {

  protected transient MBeanServer server;

  public AdapterBaseCommand() {
    server = JmxHelper.findMBeanServer();
  }

  protected ChannelManagerMBean getChannel(String channelName) throws Exception {
    ObjectName channelObject = getChannelObject(channelName);
    if (!server.isRegistered(channelObject)) {
      throw new InstanceNotFoundException("[" + channelName + "] not found");
    }
    ChannelManagerMBean bean = JMX.newMBeanProxy(server, channelObject, ChannelManagerMBean.class);
    return bean;
  }

  protected AdapterManagerMBean getAdapter() throws Exception {
    return JMX.newMBeanProxy(server, getAdapterObject(), AdapterManagerMBean.class);
  }


  protected ObjectName getChannelObject(String channelName) throws Exception, MalformedObjectNameException {
    String channelString = "com.adaptris:type=Channel,adapter=" + getAdapterName() + ",id=" + channelName;
    ObjectName channelObject = ObjectName.getInstance(channelString);
    return channelObject;
  }

  protected String getAdapterName() throws Exception {
    return getAdapterObject().getKeyProperty("id");
  }


  protected Set<ObjectInstance> queryJmx(String pattern)
      throws Exception {
    ObjectName patternName = pattern != null ? ObjectName.getInstance(pattern) : null;
    return server.queryMBeans(patternName, null);
  }

  protected ObjectName getAdapterObject() throws Exception {
    String interlokBaseObject = "com.adaptris:type=Adapter,id=*";
    ObjectName patternName = ObjectName.getInstance(interlokBaseObject);
    Set<ObjectInstance> instances = server.queryMBeans(patternName, null);

    if (instances.size() == 0)
      throw new InstanceNotFoundException("No configured Adapters");
    else
      return instances.iterator().next().getObjectName();
  }


}

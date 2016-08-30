package com.adaptris.crash.commands.actions;

import com.adaptris.core.ComponentState;
import com.adaptris.core.runtime.AdapterComponentMBean;
import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.AdapterRegistryMBean;
import org.apache.commons.lang.StringUtils;
import org.crsh.text.Color;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.RowElement;

import javax.management.*;
import java.util.Set;

abstract class BaseCommandAction {

  @SuppressWarnings("unused")
  private enum ComponentStateColour {
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

  @SuppressWarnings("unused")
  private enum ComponentTypeIndent{
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

  static AdapterManagerMBean getAdapter(MBeanServerConnection serverConnection) throws Exception {
    return JMX.newMBeanProxy(serverConnection, getAdapterObject(serverConnection), AdapterManagerMBean.class);
  }

  static AdapterRegistryMBean getRegistry(MBeanServerConnection serverConnection) throws Exception {
    return JMX.newMBeanProxy(serverConnection, ObjectName.getInstance(AdapterRegistryMBean.STANDARD_REGISTRY_JMX_NAME),
        AdapterRegistryMBean.class);
  }

  static RowElement statusRow(AdapterComponentMBean instance, Boolean showJmxDetails) throws Exception{
    RowElement row = new RowElement();
    int indent = ComponentTypeIndent.valueOf(instance.createObjectName().getKeyProperty("type")).getIndent();
    row.add(new LabelElement("|" + StringUtils.repeat("-", indent) + "|"));
    row.add(new LabelElement(instance.getUniqueId()).style(statusColor(instance)));
    if (Boolean.TRUE.equals(showJmxDetails)) {
      row.add(instance.createObjectName().toString());
    }
    return row;
  }

  private static ObjectName getAdapterObject(MBeanServerConnection serverConnection) throws Exception {
    String interlokBaseObject = "com.adaptris:type=Adapter,id=*";
    ObjectName patternName = ObjectName.getInstance(interlokBaseObject);
    Set<ObjectInstance> instances = serverConnection.queryMBeans(patternName, null);

    if (instances.size() == 0) {
      throw new InstanceNotFoundException("No configured Adapters");
    } else {
      return instances.iterator().next().getObjectName();
    }
  }

  private static Style.Composite statusColor(AdapterComponentMBean instance) throws Exception{
    ComponentState state = instance.getComponentState();
    return Style.style(ComponentStateColour.valueOf(state.toString()).colour());
  }
}

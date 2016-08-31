package com.adaptris.crash.commands.actions;

import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.AdapterRegistryMBean;
import com.adaptris.core.runtime.ChannelManagerMBean;
import com.adaptris.core.runtime.WorkflowManagerMBean;
import com.adaptris.crash.commands.InterlokCommandUtils;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;
import org.crsh.text.ui.TableElement;

import javax.management.MBeanServerConnection;
import java.util.Collection;
import java.util.Map;

public enum AdapterCommandAction implements CommandAction {

  start {
    @Override
    public String execute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException {
      try {
        AdapterManagerMBean adapter = InterlokCommandUtils.getAdapter(connection);
        adapter.requestStart(TIMEOUT);
        return "Adapter (" + adapter.getUniqueId() + ") started\n";
      } catch (Exception e) {
        throw new ScriptException("Could not start the adapter: " + e.getMessage(), e);
      }
    }

  },
  stop {
    @Override
    public String execute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) {
      try {
        AdapterManagerMBean adapter = InterlokCommandUtils.getAdapter(connection);
        adapter.requestClose(TIMEOUT);
        return "Adapter (" + adapter.getUniqueId() + ") stopped/closed\n";
      } catch (Exception e) {
        throw new ScriptException("Could not stop the adapter: " + e.getMessage(), e);
      }
    }
  },
  restart {
    @Override
    public String execute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) {
      try {
        AdapterManagerMBean adapter = InterlokCommandUtils.getAdapter(connection);
        adapter.requestRestart(TIMEOUT);
        return "Adapter (" + adapter.getUniqueId() + ") restarted\n";
      } catch (Exception e) {
        throw new ScriptException("Could not restart the adapter: " + e.getMessage(), e);
      }
    }
  },
  reload {
    @Override
    public String execute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) {
      try {
        AdapterRegistryMBean registry = InterlokCommandUtils.getRegistry(connection);
        registry.reloadFromConfig();
        AdapterManagerMBean adapter = InterlokCommandUtils.getAdapter(connection);
        return "Adapter (" + adapter.getUniqueId() + ") reloaded.\n";
      } catch (Exception e) {
        throw new ScriptException("Could not reload the adapter: " + e.getMessage(), e);
      }
    }
  },
  reloadVCS{
    @Override
    public String execute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException {
      try {

        AdapterRegistryMBean registry = InterlokCommandUtils.getRegistry(connection);
        if (registry.getVersionControl() != null) {
          registry.reloadFromVersionControl();
          AdapterManagerMBean adapter = InterlokCommandUtils.getAdapter(connection);
          return "Adapter (" + adapter.getUniqueId() + ") reloaded.\n";
        } else {
          throw new ScriptException("No Version Control");
        }
      } catch (Exception e) {
        throw new ScriptException("Could not reload the adapter: " + e.getMessage(), e);
      }
    }
  },

  status {
    @Override
    public String execute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException {
      try {
        if(!validateArguments(arguments)){
          throw new ScriptException(SHOW_JMX_DETAILS_KEY + " argument is required");
        }
        Boolean showJmxDetails = (Boolean)arguments.get(SHOW_JMX_DETAILS_KEY);
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
        return null;
      } catch (Exception e) {
        throw new ScriptException(e.getMessage(),e);
      }
    }

    @Override
    public boolean validateArguments(Map<String, Object> arguments) {
      return arguments.containsKey(SHOW_JMX_DETAILS_KEY) && (arguments.get(SHOW_JMX_DETAILS_KEY) == null || arguments.get(SHOW_JMX_DETAILS_KEY) instanceof Boolean);
    }
  };

  public static final String SHOW_JMX_DETAILS_KEY = "showJMXDetails";
  private static final long TIMEOUT = 60000L;

  public boolean validateArguments(Map<String, Object> arguments){
    return true;
  }
}

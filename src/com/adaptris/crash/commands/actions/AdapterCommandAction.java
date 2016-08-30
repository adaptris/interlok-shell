package com.adaptris.crash.commands.actions;

import com.adaptris.core.runtime.AdapterManagerMBean;
import com.adaptris.core.runtime.AdapterRegistryMBean;
import org.crsh.command.InvocationContext;
import org.crsh.command.ScriptException;
import org.crsh.text.ui.TableElement;

import javax.management.MBeanServerConnection;
import java.util.Map;

public class AdapterCommandAction extends BaseCommandAction {

  private static final long TIMEOUT = 60000L;

  public enum Commands implements CommandAction {

    start {
      @Override
      public String execute(InvocationContext<Object> context, MBeanServerConnection connection, Map<String, Object> arguments) throws ScriptException {
        try {
          AdapterManagerMBean adapter = getAdapter(connection);
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
          AdapterManagerMBean adapter = getAdapter(connection);
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
          AdapterManagerMBean adapter = getAdapter(connection);
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
          AdapterRegistryMBean registry = getRegistry(connection);
          registry.reloadFromConfig();
          AdapterManagerMBean adapter = getAdapter(connection);
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

          AdapterRegistryMBean registry = getRegistry(connection);
          if (registry.getVersionControl() != null) {
            registry.reloadFromVersionControl();
            AdapterManagerMBean adapter = getAdapter(connection);
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
            throw new ScriptException("showJMXDetails argument is required");
          }
          TableElement table = new TableElement().rightCellPadding(1);
          table.add(statusRow(getAdapter(connection), (Boolean)arguments.get("showJMXDetails")));
          context.provide(table);
          return null;
        } catch (Exception e) {
          throw new ScriptException(e.getMessage(),e);
        }
      }

      @Override
      protected boolean validateArguments(Map<String, Object> arguments) {
        return arguments.containsKey("showJMXDetails") && (arguments.get("showJMXDetails") == null || arguments.get("showJMXDetails") instanceof Boolean);
      }
    };

    protected boolean validateArguments(Map<String, Object> arguments){
      return true;
    }

  }
}

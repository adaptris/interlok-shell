package com.adaptris.crash.commands.completion;

import com.adaptris.core.runtime.ChannelManagerMBean;
import com.adaptris.crash.commands.InterlokCommandUtils;
import org.crsh.cli.descriptor.ParameterDescriptor;
import org.crsh.cli.spi.Completer;
import org.crsh.cli.spi.Completion;

import javax.management.MBeanServerConnection;
import java.util.Collection;

public class ChannelCompletion implements Completer {

  private transient MBeanServerConnection serverConnection;

  public ChannelCompletion(MBeanServerConnection serverConnection) {
    this.serverConnection = serverConnection;
  }

  @Override
  public Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
    Completion.Builder builder = null;
    Collection<ChannelManagerMBean> channels = InterlokCommandUtils.getAllChannels(serverConnection, InterlokCommandUtils.getAdapter(serverConnection));
    for (ChannelManagerMBean value : channels) {
      String name = value.getUniqueId();
      if (name.startsWith(prefix)) {
        if (builder == null) {
          builder = Completion.builder(prefix);
        }
        builder.add(name.substring(prefix.length()), true);
      }
    }
    return  builder != null ? builder.build() : Completion.create();
  }
}

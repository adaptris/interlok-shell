package com.adaptris.crash.commands;

import javax.management.MBeanServerConnection;

public interface AdapterConnectionCommand {

  MBeanServerConnection connection();
}

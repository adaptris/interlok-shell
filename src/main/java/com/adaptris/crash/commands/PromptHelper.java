package com.adaptris.crash.commands;

import java.util.Set;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import com.adaptris.core.util.JmxHelper;

public class PromptHelper {

  private static String adapterId = null;


  public static String getPrompt() {
    return getAdapterId() + "% ";
  }

  private static String getAdapterId() {

    if (adapterId == null) {
      try {
        MBeanServer server = JmxHelper.findMBeanServer();
        String interlokBaseObject = "com.adaptris:type=Adapter,id=*";
        ObjectName patternName = ObjectName.getInstance(interlokBaseObject);
        Set<ObjectInstance> instances = server.queryMBeans(patternName, null);
        if (instances.size() > 0)
          adapterId = instances.iterator().next().getObjectName().getKeyProperty("id");
      } catch (Exception e) {
        adapterId = "";
      }
    }
    return adapterId;
  }
}

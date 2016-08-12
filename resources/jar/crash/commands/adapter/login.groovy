welcome = { ->
  def hostName;
  def adapterVersion;
  try {
    hostName = java.net.InetAddress.getLocalHost().getHostName();
  } catch (java.net.UnknownHostException ignore) {
    hostName = "localhost";
  }
  adapterVersion = com.adaptris.core.management.VersionReport.getInstance().getAdapterBuildVersion();
  String fileContents = org.apache.commons.io.IOUtils.toString(getClass().getClassLoader().getResourceAsStream("crash/adaptris-logo.txt"),"UTF-8");
  String output = fileContents + System.lineSeparator() +
          " Interlok Version: " + adapterVersion + System.lineSeparator() +
          "             Host: " + hostName + System.lineSeparator() +
          System.lineSeparator();
  return output;
}

prompt = { ->
  return "% ";
}
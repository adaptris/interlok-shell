welcome = { ->
  def hostName;
  def adapterVersion;
  try {
    hostName = java.net.InetAddress.getLocalHost().getHostName();
  } catch (java.net.UnknownHostException ignore) {
    hostName = "localhost";
  }
  adapterVersion = com.adaptris.core.management.VersionReport.getInstance().getAdapterBuildVersion(); 
  return """\

 
 _____         _                _         _    
|_   _|       | |              | |       | |    
  | |   _ __  | |_   ___  _ __ | |  ___  | | __
  | |  | '_ ` | __| / _ `| '__|| | / _ ` | |/ /
 _| |_ | | | || |_ |  __/| |   | || (_) ||   < 
 `___/ |_| |_| `__| `___||_|   |_| `___/ |_|`_`


Follow and support the project on http://www.crashub.org
Welcome to $hostName running $adapterVersion
It is ${new Date()} now
""";
}

prompt = { ->
  return "% ";
}
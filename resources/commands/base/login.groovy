welcome = { ->
  def hostName;
  try {
    hostName = java.net.InetAddress.getLocalHost().getHostName();
  } catch (java.net.UnknownHostException ignore) {
    hostName = "localhost";
  }
  return """\

_____  _____    _____  ________________ _____________  __________    _____             ________    _____  ______
|    | |    |   |    | |              | |            | |         `   |    |           ~        `   |    | ~    ~  ${crash.context.version}
|    | |     `  |    | |____      ____| |     _______| |    __    `  |    |          ~          `  |    |~    ~
|    | |      ` |    |      |    |      |     |___     |   |_|    |  |    |         |     __     | |         ~
|    | |       `|    |      |    |      |         |    |          ~  |    |         |    |_|     | |        `
|    | |    |`       |      |    |      |     ____|    |         `   |    |         |            | |         `
|    | |    | `      |      |    |      |     |_______ |         `   |    |________ |            | |          `
|    | |    |  `     |      |    |      |            | |    |`    `  |            |  `          ~  |    |`     |
|____| |____|   `____|      |____|      |____________| |____| `____` |____________|   `________~   |____| `____|

Follow and support the project on http://www.crashub.org
Welcome to $hostName + !
It is ${new Date()} now
""";
}

prompt = { ->
  return "% ";
}

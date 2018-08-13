import com.adaptris.core.management.VersionReport
import org.crsh.cli.Command
import org.crsh.cli.Usage

class version {
  @Usage("Display Interlok version information")
  @Command
  Object main() {
    VersionReport r = VersionReport.getInstance();
    StringBuilder sb = new StringBuilder();
    sb.append("Version Information");
    for (String modules : r.getReport()) {
      sb.append(System.lineSeparator())
      sb.append("  " + modules);
    }
    return sb.toString();
  }
}
import com.adaptris.core.management.VersionReport;
import java.lang.StringBuilder;

class version {
  @Usage("show the interlok version")
  @Command
  Object main() {
    VersionReport r = VersionReport.getInstance();
    StringBuilder sb = new StringBuilder();
    sb.append("Version Information");
    sb.append(System.lineSeparator())
    for (String modules : r.getReport()) {
      sb.append("  " + modules);
      sb.append(System.lineSeparator())
    }
    return sb.toString();
  }
}
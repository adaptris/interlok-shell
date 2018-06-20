package crash.commands

import org.crsh.cli.Command
import org.crsh.cli.Man
import org.crsh.cli.Usage

@Usage("clear the terminal screen")
class clear {

  @Command
  @Man("clear  clears your screen if possible.")
  void main() {
    out.cls()
  }
}

package com.adaptris.crash.commands.parameters;

import org.crsh.cli.Man;
import org.crsh.cli.Option;
import org.crsh.cli.Usage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Usage("show jmx details")
@Man("Supplement results with JMX object names")
@Option(names = {"j","show-jmx-details"})
public @interface ShowJMXDetailsOptions {
}

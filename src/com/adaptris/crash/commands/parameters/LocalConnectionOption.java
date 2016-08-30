package com.adaptris.crash.commands.parameters;

import org.crsh.cli.Man;
import org.crsh.cli.Option;
import org.crsh.cli.Usage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Usage("Ignores JMXServiceURL as use local connection")
@Man("Ignores JMXServiceURL as use local connection")
@Option(names = {"local"})
public @interface LocalConnectionOption {
}

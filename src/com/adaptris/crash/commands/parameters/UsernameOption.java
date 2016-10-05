package com.adaptris.crash.commands.parameters;

import org.crsh.cli.Option;
import org.crsh.cli.Usage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Option(names={"u","username"})
@Usage("username for JMX connection")
public @interface UsernameOption{
}
package com.adaptris.crash.commands.completion;

import com.adaptris.crash.commands.actions.NamedCommandAction;
import org.crsh.cli.descriptor.ParameterDescriptor;
import org.crsh.cli.spi.Completer;
import org.crsh.cli.spi.Completion;

import java.util.EnumSet;


public class NamedCommandCompletion implements Completer {

  private final Class<? extends Enum> clazz;

  public <S extends Enum & NamedCommandAction> NamedCommandCompletion(Class<S> clazz){
    this.clazz = clazz;
  }

  @Override
  public Completion complete(ParameterDescriptor parameter, String prefix) throws Exception {
    Completion.Builder builder = null;
    for (Object e : EnumSet.allOf(clazz)){
      String name = ((NamedCommandAction)e) .commandName();
      if (name.startsWith(prefix)) {
        if (builder == null) {
          builder = Completion.builder(prefix);
        }
        builder.add(name.substring(prefix.length()), true);
      }
    }
    return  builder != null ? builder.build() : Completion.create();
  }

}

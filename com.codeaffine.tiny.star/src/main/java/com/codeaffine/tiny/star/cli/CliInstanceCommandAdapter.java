package com.codeaffine.tiny.star.cli;

import static lombok.AccessLevel.PACKAGE;

import static java.util.Objects.nonNull;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.spi.CliCommand;

import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor(access = PACKAGE)
class CliInstanceCommandAdapter implements CliCommand {

    @NonNull
    private final ApplicationServer applicationServer;
    @NonNull
    @Delegate(excludes = Excludes.class)
    private final CliCommand delegate;
    private final Integer cliInstanceNumber;

    interface Excludes {
        String getCode();
        void execute(ApplicationServer applicationServer, Map<String, CliCommand> codeToCommandMap);
    }

    @Override
    public String getCode() {
        if(nonNull(cliInstanceNumber)) {
            return delegate.getCode() + cliInstanceNumber;
        }
        return delegate.getCode();
    }

    @Override
    public void execute(ApplicationServer applicationServer, Map<String, CliCommand> codeToCommandMap) {
        delegate.execute(this.applicationServer, new HashMap<>(codeToCommandMap));
    }
}

package com.balugaq.msua.command;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Obsolete
public class CommandMissingArgException extends RuntimeException {
    public final List<String> missingRequirements;

    public List<String> getMissingRequirements() {
        return missingRequirements;
    }

    public CommandMissingArgException(List<String> missingRequirements) {
        super();
        this.missingRequirements = missingRequirements;
    }

}

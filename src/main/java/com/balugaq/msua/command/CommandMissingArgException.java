package com.balugaq.msua.command;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Obsolete
@Getter
public class CommandMissingArgException extends RuntimeException {
    public final List<String> missingRequirements;

    public CommandMissingArgException(List<String> missingRequirements) {
        super();
        this.missingRequirements = missingRequirements;
    }

}

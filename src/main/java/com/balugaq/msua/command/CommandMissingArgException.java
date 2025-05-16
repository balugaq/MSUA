package com.balugaq.msua.command;

import lombok.Getter;

import java.util.List;

@Getter
public class CommandMissingArgException extends RuntimeException {
    public final List<String> missingRequirements;
    public CommandMissingArgException(List<String> missingRequirements) {
        super();
        this.missingRequirements = missingRequirements;
    }

}

package com.ollie.mcsoc_hunt.helpers;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TaskHolder {

    private final List<String> locations = new ArrayList<>();

    public TaskHolder() {

        locations.add("test");
        locations.add("test2");
        locations.add("test3");
        locations.add("test4");

    }

}

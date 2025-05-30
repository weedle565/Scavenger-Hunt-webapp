package com.ollie.mcsoc_hunt.helpers;

import com.ollie.mcsoc_hunt.entities.Task;
import lombok.Builder;

import java.util.List;

@Builder
public class TaskDTO {

    private Long id;
    private String name;
    private String place;  // May be obfuscated
}

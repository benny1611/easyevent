package com.benny1611.easyevent.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChangeRolesRequest {
    private List<String> roles;
}

package com.ansh.dto;

import lombok.Data;

@Data
public class RequestDTO<T> {

    private String source;

    private T data;
}

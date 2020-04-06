package com.ansh.service;

import com.ansh.dto.RequestDTO;
import com.ansh.dto.RequestData;
import com.ansh.dto.ResponseDTO;
import com.ansh.dto.ResponseData;
import com.ansh.util.HttpRestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BasicService {

    public ResponseDTO getMethod(){
        log.info("Basic service : getMethod method");
        ResponseDTO<ResponseData> response = HttpRestUtils.getData("http://localhost:8080/get", null, null, ResponseDTO.class);
        return response;
    }

    public ResponseDTO postMethod(RequestDTO<RequestData> requestDTO){
        log.info("Basic service : postMethod method");
        ResponseDTO<ResponseData> response = HttpRestUtils.postData("http://localhost:8080/post", requestDTO, ResponseDTO.class);
        return response;
    }
}

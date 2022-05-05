package com.example.learning.erpMain.payload.response;

import com.example.learning.erpMain.payload.request.ErrorDto;

import java.util.Set;

public class ErrorResponse {
    private String status;
    private Set<ErrorDto> errorDto;

    public Set<ErrorDto> getErrorDto() {
        return errorDto;
    }

    public void setErrorDto(Set<ErrorDto> errorDto) {
        this.errorDto = errorDto;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}

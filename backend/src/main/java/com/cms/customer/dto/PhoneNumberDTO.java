package com.cms.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumberDTO {

    private Long id;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    private String phoneType;

    private Boolean isPrimary;
}

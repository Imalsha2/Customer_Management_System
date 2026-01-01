package com.cms.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountryDTO {

    private Long id;

    @NotBlank(message = "Country name is required")
    private String name;

    @NotBlank(message = "Country code is required")
    private String code;
}

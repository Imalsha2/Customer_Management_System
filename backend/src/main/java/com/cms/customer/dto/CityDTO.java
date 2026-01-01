package com.cms.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityDTO {

    private Long id;

    @NotBlank(message = "City name is required")
    private String name;

    @NotNull(message = "Country ID is required")
    private Long countryId;

    private String countryName;

    private String zipCode;
}

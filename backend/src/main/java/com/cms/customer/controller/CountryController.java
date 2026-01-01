package com.cms.customer.controller;

import com.cms.customer.dto.ApiResponse;
import com.cms.customer.dto.CountryDTO;
import com.cms.customer.entity.Country;
import com.cms.customer.repository.CountryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/countries")
public class CountryController {

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CountryDTO>>> getAllCountries() {
        List<Country> countries = countryRepository.findAll();
        List<CountryDTO> countryDTOs = countries.stream()
                .map(country -> modelMapper.map(country, CountryDTO.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(countryDTOs));
    }
}

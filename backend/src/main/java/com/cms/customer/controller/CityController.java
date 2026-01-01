package com.cms.customer.controller;

import com.cms.customer.dto.ApiResponse;
import com.cms.customer.dto.CityDTO;
import com.cms.customer.entity.City;
import com.cms.customer.repository.CityRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cities")
public class CityController {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CityDTO>>> getAllCities() {
        List<City> cities = cityRepository.findAll();
        List<CityDTO> cityDTOs = cities.stream()
                .map(city -> {
                    CityDTO dto = modelMapper.map(city, CityDTO.class);
                    dto.setCountryId(city.getCountry().getId());
                    dto.setCountryName(city.getCountry().getName());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(cityDTOs));
    }

    @GetMapping("/country/{countryId}")
    public ResponseEntity<ApiResponse<List<CityDTO>>> getCitiesByCountry(@PathVariable Long countryId) {
        List<City> cities = cityRepository.findByCountryId(countryId);
        List<CityDTO> cityDTOs = cities.stream()
                .map(city -> {
                    CityDTO dto = modelMapper.map(city, CityDTO.class);
                    dto.setCountryId(city.getCountry().getId());
                    dto.setCountryName(city.getCountry().getName());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(cityDTOs));
    }
}

package com.cms.customer.service;

import com.cms.customer.dto.CustomerDTO;
import com.cms.customer.dto.ImportResultDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CustomerService {

    CustomerDTO createCustomer(CustomerDTO customerDTO);

    CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO);

    CustomerDTO getCustomerById(Long id);

    Page<CustomerDTO> getAllCustomers(Pageable pageable);

    Page<CustomerDTO> searchCustomers(String keyword, Pageable pageable);

    void deleteCustomer(Long id);

    ImportResultDTO importCustomersFromExcel(MultipartFile file);

    byte[] exportCustomersToExcel();

    void addFamilyMember(Long customerId, Long familyMemberId);

    void removeFamilyMember(Long customerId, Long familyMemberId);
}

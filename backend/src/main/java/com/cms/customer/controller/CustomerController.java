package com.cms.customer.controller;

import com.cms.customer.dto.ApiResponse;
import com.cms.customer.dto.CustomerDTO;
import com.cms.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDTO>> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        CustomerDTO createdCustomer = customerService.createCustomer(customerDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully", createdCustomer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDTO customerDTO) {
        CustomerDTO updatedCustomer = customerService.updateCustomer(id, customerDTO);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", updatedCustomer));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomer(@PathVariable Long id) {
        CustomerDTO customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CustomerDTO>>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CustomerDTO> customers = customerService.getAllCustomers(pageable);

        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<CustomerDTO>>> searchCustomers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerDTO> customers = customerService.searchCustomers(keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully", null));
    }

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> importCustomers(
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Please select a file to upload"));
        }

        List<CustomerDTO> importedCustomers = customerService.importCustomersFromExcel(file);
        return ResponseEntity.ok(ApiResponse.success(
                "Imported " + importedCustomers.size() + " customers successfully",
                importedCustomers));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCustomers() {
        byte[] excelFile = customerService.exportCustomersToExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "customers.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(excelFile);
    }

    @PostMapping("/{customerId}/family-members/{familyMemberId}")
    public ResponseEntity<ApiResponse<Void>> addFamilyMember(
            @PathVariable Long customerId,
            @PathVariable Long familyMemberId) {

        customerService.addFamilyMember(customerId, familyMemberId);
        return ResponseEntity.ok(ApiResponse.success("Family member added successfully", null));
    }

    @DeleteMapping("/{customerId}/family-members/{familyMemberId}")
    public ResponseEntity<ApiResponse<Void>> removeFamilyMember(
            @PathVariable Long customerId,
            @PathVariable Long familyMemberId) {

        customerService.removeFamilyMember(customerId, familyMemberId);
        return ResponseEntity.ok(ApiResponse.success("Family member removed successfully", null));
    }
}

package com.cms.customer.service.impl;

import com.cms.customer.dto.CustomerDTO;
import com.cms.customer.entity.Customer;
import com.cms.customer.exception.DuplicateResourceException;
import com.cms.customer.exception.ResourceNotFoundException;
import com.cms.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private CustomerDTO customerDTO;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customerDTO = new CustomerDTO();
        customerDTO.setFirstName("John");
        customerDTO.setLastName("Doe");
        customerDTO.setNic("123456789V");
        customerDTO.setEmail("john.doe@example.com");
        customerDTO.setDateOfBirth(new Date());
        customerDTO.setGender("MALE");

        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setNic("123456789V");
        customer.setEmail("john.doe@example.com");
        customer.setDateOfBirth(new Date());
        customer.setGender("MALE");
    }

    @Test
    void testCreateCustomer_Success() {
        // Arrange
        when(customerRepository.existsByNic(customerDTO.getNic())).thenReturn(false);
        when(modelMapper.map(customerDTO, Customer.class)).thenReturn(customer);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(modelMapper.map(customer, CustomerDTO.class)).thenReturn(customerDTO);

        // Act
        CustomerDTO result = customerService.createCustomer(customerDTO);

        // Assert
        assertNotNull(result);
        assertEquals(customerDTO.getFirstName(), result.getFirstName());
        assertEquals(customerDTO.getNic(), result.getNic());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void testCreateCustomer_DuplicateNIC_ThrowsException() {
        // Arrange
        when(customerRepository.existsByNic(customerDTO.getNic())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> {
            customerService.createCustomer(customerDTO);
        });

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void testGetCustomerById_Success() {
        // Arrange
        when(customerRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(customer));
        when(modelMapper.map(customer, CustomerDTO.class)).thenReturn(customerDTO);

        // Act
        CustomerDTO result = customerService.getCustomerById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(customerDTO.getFirstName(), result.getFirstName());
    }

    @Test
    void testGetCustomerById_NotFound_ThrowsException() {
        // Arrange
        when(customerRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.getCustomerById(999L);
        });
    }

    @Test
    void testDeleteCustomer_Success() {
        // Arrange
        when(customerRepository.existsById(1L)).thenReturn(true);

        // Act
        customerService.deleteCustomer(1L);

        // Assert
        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteCustomer_NotFound_ThrowsException() {
        // Arrange
        when(customerRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.deleteCustomer(999L);
        });

        verify(customerRepository, never()).deleteById(anyLong());
    }
}

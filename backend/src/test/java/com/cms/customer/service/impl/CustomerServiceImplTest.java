package com.cms.customer.service.impl;

import com.cms.customer.dto.CustomerDTO;
import com.cms.customer.dto.ImportResultDTO;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
    private EntityManager entityManager;

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

        entityManager = mock(EntityManager.class);
        ReflectionTestUtils.setField(customerService, "entityManager", entityManager);
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

    @Test
    void testImportCustomersFromExcel_BatchSaveAndSkipDuplicates() throws Exception {
        // Create a small XLSX in-memory
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Customers");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("firstName");
        header.createCell(1).setCellValue("lastName");
        header.createCell(2).setCellValue("dateOfBirth");
        header.createCell(3).setCellValue("nic");
        header.createCell(4).setCellValue("email");
        header.createCell(5).setCellValue("gender");

        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("Alice");
        row1.createCell(1).setCellValue("Smith");
        row1.createCell(3).setCellValue("NIC-NEW");
        row1.createCell(4).setCellValue("alice@example.com");
        row1.createCell(5).setCellValue("FEMALE");

        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("Bob");
        row2.createCell(1).setCellValue("Jones");
        row2.createCell(3).setCellValue("NIC-DUP");
        row2.createCell(4).setCellValue("bob@example.com");
        row2.createCell(5).setCellValue("MALE");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "customers.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ByteArrayInputStream(baos.toByteArray()));

        // Existing NIC set should cause one skip
        when(customerRepository.findExistingNics(any(Set.class))).thenReturn(Set.of("NIC-DUP"));

        // Capture saveAll to assert only new NIC is persisted
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.List<Customer> savedList = (java.util.List<Customer>) invocation.getArgument(0);
            assertEquals(1, savedList.size());
            assertEquals("NIC-NEW", savedList.get(0).getNic());
            return null;
        }).when(customerRepository).saveAll(any());

        ImportResultDTO result = customerService.importCustomersFromExcel(multipartFile);

        assertEquals(1, result.getImportedCount());
        assertEquals(1, result.getSkippedDuplicates());
        assertTrue(result.getErrors().isEmpty());
        verify(customerRepository, times(1)).saveAll(any());
        verify(customerRepository, times(1)).flush();
    }
}

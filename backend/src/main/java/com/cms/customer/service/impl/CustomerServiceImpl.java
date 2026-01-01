package com.cms.customer.service.impl;

import com.cms.customer.dto.AddressDTO;
import com.cms.customer.dto.CustomerDTO;
import com.cms.customer.dto.ImportResultDTO;
import com.cms.customer.dto.PhoneNumberDTO;
import com.cms.customer.entity.Address;
import com.cms.customer.entity.City;
import com.cms.customer.entity.Customer;
import com.cms.customer.entity.PhoneNumber;
import com.cms.customer.exception.DuplicateResourceException;
import com.cms.customer.exception.ResourceNotFoundException;
import com.cms.customer.repository.CityRepository;
import com.cms.customer.repository.CustomerRepository;
import com.cms.customer.service.CustomerService;
import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private ModelMapper modelMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int BATCH_SIZE = 1000;
    private static final int ERROR_LIMIT = 100;

    @Override
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        // Check if NIC already exists
        if (customerRepository.existsByNic(customerDTO.getNic())) {
            throw new DuplicateResourceException("Customer", "NIC", customerDTO.getNic());
        }

        Customer customer = convertToEntity(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return convertToDTO(savedCustomer);
    }

    @Override
    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        // Check if NIC is being changed and if new NIC already exists
        if (!existingCustomer.getNic().equals(customerDTO.getNic())
                && customerRepository.existsByNic(customerDTO.getNic())) {
            throw new DuplicateResourceException("Customer", "NIC", customerDTO.getNic());
        }

        // Update basic fields
        existingCustomer.setFirstName(customerDTO.getFirstName());
        existingCustomer.setLastName(customerDTO.getLastName());
        existingCustomer.setDateOfBirth(customerDTO.getDateOfBirth());
        existingCustomer.setNic(customerDTO.getNic());
        existingCustomer.setEmail(customerDTO.getEmail());
        existingCustomer.setGender(customerDTO.getGender());

        // Update addresses
        existingCustomer.getAddresses().clear();
        if (customerDTO.getAddresses() != null) {
            for (AddressDTO addressDTO : customerDTO.getAddresses()) {
                Address address = new Address();
                address.setAddressLine1(addressDTO.getAddressLine1());
                address.setAddressLine2(addressDTO.getAddressLine2());
                address.setAddressType(addressDTO.getAddressType());
                address.setIsPrimary(addressDTO.getIsPrimary());
                address.setCustomer(existingCustomer);

                if (addressDTO.getCityId() != null) {
                    City city = cityRepository.findById(addressDTO.getCityId())
                            .orElseThrow(() -> new ResourceNotFoundException("City", "id", addressDTO.getCityId()));
                    address.setCity(city);
                } else {
                    throw new IllegalArgumentException("City must be selected for address");
                }

                existingCustomer.addAddress(address);
            }
        }

        // Update phone numbers
        existingCustomer.getPhoneNumbers().clear();
        if (customerDTO.getPhoneNumbers() != null) {
            for (PhoneNumberDTO phoneDTO : customerDTO.getPhoneNumbers()) {
                PhoneNumber phoneNumber = new PhoneNumber();
                phoneNumber.setPhoneNumber(phoneDTO.getPhoneNumber());
                phoneNumber.setPhoneType(phoneDTO.getPhoneType());
                phoneNumber.setIsPrimary(phoneDTO.getIsPrimary());
                phoneNumber.setCustomer(existingCustomer);
                existingCustomer.addPhoneNumber(phoneNumber);
            }
        }

        Customer updatedCustomer = customerRepository.save(existingCustomer);
        return convertToDTO(updatedCustomer);
    }

    @Override
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return convertToDTO(customer);
    }

    @Override
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        Page<Customer> customers = customerRepository.findAll(pageable);
        return customers.map(this::convertToDTO);
    }

    @Override
    public Page<CustomerDTO> searchCustomers(String keyword, Pageable pageable) {
        Page<Customer> customers = customerRepository.searchCustomers(keyword, pageable);
        return customers.map(this::convertToDTO);
    }

    @Override
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer", "id", id);
        }
        customerRepository.deleteById(id);
    }

    @Override
    public ImportResultDTO importCustomersFromExcel(MultipartFile file) {
        long importedCount = 0;
        long skippedDuplicates = 0;
        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream();
                Workbook workbook = StreamingReader.builder()
                        .rowCacheSize(200)
                        .bufferSize(4096)
                        .open(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            List<Customer> batch = new ArrayList<>();
            Set<String> batchNics = new HashSet<>();
            int rowIndex = 0;

            for (Row row : sheet) {
                if (rowIndex++ == 0) {
                    continue; // header
                }

                try {
                    Customer customer = parseCustomerFromRow(row);
                    if (customer.getNic() == null || customer.getNic().isEmpty()) {
                        addError(errors, row.getRowNum(), "NIC is required");
                        continue;
                    }

                    batch.add(customer);
                    batchNics.add(customer.getNic());

                    if (batch.size() >= BATCH_SIZE) {
                        BatchResult result = saveBatchIfNew(batch, batchNics);
                        importedCount += result.saved;
                        skippedDuplicates += result.skipped;
                        batch.clear();
                        batchNics.clear();
                        entityManager.clear();
                    }
                } catch (Exception e) {
                    addError(errors, row.getRowNum(), e.getMessage());
                }
            }

            if (!batch.isEmpty()) {
                BatchResult result = saveBatchIfNew(batch, batchNics);
                importedCount += result.saved;
                skippedDuplicates += result.skipped;
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage());
        }

        return new ImportResultDTO(importedCount, skippedDuplicates, errors);
    }

    private BatchResult saveBatchIfNew(List<Customer> batch, Set<String> batchNics) {
        Set<String> existing = customerRepository.findExistingNics(batchNics);
        List<Customer> toSave = batch.stream()
                .filter(c -> !existing.contains(c.getNic()))
                .collect(Collectors.toList());

        if (!toSave.isEmpty()) {
            saveCustomerBatch(toSave);
        }

        return new BatchResult(toSave.size(), existing.size());
    }

    private void addError(List<String> errors, int rowNum, String message) {
        if (errors.size() < ERROR_LIMIT) {
            errors.add("Row " + rowNum + ": " + message);
        }
    }

    @Override
    public byte[] exportCustomersToExcel() {
        List<Customer> customers = customerRepository.findAll();

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) { // Keep 100 rows in memory
            SXSSFSheet sheet = workbook.createSheet("Customers");
            sheet.trackAllColumnsForAutoSizing();

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "First Name", "Last Name", "Date of Birth", "NIC", "Email", "Gender",
                    "Primary Phone", "Primary Address" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Create data rows
            int rowNum = 1;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (Customer customer : customers) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(customer.getId());
                row.createCell(1).setCellValue(customer.getFirstName());
                row.createCell(2).setCellValue(customer.getLastName());
                Date dob = customer.getDateOfBirth();
                row.createCell(3).setCellValue(dob != null ? dateFormat.format(dob) : "");
                row.createCell(4).setCellValue(customer.getNic());
                row.createCell(5).setCellValue(customer.getEmail() != null ? customer.getEmail() : "");
                row.createCell(6).setCellValue(customer.getGender() != null ? customer.getGender() : "");

                // Get primary phone number
                Set<PhoneNumber> phoneNumbers = customer.getPhoneNumbers();
                String primaryPhone = phoneNumbers != null
                    ? phoneNumbers.stream()
                        .filter(p -> Boolean.TRUE.equals(p.getIsPrimary()))
                        .findFirst()
                        .map(PhoneNumber::getPhoneNumber)
                        .orElse("")
                    : "";
                row.createCell(7).setCellValue(primaryPhone);

                // Get primary address
                Set<Address> addresses = customer.getAddresses();
                String primaryAddress = addresses != null
                    ? addresses.stream()
                        .filter(a -> Boolean.TRUE.equals(a.getIsPrimary()))
                        .findFirst()
                        .map(Address::getAddressLine1)
                        .orElse("")
                    : "";
                row.createCell(8).setCellValue(primaryAddress);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file: " + e.getMessage());
        }
    }

    @Override
    public void addFamilyMember(Long customerId, Long familyMemberId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Customer familyMember = customerRepository.findById(familyMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", familyMemberId));

        customer.addFamilyMember(familyMember);
        customerRepository.save(customer);
    }

    @Override
    public void removeFamilyMember(Long customerId, Long familyMemberId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        Customer familyMember = customerRepository.findById(familyMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", familyMemberId));

        customer.removeFamilyMember(familyMember);
        customerRepository.save(customer);
    }

    private void saveCustomerBatch(List<Customer> customers) {
        customerRepository.saveAll(customers);
        customerRepository.flush();
    }

    private static class BatchResult {
        final long saved;
        final long skipped;

        BatchResult(long saved, long skipped) {
            this.saved = saved;
            this.skipped = skipped;
        }
    }

    private Customer parseCustomerFromRow(Row row) {
        Customer customer = new Customer();

        customer.setFirstName(getCellValueAsString(row.getCell(0)));
        customer.setLastName(getCellValueAsString(row.getCell(1)));

        // Parse date
        Cell dobCell = row.getCell(2);
        if (dobCell != null && dobCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dobCell)) {
            customer.setDateOfBirth(dobCell.getDateCellValue());
        }

        customer.setNic(getCellValueAsString(row.getCell(3)));
        customer.setEmail(getCellValueAsString(row.getCell(4)));
        customer.setGender(getCellValueAsString(row.getCell(5)));

        return customer;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            default:
                return null;
        }
    }

    private CustomerDTO convertToDTO(Customer customer) {
        CustomerDTO dto = modelMapper.map(customer, CustomerDTO.class);

        // Map addresses
        if (customer.getAddresses() != null) {
            Set<AddressDTO> addressDTOs = customer.getAddresses().stream()
                    .map(address -> {
                        AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);
                        addressDTO.setCityId(address.getCity().getId());
                        addressDTO.setCityName(address.getCity().getName());
                        return addressDTO;
                    })
                    .collect(Collectors.toSet());
            dto.setAddresses(addressDTOs);
        }

        // Map phone numbers
        if (customer.getPhoneNumbers() != null) {
            Set<PhoneNumberDTO> phoneDTOs = customer.getPhoneNumbers().stream()
                    .map(phone -> modelMapper.map(phone, PhoneNumberDTO.class))
                    .collect(Collectors.toSet());
            dto.setPhoneNumbers(phoneDTOs);
        }

        // Map family member IDs
        if (customer.getFamilyMembers() != null) {
            Set<Long> familyMemberIds = customer.getFamilyMembers().stream()
                    .map(Customer::getId)
                    .collect(Collectors.toSet());
            dto.setFamilyMemberIds(familyMemberIds);
        }

        return dto;
    }

    private Customer convertToEntity(CustomerDTO dto) {
        // Manually map basic fields to avoid ModelMapper creating child entities
        // without relations
        Customer customer = new Customer();
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setDateOfBirth(dto.getDateOfBirth());
        customer.setNic(dto.getNic());
        customer.setEmail(dto.getEmail());
        customer.setGender(dto.getGender());

        // Map addresses
        if (dto.getAddresses() != null) {
            for (AddressDTO addressDTO : dto.getAddresses()) {
                Address address = new Address();
                address.setAddressLine1(addressDTO.getAddressLine1());
                address.setAddressLine2(addressDTO.getAddressLine2());
                address.setAddressType(addressDTO.getAddressType());
                address.setIsPrimary(addressDTO.getIsPrimary());
                address.setCustomer(customer);

                if (addressDTO.getCityId() != null) {
                    City city = cityRepository.findById(addressDTO.getCityId())
                            .orElseThrow(() -> new ResourceNotFoundException("City", "id", addressDTO.getCityId()));
                    address.setCity(city);
                } else {
                    throw new IllegalArgumentException("City must be selected for address");
                }

                customer.addAddress(address);
            }
        }

        // Map phone numbers
        if (dto.getPhoneNumbers() != null) {
            for (PhoneNumberDTO phoneDTO : dto.getPhoneNumbers()) {
                PhoneNumber phoneNumber = new PhoneNumber();
                phoneNumber.setPhoneNumber(phoneDTO.getPhoneNumber());
                phoneNumber.setPhoneType(phoneDTO.getPhoneType());
                phoneNumber.setIsPrimary(phoneDTO.getIsPrimary());
                phoneNumber.setCustomer(customer);
                customer.addPhoneNumber(phoneNumber);
            }
        }

        return customer;
    }
}

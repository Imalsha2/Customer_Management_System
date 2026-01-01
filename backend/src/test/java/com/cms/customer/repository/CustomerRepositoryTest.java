package com.cms.customer.repository;

import com.cms.customer.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setFirstName("Jane");
        customer.setLastName("Smith");
        customer.setNic("987654321V");
        customer.setEmail("jane.smith@example.com");
        customer.setDateOfBirth(new Date());
        customer.setGender("FEMALE");
    }

    @Test
    void testSaveCustomer() {
        // Act
        Customer savedCustomer = customerRepository.save(customer);

        // Assert
        assertNotNull(savedCustomer.getId());
        assertEquals("Jane", savedCustomer.getFirstName());
        assertEquals("987654321V", savedCustomer.getNic());
    }

    @Test
    void testFindByNic() {
        // Arrange
        entityManager.persist(customer);
        entityManager.flush();

        // Act
        Optional<Customer> found = customerRepository.findByNic("987654321V");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Jane", found.get().getFirstName());
    }

    @Test
    void testExistsByNic() {
        // Arrange
        entityManager.persist(customer);
        entityManager.flush();

        // Act
        boolean exists = customerRepository.existsByNic("987654321V");

        // Assert
        assertTrue(exists);
    }

    @Test
    void testDeleteCustomer() {
        // Arrange
        Customer savedCustomer = entityManager.persist(customer);
        entityManager.flush();

        // Act
        customerRepository.deleteById(savedCustomer.getId());

        // Assert
        Optional<Customer> found = customerRepository.findById(savedCustomer.getId());
        assertFalse(found.isPresent());
    }
}

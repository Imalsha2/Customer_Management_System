package com.cms.customer.repository;

import com.cms.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByNic(String nic);

    boolean existsByNic(String nic);

    List<Customer> findByFirstNameContainingOrLastNameContaining(String firstName, String lastName);

    @Query("SELECT c FROM Customer c WHERE " +
            "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.nic) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Customer> searchCustomers(@Param("keyword") String keyword, Pageable pageable);

    List<Customer> findByDateOfBirthBetween(Date startDate, Date endDate);

    @Query("SELECT c FROM Customer c " +
            "LEFT JOIN FETCH c.addresses a " +
            "LEFT JOIN FETCH c.phoneNumbers p " +
            "WHERE c.id = :id")
    Optional<Customer> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Customer c " +
            "LEFT JOIN FETCH c.addresses " +
            "LEFT JOIN FETCH c.phoneNumbers " +
            "WHERE c.id IN :ids")
    List<Customer> findByIdInWithDetails(@Param("ids") List<Long> ids);
}

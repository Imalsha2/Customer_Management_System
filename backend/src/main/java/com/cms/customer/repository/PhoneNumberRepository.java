package com.cms.customer.repository;

import com.cms.customer.entity.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, Long> {

    List<PhoneNumber> findByCustomerId(Long customerId);

    List<PhoneNumber> findByCustomerIdAndIsPrimaryTrue(Long customerId);

    void deleteByCustomerId(Long customerId);
}

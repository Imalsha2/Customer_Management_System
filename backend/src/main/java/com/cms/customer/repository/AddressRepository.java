package com.cms.customer.repository;

import com.cms.customer.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByCustomerId(Long customerId);

    List<Address> findByCustomerIdAndIsPrimaryTrue(Long customerId);

    void deleteByCustomerId(Long customerId);
}

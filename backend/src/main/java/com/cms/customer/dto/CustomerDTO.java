package com.cms.customer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOfBirth;

    @NotBlank(message = "NIC is required")
    private String nic;

    @Email(message = "Invalid email format")
    private String email;

    private String gender;

    @Valid
    private Set<AddressDTO> addresses = new HashSet<>();

    @Valid
    private Set<PhoneNumberDTO> phoneNumbers = new HashSet<>();

    private Set<Long> familyMemberIds = new HashSet<>();
}

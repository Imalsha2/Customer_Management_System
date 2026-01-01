package com.cms.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDTO {
    private long importedCount;
    private long skippedDuplicates;
    private List<String> errors;
}

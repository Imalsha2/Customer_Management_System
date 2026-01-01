# Excel Import/Export Guide

## Import Template

### Required Columns (in order)

1. **First Name** (Required)
   - Type: Text
   - Max Length: 100 characters
   - Example: "Nimal"

2. **Last Name** (Required)
   - Type: Text
   - Max Length: 100 characters
   - Example: "Perera"

3. **Date of Birth** (Required)
   - Type: Date
   - Format: yyyy-MM-dd or Excel date
   - Example: "1990-05-15"

4. **NIC** (Required, Unique)
   - Type: Text
   - Max Length: 20 characters
   - Must be unique across all customers
   - Example: "900515123V"

5. **Email** (Optional)
   - Type: Text
   - Max Length: 150 characters
   - Must be valid email format
   - Example: "nimal.perera@email.com"

6. **Gender** (Optional)
   - Type: Text
   - Allowed values: MALE, FEMALE, OTHER
   - Example: "MALE"

### Sample Excel Template

| First Name | Last Name | Date of Birth | NIC        | Email                    | Gender |
|-----------|-----------|---------------|------------|--------------------------|--------|
| Nimal     | Perera    | 1990-05-15    | 900515123V | nimal.perera@email.com   | MALE   |
| Kamani    | Silva     | 1985-08-20    | 850820456V | kamani.silva@email.com   | FEMALE |
| Ruwan     | Fernando  | 1992-12-10    | 921210789V | ruwan.fernando@email.com | MALE   |

## Import Instructions

### Step 1: Prepare Excel File
1. Create new Excel file (.xlsx format)
2. Add header row with exact column names
3. Fill in customer data starting from row 2
4. Save file

### Step 2: Import via Web Interface
1. Go to Customer Management page
2. Click "Import" button
3. Select your Excel file
4. Wait for processing (may take time for large files)
5. Review success message

### Step 3: Verify Import
1. Check total number of imported customers
2. Search for specific customers
3. Verify data accuracy

## Import Behavior

### Duplicate Handling
- Customers with existing NIC will be **skipped**
- No error thrown, import continues
- Only new customers are added

### Error Handling
- Invalid date formats: Row skipped
- Missing required fields: Row skipped
- Invalid email: Row skipped
- Errors are logged but don't stop import

### Performance
- **Batch Size:** 100 records per batch
- **Memory Management:** Entity manager cleared after each batch
- **Large Files:** Can handle 1M+ records
- **Processing Time:** ~1000 records per second (approximate)

## Export Format

### Export Features
- All customers exported to single file
- Includes primary phone and address
- File name: `customers.xlsx`
- Format optimized for re-import

### Exported Columns
1. ID
2. First Name
3. Last Name
4. Date of Birth
5. NIC
6. Email
7. Gender
8. Primary Phone
9. Primary Address

## Best Practices

### For Large Imports (100K+ records)
1. **Split files:** Import in batches of 100K
2. **Check duplicates:** Use VLOOKUP to check NICs
3. **Test first:** Import small sample first
4. **Monitor:** Watch application logs during import
5. **Backup:** Backup database before large imports

### Data Quality
1. **Clean data:** Remove duplicates before import
2. **Validate dates:** Ensure proper date format
3. **Validate emails:** Use Excel data validation
4. **Standardize:** Use consistent gender values
5. **Trim spaces:** Remove leading/trailing spaces

### Performance Tips
1. **Use XLSX:** Don't use old XLS format
2. **Minimal formatting:** Remove cell colors, borders
3. **Close applications:** Free up memory
4. **Increase heap:** For very large files, increase JVM heap
   ```
   java -Xmx2G -jar application.jar
   ```

## Troubleshooting

### Import Fails Completely
- **Check file format:** Must be .xlsx
- **Check file size:** Max 100MB (configurable)
- **Check columns:** Must match template exactly
- **Check server logs:** Look for detailed errors

### Some Records Not Imported
- **Check NIC uniqueness:** Duplicates are skipped
- **Check required fields:** Missing data causes skip
- **Check date formats:** Invalid dates cause skip
- **Review logs:** Application logs show skipped rows

### Slow Import
- **Large file:** Normal for 500K+ records
- **Server resources:** Check CPU/memory
- **Database:** Check DB connection pool
- **Network:** Ensure stable connection

### Export Issues
- **File won't download:** Check browser settings
- **Incomplete data:** Verify database contains data
- **Large file:** May take time to generate

## Advanced: Excel Formulas for Data Preparation

### Remove Duplicates by NIC
```excel
=IF(COUNTIF($D$2:$D2,$D2)>1,"Duplicate","")
```

### Validate Date Format
```excel
=IF(ISDATE(C2),"Valid","Invalid")
```

### Validate Email
```excel
=IF(AND(FIND("@",E2)>0,FIND(".",E2)>FIND("@",E2)),"Valid","Invalid")
```

### Generate Random NIC (for testing)
```excel
=TEXT(RANDBETWEEN(850101,000101),"000000")&TEXT(RANDBETWEEN(100,999),"000")&"V"
```

## Sample Data Files

Create sample Excel files with these characteristics:

### Small Sample (100 records)
- Good for testing
- Quick import
- Verify functionality

### Medium Sample (10,000 records)
- Performance testing
- Realistic scenario
- Validate batch processing

### Large Sample (1,000,000 records)
- Stress testing
- Production simulation
- Memory/performance validation

## Support

For issues with import/export:
1. Check application logs
2. Verify Excel format
3. Test with small sample
4. Contact support team

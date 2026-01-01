# API Documentation - Customer Management System

## Base URL
```
http://localhost:8080/api
```

## Response Format

All API responses follow this structure:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

Error responses:
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

## Customer Endpoints

### 1. Get All Customers (Paginated)

**GET** `/customers`

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Page size
- `sortBy` (optional, default: "id") - Sort field
- `sortDir` (optional, default: "ASC") - Sort direction (ASC/DESC)

**Example Request:**
```
GET /api/customers?page=0&size=10&sortBy=firstName&sortDir=ASC
```

**Example Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "content": [
      {
        "id": 1,
        "firstName": "John",
        "lastName": "Doe",
        "dateOfBirth": "1990-05-15",
        "nic": "900515123V",
        "email": "john.doe@example.com",
        "gender": "MALE",
        "addresses": [],
        "phoneNumbers": [],
        "familyMemberIds": []
      }
    ],
    "totalPages": 5,
    "totalElements": 50,
    "number": 0,
    "size": 10
  }
}
```

### 2. Search Customers

**GET** `/customers/search`

**Query Parameters:**
- `keyword` (required) - Search keyword
- `page` (optional, default: 0)
- `size` (optional, default: 10)

**Example Request:**
```
GET /api/customers/search?keyword=john&page=0&size=10
```

### 3. Get Customer by ID

**GET** `/customers/{id}`

**Example Request:**
```
GET /api/customers/1
```

**Example Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "dateOfBirth": "1990-05-15",
    "nic": "900515123V",
    "email": "john.doe@example.com",
    "gender": "MALE",
    "addresses": [
      {
        "id": 1,
        "addressLine1": "No. 123, Main Street",
        "addressLine2": "Colombo 03",
        "cityId": 1,
        "cityName": "Colombo",
        "addressType": "HOME",
        "isPrimary": true
      }
    ],
    "phoneNumbers": [
      {
        "id": 1,
        "phoneNumber": "+94771234567",
        "phoneType": "MOBILE",
        "isPrimary": true
      }
    ],
    "familyMemberIds": [2, 3]
  }
}
```

### 4. Create Customer

**POST** `/customers`

**Request Body:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "dateOfBirth": "1992-08-20",
  "nic": "920820456V",
  "email": "jane.smith@example.com",
  "gender": "FEMALE",
  "addresses": [
    {
      "addressLine1": "No. 456, Park Road",
      "addressLine2": "Kandy",
      "cityId": 3,
      "addressType": "HOME",
      "isPrimary": true
    }
  ],
  "phoneNumbers": [
    {
      "phoneNumber": "+94777654321",
      "phoneType": "MOBILE",
      "isPrimary": true
    }
  ]
}
```

**Example Response:**
```json
{
  "success": true,
  "message": "Customer created successfully",
  "data": {
    "id": 10,
    "firstName": "Jane",
    "lastName": "Smith",
    ...
  }
}
```

### 5. Update Customer

**PUT** `/customers/{id}`

**Request Body:** Same as Create Customer

**Example Response:**
```json
{
  "success": true,
  "message": "Customer updated successfully",
  "data": { ... }
}
```

### 6. Delete Customer

**DELETE** `/customers/{id}`

**Example Response:**
```json
{
  "success": true,
  "message": "Customer deleted successfully",
  "data": null
}
```

### 7. Import Customers from Excel

**POST** `/customers/import`

**Content-Type:** `multipart/form-data`

**Form Data:**
- `file` - Excel file (.xlsx)

**Excel Format:**
| First Name | Last Name | Date of Birth | NIC | Email | Gender |
|------------|-----------|---------------|-----|-------|--------|
| John | Doe | 1990-05-15 | 900515123V | john@example.com | MALE |

**Example Response:**
```json
{
  "success": true,
  "message": "Imported 1000 customers successfully",
  "data": [ ... ]
}
```

### 8. Export Customers to Excel

**GET** `/customers/export`

**Response:** Excel file download

### 9. Add Family Member

**POST** `/customers/{customerId}/family-members/{familyMemberId}`

**Example Request:**
```
POST /api/customers/1/family-members/2
```

**Example Response:**
```json
{
  "success": true,
  "message": "Family member added successfully",
  "data": null
}
```

### 10. Remove Family Member

**DELETE** `/customers/{customerId}/family-members/{familyMemberId}`

**Example Response:**
```json
{
  "success": true,
  "message": "Family member removed successfully",
  "data": null
}
```

## Master Data Endpoints

### 1. Get All Countries

**GET** `/countries`

**Example Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": 1,
      "name": "Sri Lanka",
      "code": "LK"
    },
    {
      "id": 2,
      "name": "India",
      "code": "IN"
    }
  ]
}
```

### 2. Get All Cities

**GET** `/cities`

**Example Response:**
```json
{
  "success": true,
  "message": "Operation successful",
  "data": [
    {
      "id": 1,
      "name": "Colombo",
      "countryId": 1,
      "countryName": "Sri Lanka",
      "zipCode": "00100"
    }
  ]
}
```

### 3. Get Cities by Country

**GET** `/cities/country/{countryId}`

**Example Request:**
```
GET /api/cities/country/1
```

## Error Codes

| Status Code | Description |
|-------------|-------------|
| 200 | OK - Success |
| 201 | Created - Resource created successfully |
| 400 | Bad Request - Invalid input |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Duplicate resource (e.g., NIC already exists) |
| 500 | Internal Server Error |

## Validation Errors

**Example:**
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "firstName": "First name is required",
    "nic": "NIC is required",
    "email": "Invalid email format"
  }
}
```

## Notes

- All dates are in `yyyy-MM-dd` format
- NIC must be unique across all customers
- Maximum file upload size: 100MB
- Batch processing handles large imports efficiently
- Family member relationships are bidirectional

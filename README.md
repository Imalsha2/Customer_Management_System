# Customer Management System

Full-stack enterprise Customer Management System built with Spring Boot, React, and MariaDB.

## ✍️ About & Approach (solo build)
- Built end-to-end (backend + frontend + DB) by me for interview-ready demonstration.
- Focused on clean layering: controller → service → repository → entity/DTO with ModelMapper.
- Prioritized operability: sensible defaults, seed data, and repeatable setup scripts.

## 🧭 Architecture at a Glance
- **Backend:** Spring Boot (REST), MariaDB, JPA/Hibernate, POI for streaming Excel import/export.
- **Frontend:** React + Axios + React-Bootstrap; simple service layer wrapping API calls.
- **Packaging:** Maven multi-module style folder (backend + frontend sibling), with environment-specific properties.

## 🧠 Design Decisions & Trade-offs
- **Streaming Excel (SXSSF):** avoids OOM for 1M+ rows at the cost of slightly slower writes.
- **NIC uniqueness:** enforced in service/import to keep DB clean; chosen over DB unique constraint to allow controlled skips with feedback.
- **Batch size 1000:** balance between JDBC round-trips and memory pressure during bulk import.
- **Open-in-view left on (default):** acceptable for small demo; can be disabled with dedicated DTO projections if needed.
- **Context path `/api`:** keeps backend neatly namespaced for proxying from React dev server.

## 🔧 What I’d Improve Next
- Add pagination/sort params to export for partial dumps.
- Add integration tests around import/export happy-path and edge cases.
- Introduce request validation on DTOs (e.g., phone/email formats) and consistent error codes.
- Docker Compose for DB + backend + frontend to simplify first-time spin-up.

## ✅ Testing & Verification (done manually here)
- Backend: `mvn test` (unit set), `mvn spring-boot:run` smoke with sample data.
- Frontend: `npm start` smoke; verified customer list + CRUD + export after null-safety fix.
- Export: null-safe primary flags added in `CustomerServiceImpl` to avoid NPE on missing primary markers.

## 🎙️ Interview Highlights (talking points)
- Bulk import/export: streaming POI (SXSSF), batch writes, and duplicate NIC skip with summary.
- Data model: customers with addresses, phone numbers, and family links; master data for cities/countries.
- Error handling: global handler returning structured API responses; CORS configured for localhost dev.
- Performance: batch size tuning, HikariCP defaults, and lazy relations to keep memory low during bulk ops.

## 🚀 Technologies Used

### Backend
- **Java 8**
- **Spring Boot 2.7.18**
- **Spring Data JPA** (Hibernate)
- **MariaDB** - Relational Database
- **Apache POI** - Excel Import/Export
- **Maven** - Dependency Management
- **JUnit 5** - Unit Testing
- **Lombok** - Reduce Boilerplate Code
- **ModelMapper** - DTO Mapping

### Frontend
- **React 18.2**
- **React Router** - Navigation
- **Axios** - HTTP Client
- **React Bootstrap** - UI Components
- **React Icons** - Icon Library
- **React Toastify** - Notifications

## 📋 Features

### Core Features
✅ **CRUD Operations** - Create, Read, Update, Delete customers
✅ **Search & Filter** - Advanced customer search functionality
✅ **Pagination** - Efficient data loading with pagination
✅ **Excel Import/Export** - Bulk operations supporting 1M+ records
✅ **Family Relationships** - Link customers as family members
✅ **Multiple Addresses** - Support for multiple addresses per customer
✅ **Multiple Phone Numbers** - Support for multiple phone numbers
✅ **Master Data** - Cities and Countries management
✅ **NIC Validation** - Unique National Identity Card validation
✅ **RESTful API** - Well-structured REST endpoints
✅ **Exception Handling** - Global exception handling
✅ **CORS Support** - Cross-Origin Resource Sharing enabled
✅ **Batch Processing** - Optimized database operations
✅ **Unit Tests** - Comprehensive test coverage

## 🏗️ Project Structure

```
Customer_Management_System/
│
├── backend/                          # Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/cms/customer/
│   │   │   │   ├── entity/          # JPA Entities
│   │   │   │   ├── repository/      # Spring Data Repositories
│   │   │   │   ├── service/         # Business Logic
│   │   │   │   ├── controller/      # REST Controllers
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── exception/       # Custom Exceptions
│   │   │   │   └── config/          # Configuration Classes
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── schema.sql       # Database Schema
│   │   │       └── data.sql         # Sample Data
│   │   └── test/                    # JUnit Tests
│   └── pom.xml                      # Maven Dependencies
│
└── react-frontend/                  # React Frontend
    ├── public/
    ├── src/
    │   ├── components/              # React Components
    │   ├── services/                # Axios API Services
    │   ├── App.js                   # Main App Component
    │   └── index.js                 # Entry Point
    └── package.json                 # NPM Dependencies
```

## 🛠️ Installation & Setup

### Prerequisites
- **Java 8 JDK** or higher
- **Maven 3.6+**
- **Node.js 16+** and npm
- **MariaDB 10.5+** or MySQL 8.0+

### Database Setup

1. **Install MariaDB/MySQL**

2. **Create Database**
```sql
CREATE DATABASE customer_management_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **Update Database Configuration**

Edit `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/customer_management_db
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

4. **Run Database Scripts**

The application will automatically create tables on first run (DDL auto-update enabled).

To manually run DDL and sample data:
```bash
# DDL - Creates all tables (customers, addresses, phone_numbers, cities, countries, etc.)
mysql -u root -p customer_management_db < backend/src/main/resources/schema.sql

# DML - Inserts sample countries, cities, and test customers
mysql -u root -p customer_management_db < backend/src/main/resources/data.sql
```

**Database Scripts Location:**
- **DDL:** `backend/src/main/resources/schema.sql` (table definitions)
- **DML:** `backend/src/main/resources/data.sql` (sample data)

### Backend Setup

1. **Navigate to backend directory**
```bash
cd backend
```

2. **Install dependencies and build**
```bash
mvn clean install
```

3. **Run the application**
```bash
mvn spring-boot:run
```

Or run the JAR file:
```bash
java -jar target/customer-management-system-1.0.0.jar
```

Backend will run on: `http://localhost:8080/api`

### Frontend Setup

1. **Navigate to react-frontend directory**
```bash
cd react-frontend
```

2. **Install dependencies**
```bash
npm install
```

3. **Start development server**
```bash
npm start
```

Frontend will run on: `http://localhost:3000`

## 📡 API Endpoints

### Customer Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/customers` | Get all customers (paginated) |
| GET | `/api/customers/{id}` | Get customer by ID |
| GET | `/api/customers/search?keyword={keyword}` | Search customers |
| POST | `/api/customers` | Create new customer |
| PUT | `/api/customers/{id}` | Update customer |
| DELETE | `/api/customers/{id}` | Delete customer |
| POST | `/api/customers/import` | **Bulk import** customers from Excel (up to 1M rows) |
| GET | `/api/customers/export` | Export customers to Excel |
| POST | `/api/customers/{customerId}/family-members/{familyMemberId}` | Add family member |
| DELETE | `/api/customers/{customerId}/family-members/{familyMemberId}` | Remove family member |

### Bulk Import Details

**Endpoint:** `POST /api/customers/import`

**Features:**
- Supports up to **1,000,000 rows** per import
- Memory-efficient **streaming** processing (no full file load)
- **Batch processing** (1000 records per batch)
- Automatic **duplicate detection** via NIC (skips existing NICs)
- Returns summary with imported/skipped counts and first 100 errors

**Request:**
- Content-Type: `multipart/form-data`
- Parameter: `file` (Excel .xlsx file, max 50MB)

**Excel Format:**
| Column | Required | Format | Example |
|--------|----------|--------|---------|
| firstName | Yes | Text | John |
| lastName | Yes | Text | Doe |
| nic | Yes | Text (unique) | 1234567890V |
| dateOfBirth | No | yyyy-MM-dd or Excel date | 1990-05-15 |
| gender | No | MALE/FEMALE | MALE |
| email | No | Email | john@example.com |

**Response:** (ImportResultDTO)
```json
{
  "importedCount": 9500,
  "skippedDuplicates": 500,
  "errors": [
    "Row 123: Missing required field 'firstName'",
    "Row 456: Duplicate NIC '1234567890V' (skipped)"
  ]
}
```
- Maximum 100 error messages returned
- Successful records are saved even if some rows fail

### Master Data Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/countries` | Get all countries |
| GET | `/api/cities` | Get all cities |
| GET | `/api/cities/country/{countryId}` | Get cities by country |

## 📊 Database Schema

### Main Tables
- **customers** - Customer master data
- **addresses** - Customer addresses
- **phone_numbers** - Customer phone numbers
- **customer_family_members** - Family relationships
- **cities** - Master data for cities
- **countries** - Master data for countries

### Key Relationships
- Customer → Addresses (One-to-Many)
- Customer → PhoneNumbers (One-to-Many)
- Customer → FamilyMembers (Many-to-Many)
- Address → City (Many-to-One)
- City → Country (Many-to-One)

## 🧪 Running Tests

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd react-frontend
npm test
```

## 📦 Building for Production

### Backend
```bash
cd backend
mvn clean package
```
JAR file will be created in `target/` directory

### Frontend
```bash
cd react-frontend
npm run build
```
Production files will be in `build/` directory

## 🔧 Configuration

### Backend Configuration (`application.properties`)
```properties
# Server
server.port=8080
server.servlet.context-path=/api

# Database
spring.datasource.url=jdbc:mariadb://localhost:3306/customer_management_db
spring.datasource.username=root
spring.datasource.password=root

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# File Upload
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# Batch Processing
spring.jpa.properties.hibernate.jdbc.batch_size=100
```

### Frontend Configuration
Create `.env` file in react-frontend:
```
REACT_APP_API_URL=http://localhost:8080/api
```

## 🎯 Performance Optimizations

- **Batch Processing** - Inserts/updates in batches of 100 records
- **Lazy Loading** - Fetch relationships on demand
- **Streaming Excel** - Use SXSSF for large exports
- **Connection Pooling** - HikariCP with optimized settings
- **Indexed Queries** - Database indexes on NIC, names, DOB
- **Entity Manager Clearing** - Prevent memory issues during bulk operations

## 🔐 Security Considerations

- Input validation using Bean Validation
- SQL injection prevention via JPA/Hibernate
- CORS configuration for cross-origin requests
- Exception handling without exposing sensitive data

## 📝 Sample Customer JSON

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "dateOfBirth": "1990-05-15",
  "nic": "900515123V",
  "email": "john.doe@example.com",
  "gender": "MALE",
  "addresses": [
    {
      "addressLine1": "No. 123, Main Street",
      "addressLine2": "Colombo 03",
      "cityId": 1,
      "addressType": "HOME",
      "isPrimary": true
    }
  ],
  "phoneNumbers": [
    {
      "phoneNumber": "+94771234567",
      "phoneType": "MOBILE",
      "isPrimary": true
    }
  ]
}
```

## 🐛 Troubleshooting

### Common Issues

**1. Database Connection Error**
- Verify MariaDB is running
- Check database credentials in `application.properties`
- Ensure database exists

**2. Port Already in Use**
- Change port in `application.properties` (backend)
- Change port in `package.json` proxy setting (frontend)

**3. Excel Import Fails**
- Ensure file format is **.xlsx** (not .xls)
- Check file size is under **50MB**
- Verify columns match template: `firstName`, `lastName`, `nic`, `dateOfBirth` (yyyy-MM-dd), `gender`, `email`
- Import supports up to **1,000,000 rows** with streaming processing
- Duplicate NICs are automatically skipped; check response summary for details

**4. Frontend Can't Connect to Backend**
- Ensure backend is running on port 8080
- Check CORS configuration
- Verify proxy setting in package.json

## 👥 Contributors

- Imalsha 

## 📄 License

This project is for educational purposes.

## 🙏 Acknowledgments

- Spring Boot Documentation
- React Documentation
- Apache POI Project
- Bootstrap Team

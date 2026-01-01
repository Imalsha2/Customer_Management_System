# Quick Start Guide - Customer Management System

## 🚀 5-Minute Setup

### Prerequisites Check
- [ ] Java 8 installed (`java -version`)
- [ ] Maven installed (`mvn -version`)
- [ ] Node.js installed (`node -v`)
- [ ] MariaDB/MySQL running

### Step 1: Database Setup (2 minutes)

```bash
# Login to MariaDB
mysql -u root -p

# Create database
CREATE DATABASE customer_management_db;

# Exit
exit;
```

### Step 2: Backend Setup (2 minutes)

```powershell
# Navigate to backend
cd backend

# Update database credentials
# Edit: src/main/resources/application.properties
# Change: spring.datasource.username and spring.datasource.password

# Build and run
mvn spring-boot:run
```

Backend will start on `http://localhost:8080/api`

### Step 3: Frontend Setup (1 minute)

Open **new terminal**:

```powershell
# Navigate to frontend
cd react-frontend

# Install dependencies (first time only)
npm install

# Start development server
npm start
```

Frontend will open automatically on `http://localhost:3000`

## ✅ Verification

1. **Backend Health Check:**
   - Visit: `http://localhost:8080/api/countries`
   - Should see list of countries

2. **Frontend Check:**
   - Visit: `http://localhost:3000`
   - Should see Customer Management page

3. **Create Test Customer:**
   - Click "Add Customer" button
   - Fill form and submit
   - Verify customer appears in list

## 🎯 Next Steps

1. **Import Sample Data:**
   - Create Excel file with sample customers
   - Click "Import" button
   - Select file and upload

2. **Test Search:**
   - Type in search box
   - Click "Search"

3. **Export Data:**
   - Click "Export" button
   - Excel file downloads

## 📋 Common Commands

### Backend

```powershell
# Run tests
mvn test

# Build JAR
mvn clean package

# Run JAR
java -jar target/customer-management-system-1.0.0.jar
```

### Frontend

```powershell
# Run tests
npm test

# Build for production
npm run build

# Run production build
serve -s build
```

## 🔧 Troubleshooting

### Backend won't start
```powershell
# Check if port 8080 is in use
netstat -ano | findstr :8080

# Kill process if needed
taskkill /PID <PID> /F

# Or change port in application.properties
server.port=8081
```

### Frontend can't connect to backend
```powershell
# Check backend is running
# Visit: http://localhost:8080/api/countries

# If using different port, update package.json:
"proxy": "http://localhost:8081"
```

### Database connection error
```properties
# Verify in application.properties:
spring.datasource.url=jdbc:mariadb://localhost:3306/customer_management_db
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

## 📁 Project Structure at a Glance

```
Customer_Management_System/
├── backend/                 # Spring Boot API
│   ├── src/main/java/      # Java source code
│   ├── src/main/resources/ # Config files
│   └── pom.xml            # Dependencies
│
├── react-frontend/         # React UI
│   ├── src/               # React components
│   ├── public/            # Static files
│   └── package.json       # Dependencies
│
├── README.md              # Full documentation
├── API_DOCUMENTATION.md   # API reference
└── EXCEL_IMPORT_GUIDE.md # Import/Export guide
```

## 🌟 Key Features to Try

1. **CRUD Operations** - Create, view, edit, delete customers
2. **Search** - Find customers by name, NIC, email
3. **Pagination** - Navigate through customer pages
4. **Excel Import** - Bulk import customers
5. **Excel Export** - Download all customers
6. **Family Members** - Link related customers

## 📞 Support

Need help? Check:
1. **README.md** - Complete documentation
2. **API_DOCUMENTATION.md** - API endpoints
3. **EXCEL_IMPORT_GUIDE.md** - Import/Export help

## 🎉 You're Ready!

The application is now running. Start adding customers!

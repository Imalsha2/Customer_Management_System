import axiosInstance from './axiosConfig';

const CustomerService = {
  // Get all customers with pagination
  getAllCustomers: (page = 0, size = 10, sortBy = 'id', sortDir = 'ASC') => {
    return axiosInstance.get('/customers', {
      params: { page, size, sortBy, sortDir }
    });
  },

  // Search customers
  searchCustomers: (keyword, page = 0, size = 10) => {
    return axiosInstance.get('/customers/search', {
      params: { keyword, page, size }
    });
  },

  // Get customer by ID
  getCustomerById: (id) => {
    return axiosInstance.get(`/customers/${id}`);
  },

  // Create new customer
  createCustomer: (customerData) => {
    return axiosInstance.post('/customers', customerData);
  },

  // Update customer
  updateCustomer: (id, customerData) => {
    return axiosInstance.put(`/customers/${id}`, customerData);
  },

  // Delete customer
  deleteCustomer: (id) => {
    return axiosInstance.delete(`/customers/${id}`);
  },

  // Import customers from Excel
  importCustomers: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    
    return axiosInstance.post('/customers/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  },

  // Export customers to Excel
  exportCustomers: () => {
    return axiosInstance.get('/customers/export', {
      responseType: 'blob'
    });
  },

  // Add family member
  addFamilyMember: (customerId, familyMemberId) => {
    return axiosInstance.post(`/customers/${customerId}/family-members/${familyMemberId}`);
  },

  // Remove family member
  removeFamilyMember: (customerId, familyMemberId) => {
    return axiosInstance.delete(`/customers/${customerId}/family-members/${familyMemberId}`);
  }
};

export default CustomerService;

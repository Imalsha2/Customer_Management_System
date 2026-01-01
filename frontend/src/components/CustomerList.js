import React, { useState, useEffect } from 'react';
import { Container, Table, Button, Form, Row, Col, Modal, Alert, Spinner } from 'react-bootstrap';
import { FaEdit, FaTrash, FaPlus, FaFileExcel, FaSearch } from 'react-icons/fa';
import CustomerService from '../services/CustomerService';
import MasterDataService from '../services/MasterDataService';
import { toast } from 'react-toastify';

function CustomerList() {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [cities, setCities] = useState([]);
  const [countries, setCountries] = useState([]);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    dateOfBirth: '',
    nic: '',
    email: '',
    gender: 'MALE',
    addresses: [],
    phoneNumbers: []
  });

  useEffect(() => {
    loadCustomers();
    loadMasterData();
  }, [currentPage]);

  const loadCustomers = async () => {
    setLoading(true);
    try {
      const response = await CustomerService.getAllCustomers(currentPage, 10);
      if (response.success) {
        setCustomers(response.data.content);
        setTotalPages(response.data.totalPages);
      }
    } catch (error) {
      toast.error('Failed to load customers');
    } finally {
      setLoading(false);
    }
  };

  const loadMasterData = async () => {
    try {
      const [countriesRes, citiesRes] = await Promise.all([
        MasterDataService.getAllCountries(),
        MasterDataService.getAllCities()
      ]);
      
      if (countriesRes.success) setCountries(countriesRes.data);
      if (citiesRes.success) setCities(citiesRes.data);
    } catch (error) {
      console.error('Failed to load master data', error);
    }
  };

  const handleSearch = async () => {
    if (!searchKeyword.trim()) {
      loadCustomers();
      return;
    }

    setLoading(true);
    try {
      const response = await CustomerService.searchCustomers(searchKeyword, currentPage, 10);
      if (response.success) {
        setCustomers(response.data.content);
        setTotalPages(response.data.totalPages);
      }
    } catch (error) {
      toast.error('Search failed');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this customer?')) {
      try {
        await CustomerService.deleteCustomer(id);
        toast.success('Customer deleted successfully');
        loadCustomers();
      } catch (error) {
        toast.error('Failed to delete customer');
      }
    }
  };

  const handleEdit = (customer) => {
    setSelectedCustomer(customer);
    setFormData({
      firstName: customer.firstName,
      lastName: customer.lastName,
      dateOfBirth: customer.dateOfBirth,
      nic: customer.nic,
      email: customer.email || '',
      gender: customer.gender || 'MALE',
      addresses: customer.addresses || [],
      phoneNumbers: customer.phoneNumbers || []
    });
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      if (selectedCustomer) {
        await CustomerService.updateCustomer(selectedCustomer.id, formData);
        toast.success('Customer updated successfully');
      } else {
        await CustomerService.createCustomer(formData);
        toast.success('Customer created successfully');
      }
      
      setShowModal(false);
      resetForm();
      loadCustomers();
    } catch (error) {
      toast.error(error.message || 'Operation failed');
    }
  };

  const resetForm = () => {
    setSelectedCustomer(null);
    setFormData({
      firstName: '',
      lastName: '',
      dateOfBirth: '',
      nic: '',
      email: '',
      gender: 'MALE',
      addresses: [],
      phoneNumbers: []
    });
  };

  const handleExport = async () => {
    try {
      const response = await CustomerService.exportCustomers();
      const url = window.URL.createObjectURL(new Blob([response]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'customers.xlsx');
      document.body.appendChild(link);
      link.click();
      link.remove();
      toast.success('Customers exported successfully');
    } catch (error) {
      toast.error('Export failed');
    }
  };

  const handleImport = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    try {
      setLoading(true);
      await CustomerService.importCustomers(file);
      toast.success('Customers imported successfully');
      loadCustomers();
    } catch (error) {
      toast.error('Import failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container className="mt-4">
      <h2>Customer Management</h2>
      
      <Row className="mb-3">
        <Col md={6}>
          <Form.Group className="d-flex">
            <Form.Control
              type="text"
              placeholder="Search customers..."
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
            />
            <Button variant="primary" className="ms-2" onClick={handleSearch}>
              <FaSearch /> Search
            </Button>
          </Form.Group>
        </Col>
        <Col md={6} className="text-end">
          <Button variant="success" className="me-2" onClick={() => setShowModal(true)}>
            <FaPlus /> Add Customer
          </Button>
          <Button variant="info" className="me-2" onClick={handleExport}>
            <FaFileExcel /> Export
          </Button>
          <label htmlFor="import-file" className="btn btn-warning">
            <FaFileExcel /> Import
          </label>
          <input
            id="import-file"
            type="file"
            accept=".xlsx,.xls"
            style={{ display: 'none' }}
            onChange={handleImport}
          />
        </Col>
      </Row>

      {loading ? (
        <div className="text-center">
          <Spinner animation="border" />
        </div>
      ) : (
        <>
          <Table striped bordered hover responsive>
            <thead>
              <tr>
                <th>ID</th>
                <th>First Name</th>
                <th>Last Name</th>
                <th>NIC</th>
                <th>Email</th>
                <th>Gender</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {customers.length === 0 ? (
                <tr>
                  <td colSpan="7" className="text-center">No customers found</td>
                </tr>
              ) : (
                customers.map(customer => (
                  <tr key={customer.id}>
                    <td>{customer.id}</td>
                    <td>{customer.firstName}</td>
                    <td>{customer.lastName}</td>
                    <td>{customer.nic}</td>
                    <td>{customer.email}</td>
                    <td>{customer.gender}</td>
                    <td>
                      <Button
                        variant="warning"
                        size="sm"
                        className="me-2"
                        onClick={() => handleEdit(customer)}
                      >
                        <FaEdit /> Edit
                      </Button>
                      <Button
                        variant="danger"
                        size="sm"
                        onClick={() => handleDelete(customer.id)}
                      >
                        <FaTrash /> Delete
                      </Button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </Table>

          <div className="d-flex justify-content-between">
            <Button
              disabled={currentPage === 0}
              onClick={() => setCurrentPage(currentPage - 1)}
            >
              Previous
            </Button>
            <span>Page {currentPage + 1} of {totalPages}</span>
            <Button
              disabled={currentPage >= totalPages - 1}
              onClick={() => setCurrentPage(currentPage + 1)}
            >
              Next
            </Button>
          </div>
        </>
      )}

      {/* Customer Form Modal */}
      <Modal show={showModal} onHide={() => { setShowModal(false); resetForm(); }} size="lg">
        <Modal.Header closeButton>
          <Modal.Title>{selectedCustomer ? 'Edit Customer' : 'Add Customer'}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form onSubmit={handleSubmit}>
            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>First Name *</Form.Label>
                  <Form.Control
                    type="text"
                    required
                    value={formData.firstName}
                    onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Last Name *</Form.Label>
                  <Form.Control
                    type="text"
                    required
                    value={formData.lastName}
                    onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                  />
                </Form.Group>
              </Col>
            </Row>

            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>NIC *</Form.Label>
                  <Form.Control
                    type="text"
                    required
                    value={formData.nic}
                    onChange={(e) => setFormData({ ...formData, nic: e.target.value })}
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Date of Birth *</Form.Label>
                  <Form.Control
                    type="date"
                    required
                    value={formData.dateOfBirth}
                    onChange={(e) => setFormData({ ...formData, dateOfBirth: e.target.value })}
                  />
                </Form.Group>
              </Col>
            </Row>

            <Row>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Email</Form.Label>
                  <Form.Control
                    type="email"
                    value={formData.email}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  />
                </Form.Group>
              </Col>
              <Col md={6}>
                <Form.Group className="mb-3">
                  <Form.Label>Gender</Form.Label>
                  <Form.Select
                    value={formData.gender}
                    onChange={(e) => setFormData({ ...formData, gender: e.target.value })}
                  >
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                  </Form.Select>
                </Form.Group>
              </Col>
            </Row>

            <div className="text-end">
              <Button variant="secondary" className="me-2" onClick={() => { setShowModal(false); resetForm(); }}>
                Cancel
              </Button>
              <Button variant="primary" type="submit">
                {selectedCustomer ? 'Update' : 'Create'}
              </Button>
            </div>
          </Form>
        </Modal.Body>
      </Modal>
    </Container>
  );
}

export default CustomerList;

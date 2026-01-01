import React, { useState, useEffect, useCallback } from 'react';
import { Container, Table, Button, Form, Row, Col, Modal, Spinner, Card, Badge } from 'react-bootstrap';
import { FaEdit, FaTrash, FaPlus, FaFileExcel, FaSearch, FaEye } from 'react-icons/fa';
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
  const [showViewModal, setShowViewModal] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [viewCustomer, setViewCustomer] = useState(null);
  const [cities, setCities] = useState([]);
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
  const [submitting, setSubmitting] = useState(false);

  const loadCustomers = useCallback(async () => {
    setLoading(true);
    try {
      const response = await CustomerService.getAllCustomers(currentPage, 10);
      if (response.success) {
        setCustomers(response.data.content);
        setTotalPages(response.data.totalPages);
      }
    } catch (error) {
      toast.error('Unable to load customers. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [currentPage]);

  useEffect(() => {
    loadCustomers();
    loadMasterData();
  }, [loadCustomers]);

  const loadMasterData = async () => {
    try {
      const citiesRes = await MasterDataService.getAllCities();
      if (citiesRes.success) setCities(citiesRes.data);
    } catch (error) {
      console.error('Failed to load master data', error);
      toast.error('Could not load master data. Please retry.');
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
      toast.error('Search failed. Please try again.');
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
        toast.error('Unable to delete customer. Please try again.');
      }
    }
  };

  const handleView = async (customerId) => {
    try {
      const response = await CustomerService.getCustomerById(customerId);
      if (response.success) {
        setViewCustomer(response.data);
        setShowViewModal(true);
      }
    } catch (error) {
      toast.error('Unable to load customer details. Please try again.');
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
    setSubmitting(true);

    try {
      // Filter out empty phone numbers and addresses
      const normalizedAddresses = formData.addresses
        .map(a => ({
          ...a,
          cityId: a.cityId !== null && a.cityId !== undefined && a.cityId !== ''
            ? Number(a.cityId)
            : null
        }))
        .filter(a => {
          return a.addressLine1 && a.addressLine1.trim() && a.cityId && !Number.isNaN(a.cityId) && a.cityId > 0;
        });

      const normalizedPhones = formData.phoneNumbers
        .map(p => ({ ...p, phoneNumber: p.phoneNumber ? p.phoneNumber.trim() : '' }))
        .filter(p => p.phoneNumber);

      const dataToSubmit = {
        ...formData,
        phoneNumbers: normalizedPhones,
        addresses: normalizedAddresses
      };

      if (selectedCustomer) {
        await CustomerService.updateCustomer(selectedCustomer.id, dataToSubmit);
        toast.success('Customer updated successfully');
      } else {
        await CustomerService.createCustomer(dataToSubmit);
        toast.success('Customer created successfully');
      }
      
      setShowModal(false);
      resetForm();
      loadCustomers();
    } catch (error) {
      toast.error(error.message || 'Save failed. Please try again.');
    } finally {
      setSubmitting(false);
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
      const res = await CustomerService.exportCustomers();
      const blob = res.data || res;
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');

      // Try to use filename from header; fallback to default
      const disposition = res.headers ? res.headers['content-disposition'] : null;
      let filename = 'customers.xlsx';
      if (disposition && disposition.indexOf('filename=') !== -1) {
        filename = disposition.split('filename=')[1].replace(/"/g, '').trim();
      }

      link.href = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      toast.success('Customers exported successfully');
    } catch (error) {
      const status = error?.status || error?.statusCode || error?.response?.status;
      const message = status ? `Export failed (HTTP ${status})` : 'Export failed. Check that the backend is running.';
      toast.error(message);
    }
  };

  const handleDownloadTemplate = () => {
    // Create template Excel with headers
    const headers = ['firstName', 'lastName', 'dateOfBirth', 'nic', 'email', 'gender'];
    const sampleData = [
      ['John', 'Doe', '1990-05-15', '900515123V', 'john.doe@email.com', 'MALE'],
      ['Jane', 'Smith', '1992-08-20', '920820456V', 'jane.smith@email.com', 'FEMALE']
    ];
    
    // Create CSV content
    let csvContent = headers.join(',') + '\n';
    csvContent += sampleData.map(row => row.join(',')).join('\n');
    
    // Create blob and download
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', 'customer_import_template.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    toast.info('Template downloaded. Fill with your data and upload as Excel (.xlsx)');
  };

  const handleImport = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // Validate file type
    if (!file.name.endsWith('.xlsx') && !file.name.endsWith('.xls')) {
      toast.error('Please select an Excel file (.xlsx or .xls)');
      e.target.value = ''; // Reset file input
      return;
    }

    try {
      setLoading(true);
      const response = await CustomerService.importCustomers(file);
      
      // Show detailed success message with counts
      const { importedCount, skippedDuplicates, errors } = response.data || response;
      
      let message = `Successfully imported ${importedCount} customers`;
      if (skippedDuplicates > 0) {
        message += ` (${skippedDuplicates} duplicates skipped)`;
      }
      
      toast.success(message, { autoClose: 5000 });
      
      // Show errors if any
      if (errors && errors.length > 0) {
        const errorCount = errors.length;
        toast.warning(`${errorCount} rows had errors. Check console for details.`, { autoClose: 8000 });
        console.error('Import errors:', errors);
      }
      
      loadCustomers();
      e.target.value = ''; // Reset file input
    } catch (error) {
      console.error('Import error:', error);
      
      // Extract detailed error message
      let errorMessage = 'Import failed. Please review the file and try again.';
      
      if (error.response) {
        // Backend returned error response
        const { status, data } = error.response;
        
        if (data && data.message) {
          errorMessage = data.message;
        } else if (status === 400) {
          errorMessage = 'Invalid file format. Please check your Excel file.';
        } else if (status === 413) {
          errorMessage = 'File too large. Maximum size is 100MB.';
        } else if (status === 500) {
          errorMessage = 'Server error while processing file. Check file format.';
        } else {
          errorMessage = `Import failed (HTTP ${status})`;
        }
      } else if (error.message) {
        errorMessage = error.message;
      }
      
      toast.error(errorMessage, { autoClose: 8000 });
      e.target.value = ''; // Reset file input
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container className="mt-4">
      <Card className="shadow-sm mb-4">
        <Card.Body>
          <div className="d-flex justify-content-between align-items-start mb-3 flex-wrap gap-2">
            <div>
              <h2 className="mb-1">Customer Management</h2>
            </div>
            <div className="d-flex flex-wrap gap-2">
              <Button variant="success" onClick={() => setShowModal(true)} disabled={loading}>
                <FaPlus /> Add Customer
              </Button>
              <Button variant="info" onClick={handleExport} disabled={loading}>
                <FaFileExcel /> Export
              </Button>
              <Button variant="secondary" size="sm" onClick={handleDownloadTemplate} disabled={loading}>
                <FaFileExcel /> Template
              </Button>
              <label
                htmlFor="import-file"
                className={`btn btn-warning ${loading ? 'disabled' : ''}`}
                aria-disabled={loading}
              >
                <FaFileExcel /> Import
              </label>
              <input
                id="import-file"
                type="file"
                accept=".xlsx,.xls"
                style={{ display: 'none' }}
                onChange={handleImport}
                disabled={loading}
              />
            </div>
          </div>

          <Row className="g-2 align-items-center mb-3">
            <Col md={6}>
              <Form.Group className="d-flex">
                <Form.Control
                  type="text"
                  placeholder="Search customers..."
                  value={searchKeyword}
                  onChange={(e) => setSearchKeyword(e.target.value)}
                />
                <Button variant="primary" className="ms-2" onClick={handleSearch} disabled={loading}>
                  <FaSearch /> Search
                </Button>
              </Form.Group>
            </Col>
          </Row>

          {loading ? (
            <div className="text-center py-4">
              <Spinner animation="border" role="status" />
              <div className="mt-2">Loading customers...</div>
            </div>
          ) : (
            <>
              <Table striped bordered hover responsive className="align-middle" style={{tableLayout: 'fixed'}}>
                <thead>
                  <tr>
                    <th style={{width: '5%'}}>ID</th>
                    <th style={{width: '15%'}}>First Name</th>
                    <th style={{width: '15%'}}>Last Name</th>
                    <th style={{width: '15%'}}>NIC</th>
                    <th style={{width: '20%'}}>Email</th>
                    <th style={{width: '10%'}}>Gender</th>
                    <th style={{width: '20%'}}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {customers.length === 0 ? (
                    <tr>
                      <td colSpan="7" className="text-center py-4">
                        <div className="mb-2">No customers found.</div>
                        <Button variant="success" size="sm" onClick={() => setShowModal(true)} disabled={loading}>
                          <FaPlus className="me-1" /> Add your first customer
                        </Button>
                      </td>
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
                        <td style={{whiteSpace: 'nowrap'}}>
                          <Button
                            variant="info"
                            size="sm"
                            className="me-2"
                            onClick={() => handleView(customer.id)}
                            disabled={loading}
                          >
                            <FaEye /> View
                          </Button>
                          <Button
                            variant="warning"
                            size="sm"
                            className="me-2"
                            onClick={() => handleEdit(customer)}
                            disabled={loading}
                          >
                            <FaEdit /> Edit
                          </Button>
                          <Button
                            variant="danger"
                            size="sm"
                            onClick={() => handleDelete(customer.id)}
                            disabled={loading}
                          >
                            <FaTrash /> Delete
                          </Button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </Table>

              <div className="d-flex justify-content-between align-items-center flex-wrap gap-2">
                <Button
                  disabled={currentPage === 0}
                  onClick={() => setCurrentPage(currentPage - 1)}
                >
                  Previous
                </Button>
                <span className="text-muted">Page {currentPage + 1} of {totalPages}</span>
                <Button
                  disabled={currentPage >= totalPages - 1}
                  onClick={() => setCurrentPage(currentPage + 1)}
                >
                  Next
                </Button>
              </div>
            </>
          )}
        </Card.Body>
      </Card>

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

            <hr />
            <h5>Phone Numbers</h5>
            {formData.phoneNumbers.map((phone, index) => (
              <Row key={index} className="mb-2">
                <Col md={5}>
                  <Form.Control
                    type="text"
                    placeholder="Phone Number"
                    value={phone.phoneNumber}
                    onChange={(e) => {
                      const newPhones = [...formData.phoneNumbers];
                      newPhones[index].phoneNumber = e.target.value;
                      setFormData({ ...formData, phoneNumbers: newPhones });
                    }}
                  />
                </Col>
                <Col md={4}>
                  <Form.Select
                    value={phone.phoneType}
                    onChange={(e) => {
                      const newPhones = [...formData.phoneNumbers];
                      newPhones[index].phoneType = e.target.value;
                      setFormData({ ...formData, phoneNumbers: newPhones });
                    }}
                  >
                    <option value="MOBILE">Mobile</option>
                    <option value="HOME">Home</option>
                    <option value="WORK">Work</option>
                  </Form.Select>
                </Col>
                <Col md={3}>
                  <Button 
                    variant="danger" 
                    size="sm"
                    onClick={() => {
                      const newPhones = formData.phoneNumbers.filter((_, i) => i !== index);
                      setFormData({ ...formData, phoneNumbers: newPhones });
                    }}
                  >
                    Remove
                  </Button>
                </Col>
              </Row>
            ))}
            <Button 
              variant="secondary" 
              size="sm" 
              className="mb-3"
              onClick={() => {
                setFormData({
                  ...formData,
                  phoneNumbers: [...formData.phoneNumbers, { phoneNumber: '', phoneType: 'MOBILE' }]
                });
              }}
            >
              + Add Phone Number
            </Button>

            <hr />
            <h5>Addresses</h5>
            {formData.addresses.map((address, index) => (
              <div key={index} className="border p-3 mb-3">
                <Row>
                  <Col md={6}>
                    <Form.Group className="mb-2">
                      <Form.Label>Address Line 1</Form.Label>
                      <Form.Control
                        type="text"
                        placeholder="Street address"
                        value={address.addressLine1}
                        onChange={(e) => {
                          const newAddresses = [...formData.addresses];
                          newAddresses[index].addressLine1 = e.target.value;
                          setFormData({ ...formData, addresses: newAddresses });
                        }}
                      />
                    </Form.Group>
                  </Col>
                  <Col md={6}>
                    <Form.Group className="mb-2">
                      <Form.Label>Address Line 2</Form.Label>
                      <Form.Control
                        type="text"
                        placeholder="Apartment, suite, etc."
                        value={address.addressLine2 || ''}
                        onChange={(e) => {
                          const newAddresses = [...formData.addresses];
                          newAddresses[index].addressLine2 = e.target.value;
                          setFormData({ ...formData, addresses: newAddresses });
                        }}
                      />
                    </Form.Group>
                  </Col>
                </Row>
                <Row>
                  <Col md={4}>
                    <Form.Group className="mb-2">
                      <Form.Label>City *</Form.Label>
                      <Form.Select
                        required
                        value={address.cityId || ''}
                        onChange={(e) => {
                          const newAddresses = [...formData.addresses];
                          const selectedValue = e.target.value;
                          const cityId = selectedValue && selectedValue !== '' ? parseInt(selectedValue, 10) : null;
                          newAddresses[index].cityId = cityId;
                          setFormData({ ...formData, addresses: newAddresses });
                        }}
                      >
                        <option value="">Select City *</option>
                        {cities.map(city => (
                          <option key={city.id} value={city.id}>{city.name}</option>
                        ))}
                      </Form.Select>
                    </Form.Group>
                  </Col>
                  <Col md={4}>
                    <Form.Group className="mb-2">
                      <Form.Label>Address Type</Form.Label>
                      <Form.Select
                        value={address.addressType}
                        onChange={(e) => {
                          const newAddresses = [...formData.addresses];
                          newAddresses[index].addressType = e.target.value;
                          setFormData({ ...formData, addresses: newAddresses });
                        }}
                      >
                        <option value="HOME">Home</option>
                        <option value="WORK">Work</option>
                        <option value="OTHER">Other</option>
                      </Form.Select>
                    </Form.Group>
                  </Col>
                  <Col md={4}>
                    <Form.Group className="mb-2">
                      <Form.Label>Primary Address</Form.Label>
                      <Form.Check
                        type="checkbox"
                        checked={address.isPrimary || false}
                        onChange={(e) => {
                          const newAddresses = [...formData.addresses];
                          newAddresses[index].isPrimary = e.target.checked;
                          setFormData({ ...formData, addresses: newAddresses });
                        }}
                      />
                    </Form.Group>
                  </Col>
                </Row>
                <Button 
                  variant="danger" 
                  size="sm"
                  onClick={() => {
                    const newAddresses = formData.addresses.filter((_, i) => i !== index);
                    setFormData({ ...formData, addresses: newAddresses });
                  }}
                >
                  Remove Address
                </Button>
              </div>
            ))}
            <Button 
              variant="secondary" 
              size="sm" 
              className="mb-3"
              onClick={() => {
                setFormData({
                  ...formData,
                  addresses: [...formData.addresses, { 
                    addressLine1: '', 
                    addressLine2: '', 
                    cityId: null, 
                    addressType: 'HOME',
                    isPrimary: false 
                  }]
                });
              }}
            >
              + Add Address
            </Button>

            <div className="text-end">
              <Button variant="secondary" className="me-2" onClick={() => { setShowModal(false); resetForm(); }}>
                Cancel
              </Button>
              <Button variant="primary" type="submit" disabled={submitting}>
                {submitting ? (
                  <>
                    <Spinner animation="border" size="sm" className="me-2" />
                    Saving...
                  </>
                ) : selectedCustomer ? 'Update' : 'Create'}
              </Button>
            </div>
          </Form>
        </Modal.Body>
      </Modal>

      {/* Customer View Modal */}
      <Modal show={showViewModal} onHide={() => setShowViewModal(false)} size="lg">
        <Modal.Header closeButton>
          <Modal.Title>Customer Details</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {viewCustomer && (
            <div>
              <Card className="mb-3">
                <Card.Header><h5>Personal Information</h5></Card.Header>
                <Card.Body>
                  <Row>
                    <Col md={6}>
                      <p><strong>First Name:</strong> {viewCustomer.firstName}</p>
                      <p><strong>Last Name:</strong> {viewCustomer.lastName}</p>
                      <p><strong>NIC:</strong> {viewCustomer.nic}</p>
                    </Col>
                    <Col md={6}>
                      <p><strong>Date of Birth:</strong> {viewCustomer.dateOfBirth}</p>
                      <p><strong>Email:</strong> {viewCustomer.email || 'N/A'}</p>
                      <p><strong>Gender:</strong> {viewCustomer.gender}</p>
                    </Col>
                  </Row>
                </Card.Body>
              </Card>

              {viewCustomer.phoneNumbers && viewCustomer.phoneNumbers.length > 0 && (
                <Card className="mb-3">
                  <Card.Header><h5>Phone Numbers</h5></Card.Header>
                  <Card.Body>
                    {viewCustomer.phoneNumbers.map((phone, index) => (
                      <p key={index}>
                        <Badge bg="primary" className="me-2">{phone.phoneType}</Badge>
                        {phone.phoneNumber}
                      </p>
                    ))}
                  </Card.Body>
                </Card>
              )}

              {viewCustomer.addresses && viewCustomer.addresses.length > 0 && (
                <Card className="mb-3">
                  <Card.Header><h5>Addresses</h5></Card.Header>
                  <Card.Body>
                    {viewCustomer.addresses.map((address, index) => (
                      <Card key={index} className="mb-2" style={{backgroundColor: '#f8f9fa'}}>
                        <Card.Body>
                          <div className="d-flex justify-content-between align-items-start mb-2">
                            <Badge bg="secondary">{address.addressType}</Badge>
                            {address.isPrimary && <Badge bg="success">Primary</Badge>}
                          </div>
                          <p className="mb-1"><strong>Address:</strong> {address.addressLine1}</p>
                          {address.addressLine2 && (
                            <p className="mb-1"><strong>Address Line 2:</strong> {address.addressLine2}</p>
                          )}
                          <p className="mb-0"><strong>City:</strong> {address.cityName || 'N/A'}</p>
                        </Card.Body>
                      </Card>
                    ))}
                  </Card.Body>
                </Card>
              )}
            </div>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowViewModal(false)}>
            Close
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
}

export default CustomerList;

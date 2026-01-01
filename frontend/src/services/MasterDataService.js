import axiosInstance from './axiosConfig';

const MasterDataService = {
  // Get all countries
  getAllCountries: () => {
    return axiosInstance.get('/countries');
  },

  // Get all cities
  getAllCities: () => {
    return axiosInstance.get('/cities');
  },

  // Get cities by country
  getCitiesByCountry: (countryId) => {
    return axiosInstance.get(`/cities/country/${countryId}`);
  }
};

export default MasterDataService;

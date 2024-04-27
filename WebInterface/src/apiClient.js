import axios from 'axios';

const axiosInstance = axios.create({
    baseURL: 'http://localhost:8080/api', // Replace this with your actual base URL
});

export default axiosInstance;
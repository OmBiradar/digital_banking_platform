const API_BASE_URL = 'http://localhost:8080/api/auth'

export const authService = {
    login: async (email, password) => {
        const response = await fetch(`${API_BASE_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify( {email, password} )
        });
        return response.json();
    },
    register: async (userData) => {
        const response = await fetch(`${API_BASE_URL}/resgister`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        });
        return response.json();
    }
};
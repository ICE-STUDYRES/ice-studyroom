import axios from 'axios';
import { useMemberHandlers } from './MemberHandlers.jsx';

export const useTokenHandler = () => {
    const { handleLogout } = useMemberHandlers();

    const getRefreshTokenFromCookie = () => {
        const cookies = document.cookie.split('; ');
        const refreshTokenCookie = cookies.find(row => row.startsWith('refresh_token='));
        return refreshTokenCookie ? refreshTokenCookie.split('=')[1] : null;
    };

    const refreshTokens = async () => {
        try {
            const refreshToken = getRefreshTokenFromCookie();
            const accessToken = localStorage.getItem('accessToken');

            if (!refreshToken) {
                console.warn("No refresh token found. Logging out.");
                handleLogout();
                return null;
            }

            const response = await axios.post(
                '/api/users/refresh',
                { },
                {
                    headers: {
                        'Authorization': `Bearer ${accessToken}`
                    },
                    withCredentials: true
                }
            );

            if (response.data.code !== "S200" || !response.data.data) {
                console.warn("Failed to refresh token. Logging out.");
                handleLogout();
                return null;
            }

            const { accessToken: newAccessToken, refreshToken: newRefreshToken } = response.data.data;
            localStorage.setItem('accessToken', newAccessToken);
            localStorage.setItem('refreshToken', newRefreshToken);

            console.log("Tokens refreshed successfully.");
            return newAccessToken;
        } catch (error) {
            console.error("Error refreshing token:", error.response?.data || error);

            if (error.response?.status === 401) {
                handleLogout();
            }
            return null;
        }
    };

    return { refreshTokens };
};
import axios from 'axios';
import { useMemberHandlers } from './MemberHandlers.jsx';

export const useTokenHandler = () => {
    const { handleLogout } = useMemberHandlers();

    const refreshTokens = async () => {
        try {
            const refreshToken = localStorage.getItem('refreshToken');
            const accessToken = localStorage.getItem('accessToken');

            if (!refreshToken) {
                console.warn("No refresh token found. Logging out.");
                handleLogout();
                return null;
            }

            const response = await axios.post(
                '/api/users/refresh',
                { refreshToken },
                {
                    headers: {
                        'Authorization': `Bearer ${accessToken}`
                    }
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
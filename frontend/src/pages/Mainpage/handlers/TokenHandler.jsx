import axios from 'axios';

let isRefreshing = false;   // 🔹 현재 토큰 갱신 중인지 추적
let refreshSubscribers = []; // 🔹 토큰이 갱신되면 대기 중인 요청을 실행

export const useTokenHandler = () => {
    const refreshTokens = async () => {
        if (isRefreshing) {
            return new Promise((resolve) => {
                refreshSubscribers.push(resolve);
            });
        }
        isRefreshing = true;

        try {
            const accessToken = sessionStorage.getItem('accessToken');
            if (!accessToken) {
                return;
            }
            const response = await axios.post(
                '/api/users/auth/refresh',
                {},
                {
                    headers: { 'Authorization': `Bearer ${accessToken}` },
                    withCredentials: true
                }
            );

            if (response.data.code !== "S200" || !response.data.data) {
                handleLogout();
                return null;
            }

            const { accessToken: newAccessToken } = response.data.data;
            sessionStorage.setItem('accessToken', newAccessToken);

            // 🔹 대기 중이던 요청들 재실행
            refreshSubscribers.forEach((callback) => callback(newAccessToken));
            refreshSubscribers = [];

            return newAccessToken;
        } catch (error) {
            return null;
        } finally {
            isRefreshing = false;
        }
    };

    return { refreshTokens };
};

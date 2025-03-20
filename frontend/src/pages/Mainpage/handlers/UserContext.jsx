import { createContext, useContext, useEffect, useState } from "react";
import axios from "axios";
import { useTokenHandler } from "./TokenHandler";

const UserContext = createContext(null);

export const UserProvider = ({ children }) => {
    const [userData, setUserData] = useState(null);
    const { refreshTokens } = useTokenHandler();

    useEffect(() => {
        const fetchUserData = async () => {
            let accessToken = sessionStorage.getItem("accessToken");
            if (!accessToken) return;

            try {
                let response = await axios.get("/api/users", {
                    headers: { Authorization: `Bearer ${accessToken}` }
                });

                if (response.data && response.data.data) {
                    setUserData(response.data.data);
                }
            } catch (error) {
                if (error.response?.status === 401) {
                    accessToken = await refreshTokens();
                    if (accessToken) {
                        return fetchUserData();
                    }
                }
            }
        };

        fetchUserData();
    }, []);

    return (
        <UserContext.Provider value={userData}>
            {children}
        </UserContext.Provider>
    );
};

export const useUser = () => useContext(UserContext);
import { createContext, useContext, useEffect, useState } from "react";
import axios from "axios";
import { useTokenHandler } from "./TokenHandler";

const UserContext = createContext(null);
const UserDispatchContext = createContext(null);

export const UserProvider = ({ children }) => {
    const [userData, setUserData] = useState(null);
    const { refreshTokens } = useTokenHandler();

    useEffect(() => {
        const fetchUserData = async () => {
            let accessToken = sessionStorage.getItem("accessToken");
            if (!accessToken || userData) return;

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
    }, [userData]);

    return (
        <UserContext.Provider value={userData}>
            <UserDispatchContext.Provider value={setUserData}>
                {children}
            </UserDispatchContext.Provider>
        </UserContext.Provider>
    );
};

export const useUser = () => useContext(UserContext);
export const useUserDispatch = () => useContext(UserDispatchContext);
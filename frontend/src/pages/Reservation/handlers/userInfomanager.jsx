import { useState, useEffect } from "react";
import { useTokenHandler } from "../../Mainpage/handlers/TokenHandler";
import { useNotification } from "../../Notification/Notification";

export const userInfoManager = () => {
  const [userInfo, setUserInfo] = useState({
    mainUser: { name: "", email: "" },
    participants: [],
  });
  const [error, setError] = useState(null);
  const { refreshTokens } = useTokenHandler();
  const { addNotification } = useNotification();

  const fetchUserInfo = async (retry = true) => {
    try {
      let accessToken = sessionStorage.getItem("accessToken");
      if (!accessToken) {
        addNotification('member', 'error');
        return;
      }
  
      const response = await fetch("/api/users", {
        method: "GET",
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      });
  
      if (response.status === 401 && retry) {
        accessToken = await refreshTokens();
        
        if (accessToken) {
          return fetchUserInfo(false); // 한 번만 재시도
        } else {
          console.error("Token refresh failed. Logging out.");
          return;
        }
      }
  
      if (!response.ok) throw new Error("사용자 정보를 가져오는 데 실패했습니다.");
  
      const responseData = await response.json();
  
      if (responseData.code !== "S200") {
        throw new Error(responseData.message || "알 수 없는 오류");
      }
  
      setUserInfo((prev) => ({
        ...prev,
        mainUser: {
          name: responseData.data.name,
          email: responseData.data.email,
        },
      }));
    } catch (err) {
      setError(err.message);
      console.error("사용자 정보 오류:", err.message);
    }
  };
  
  useEffect(() => {
    fetchUserInfo();
  }, []);
  
  return { userInfo, setUserInfo, fetchUserInfo, error };
};
  

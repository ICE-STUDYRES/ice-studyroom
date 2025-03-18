import { useState, useEffect } from 'react';
import { useTokenHandler } from "./TokenHandler";
import axios from 'axios';

export const usePenaltyHandlers = () => {
    const [penaltyEndAt, setPenaltyEndAt] = useState("");
    const [penaltyReason, setPenaltyReason] = useState(null);

      const {
        refreshTokens,
      } = useTokenHandler();

    const penaltyReasonMap = {
        CANCEL: "취소",
        LATE: "지각",
        NO_SHOW: "노쇼"
    };

    useEffect(() => {
        const fetchPenaltyData = async () => {
            try {
                let accessToken = sessionStorage.getItem("accessToken");
    
                if (!accessToken) {
                    setPenaltyEndAt("");
                    setPenaltyReason(null);
                    return;
                }
    
                let response = await axios.get("/api/users", {
                    headers: { Authorization: `Bearer ${accessToken}` },
                });
    
                if (response.data && response.data.data) {
                    const { penaltyEndAt, penaltyReasonType } = response.data.data;
    
                    if (penaltyEndAt) {
                        const endDate = new Date(penaltyEndAt + "Z");
                        const today = new Date();
    
                        const formattedEndAt = `${endDate.getUTCFullYear()}-${String(endDate.getUTCMonth() + 1).padStart(2, "0")}-${String(endDate.getUTCDate()).padStart(2, "0")}`;
                        const remainingDays = Math.floor((endDate - today) / (1000 * 60 * 60 * 24));
    
                        setPenaltyEndAt(`${formattedEndAt} (${remainingDays}일 남음)`);
                    } else {
                        setPenaltyEndAt("");
                    }
                    setPenaltyReason(penaltyReasonMap[penaltyReasonType]);
                }
            } catch (error) {
                if (error.response && error.response.status === 401) {
                    const newAccessToken = await refreshTokens();
                    if (!newAccessToken) return;
                    return fetchPenaltyData();
                }
            }
        };
    
        fetchPenaltyData();
    }, []);
    

    return {
        penaltyReason,
        penaltyEndAt,
        setPenaltyEndAt,
        setPenaltyReason,
        penaltyReasonMap,
    };
};

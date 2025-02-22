import { useState, useEffect } from 'react';
import axios from 'axios';

export const usePenaltyHandlers = () => {
    const [penaltyEndAt, setPenaltyEndAt] = useState("");
    const [penaltyReason, setPenaltyReason] = useState(null);
    const accessToken = sessionStorage.getItem("accessToken"); 

    const penaltyReasonMap = {
        CANCEL: "취소",
        LATE: "지각",
        NO_SHOW: "노쇼"
    };

    useEffect(() => {
        const fetchPenaltyData = async () => {
            try {
                if (!accessToken) {
                    setPenaltyEndAt("");
                    setPenaltyReason(null);
                    return;
                }

                const response = await axios.get('/api/users', {
                    headers: { Authorization: `Bearer ${accessToken}` }
                });

                if (response.data && response.data.data) {
                    const { penaltyEndAt, penaltyReasonType } = response.data.data;

                    if (penaltyEndAt) {
                        const endDate = new Date(penaltyEndAt + "Z"); // UTC 변환 적용
                        const today = new Date();

                        const formattedEndAt = `${endDate.getFullYear()}-${String(endDate.getMonth() + 1).padStart(2, '0')}-${String(endDate.getDate()).padStart(2, '0')}`;
                        const remainingDays = Math.floor((endDate - today) / (1000 * 60 * 60 * 24));

                        setPenaltyEndAt(`${formattedEndAt} (${remainingDays}일 남음)`);
                    } else {
                        setPenaltyEndAt("");
                    }

                    setPenaltyReason(penaltyReasonMap[penaltyReasonType]);
                }
            } catch (error) {
                console.error("Error fetching penalty data:", error);
            }
        };
        fetchPenaltyData();

    }, [accessToken]);

    return {
        penaltyReason,
        penaltyEndAt,
        setPenaltyEndAt,
        setPenaltyReason,
        penaltyReasonMap,
    };
};

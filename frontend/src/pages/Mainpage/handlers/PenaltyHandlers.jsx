import { useState, useEffect } from 'react';
import axios from 'axios';
import { useMemberHandlers } from './MemberHandlers.jsx';

export const usePenaltyHandlers = () => {
    const { isLoggedIn } = useMemberHandlers();
    const [penaltyEndAt, setPenaltyEndAt] = useState("");
    const [penaltyReason, setPenaltyReason] = useState(null);

    useEffect(() => {
        const fetchPenaltyData = async () => {
            try {
                const token = sessionStorage.getItem('accessToken');
                const response = await axios.get('/api/users', {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
    
                if (response.data && response.data.data) {
                    const { penaltyEndAt, penaltyReasonType } = response.data.data;
    
                    if (penaltyEndAt) {
                        const endDate = new Date(penaltyEndAt);
                        const today = new Date();
    
                        const formattedEndAt = `${endDate.getFullYear()}-${String(endDate.getMonth() + 1).padStart(2, '0')}-${String(endDate.getDate()).padStart(2, '0')}`;
                        const remainingDays = Math.max(0, Math.ceil((endDate - today) / (1000 * 60 * 60 * 24)));
    
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
    
        if (isLoggedIn) {
            fetchPenaltyData();
        }
    }, [isLoggedIn]);

    const penaltyReasonMap = {
        CANCEL: "취소",
        LATE: "지각",
        NO_SHOW: "노쇼"
    };

    return {
        penaltyReason,
        penaltyEndAt,
        setPenaltyEndAt,
        setPenaltyReason,
        penaltyReasonMap,
    };
};
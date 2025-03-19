import { useState, useEffect } from 'react';
import { useUser } from "../handlers/UserContext";

export const usePenaltyHandlers = () => {
    const [penaltyEndAt, setPenaltyEndAt] = useState("");
    const [penaltyReason, setPenaltyReason] = useState(null);

    const userData = useUser();

    const penaltyReasonMap = {
        CANCEL: "취소",
        LATE: "지각",
        NO_SHOW: "노쇼"
    };

    useEffect(() => {
        if (userData) {
            const { penaltyEndAt, penaltyReasonType } = userData;

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
    }, [userData]);

    return {
        penaltyReason,
        penaltyEndAt,
        setPenaltyEndAt,
        setPenaltyReason,
        penaltyReasonMap,
    };
};

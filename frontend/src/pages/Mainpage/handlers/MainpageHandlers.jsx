import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useNotification } from '../../Notification/Notification';
import { useMemberHandlers } from './MemberHandlers.jsx';
import { usePenaltyHandlers } from './PenaltyHandlers.jsx';

export const useMainpageHandlers = () => {
  const {
    isLoggedIn,
  } = useMemberHandlers();

  const {
    setPenaltyEndAt,
    setPenaltyReason,
    penaltyReasonMap,
  } = usePenaltyHandlers();

    const [showNotice, setShowNotice] = useState(false);
    const [showPenaltyPopup, setShowPenaltyPopup] = useState(false);
    const [showQRModal, setShowQRModal] = useState(false);
    const { addNotification } = useNotification();

    useEffect(() => {
      const fetchUserData = async () => {
        try {
          const token = localStorage.getItem('accessToken');
          const response = await axios.get('/api/users', {
            headers: {
              'Authorization': `Bearer ${token}`
            }
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
          console.error("Error fetching user data:", error);
        }
      };
    
      if (isLoggedIn) {
        fetchUserData();
      }
    }, [isLoggedIn]);

    const navigate = useNavigate();  
    const handleReservationClick = () => {
      if (isLoggedIn) {
        navigate('/reservation/room');
      } else {
        addNotification('member', 'error');
      }
    };
    const handleMyReservationStatusClick = () => navigate('/MyReservationStatus');
    const handleReservationStatusClick = () => navigate('/ReservationStatus');
    const handleReservationManageClick = () => {
      if (isLoggedIn) {
        navigate('/reservation/manage');
      } else {
        addNotification('member', 'error');
      }
    };
    const handleNoticeClick = () => setShowNotice(true);
    const handleCloseNotice = () => setShowNotice(false);
    const handlePenaltyClick = () => {
      if (isLoggedIn) {
        setShowPenaltyPopup(true);
      } else {
        addNotification('member', 'error');
      }
    };
    const handleClosePenaltyPopup = () => setShowPenaltyPopup(false);
    const handleQRClick = () => setShowQRModal(true);
    const handleCloseQRModal = () => setShowQRModal(false);

  return {
// 팝업
    showNotice,
    showPenaltyPopup,
    showQRModal,
    handleNoticeClick,
    handleCloseNotice,
    handlePenaltyClick,
    handleClosePenaltyPopup,
    handleQRClick,
    handleCloseQRModal,
// 페이지 이동
    handleReservationClick,
    handleReservationStatusClick,
    handleMyReservationStatusClick,
    handleReservationManageClick,
  };
};
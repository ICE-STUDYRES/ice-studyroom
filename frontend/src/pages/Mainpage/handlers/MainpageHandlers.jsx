import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotification } from '../../Notification/Notification';
import { usePenaltyHandlers } from './PenaltyHandlers.jsx';
import { useUser } from "../handlers/UserContext";
import { useTokenHandler } from "./TokenHandler";

export const useMainpageHandlers = (resId, myReservations) => {
    const {
      setPenaltyEndAt,
      setPenaltyReason,
      penaltyReasonMap,
    } = usePenaltyHandlers();

    const userData = useUser();

    const [showNotice, setShowNotice] = useState(false);
    const [showPenaltyPopup, setShowPenaltyPopup] = useState(false);
    const [showQRModal, setShowQRModal] = useState(false);
    const [qrToken, setQrToken] = useState(null);
    const { addNotification } = useNotification();
    const { refreshTokens } = useTokenHandler();
    let accessToken = sessionStorage.getItem('accessToken');

    useEffect(() => {
      if (userData) {
          const { penaltyEndAt, penaltyReasonType } = userData;

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
  }, [userData]);

    const navigate = useNavigate();  
    const handleReservationClick = () => {
      if (accessToken) {
        navigate('/reservation/room');
      } else {
        addNotification('member', 'error');
      }
    };
    const handleMyReservationStatusClick = () => navigate('/reservation/my-status');
    const handleReservationStatusClick = () => {
      if (accessToken) {
        navigate('/reservation/status');
      } else {
        addNotification('member', 'error');
      }
    };
    const handleReservationManageClick = () => {
      if (accessToken) {
        navigate('/reservation/manage');
      } else {
        addNotification('member', 'error');
      }
    };
    const handleNoticeClick = () => setShowNotice(true);
    const handleCloseNotice = () => setShowNotice(false);
    const handlePenaltyClick = () => {
      if (accessToken) {
        setShowPenaltyPopup(true);
      } else {
        addNotification('member', 'error');
      }
    };
    const handleClosePenaltyPopup = () => setShowPenaltyPopup(false);
    
    const handleQRClick = async (retry = true) => {
      let currentResId = resId;
      if (!currentResId) {
          if (!myReservations || myReservations.length === 0 || !myReservations[0].id) {
              addNotification('qr', 'error', '예약 정보를 불러올 수 없습니다.');
              return;
          }
          currentResId = myReservations[0].id;
      }

      try {
          let currentAccessToken = sessionStorage.getItem('accessToken');
          const response = await fetch(`/api/reservations/my/${currentResId}`, {
              method: 'GET',
              headers: {
                  'Authorization': `Bearer ${currentAccessToken}`,
                  'Content-Type': 'application/json'
              }
          });

          if (response.status === 401 && retry) {
              currentAccessToken = await refreshTokens();
              if (currentAccessToken) {
                  return handleQRClick(false); // 재시도
              }
          }

          const result = await response.json();

          if (!response.ok) {
              addNotification('qr', 'error', result.message || 'QR 코드 발급에 실패했습니다.');
              return;
          }

          setQrToken(result.data);
          setShowQRModal(true);

      } catch (error) {
          addNotification('qr', 'error', '네트워크 오류가 발생했습니다.');
      }
  };

  const handleCloseQRModal = () => setShowQRModal(false);

    return {
      // 팝업
      showNotice,
      showPenaltyPopup,
      showQRModal,
      qrToken,
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
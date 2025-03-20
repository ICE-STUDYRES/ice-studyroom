import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotification } from '../../Notification/Notification';
import { usePenaltyHandlers } from './PenaltyHandlers.jsx';
import useQRCodeFetcher from '../components/QRCodeFetcher';
import { useUser } from "../handlers/UserContext";

export const useMainpageHandlers = (resId) => {
    const {
      setPenaltyEndAt,
      setPenaltyReason,
      penaltyReasonMap,
    } = usePenaltyHandlers();

    const userData = useUser();

    const [showNotice, setShowNotice] = useState(false);
    const [showPenaltyPopup, setShowPenaltyPopup] = useState(false);
    const [showQRModal, setShowQRModal] = useState(false);
    const { addNotification } = useNotification();
    let accessToken = sessionStorage.getItem('accessToken');
    const { qrStatus } = useQRCodeFetcher(resId);  

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
    const handleQRClick = () => {
      if (qrStatus === 418) {
        addNotification("penalty", "error");
        return;
      }
      setShowQRModal(true);
    };
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
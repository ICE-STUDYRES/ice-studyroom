import React, { useState, useEffect } from 'react';
import loginButtonImage from "../../assets/images/Log in.png";
import logoutButtonImage from "../../assets/images/Log out.png";
import xImage from "../../assets/images/X.png";
import iconImage from "../../assets/images/icon.png";
import iconxImage from "../../assets/images/iconx.png";
import alertImage from "../../assets/images/alert.png";
import { useNavigate } from 'react-router-dom';
import { X } from 'lucide-react';
import './mainpage.css';

const MainPage = () => {
  const [currentDate, setCurrentDate] = useState("");
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [studentId] = useState("201902149");
  const [studentName] = useState("양재원");
  const [qrCodeUrl, setQrCodeUrl] = useState(null);
  const [showNotice, setShowNotice] = useState(false);
  const [showPenaltyPopup, setShowPenaltyPopup] = useState(false);
  const [showQRModal, setShowQRModal] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const today = new Date();
    const formattedDate = today.toLocaleDateString('ko-KR', {
      month: 'numeric',
      day: 'numeric',
      year: 'numeric'
    });
    setCurrentDate(formattedDate);
  }, []);

  useEffect(() => {
    const fetchQRCode = async () => {
      try {
        const response = await fetch(`/api/qr/${studentId}/${studentName}`);
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        setQrCodeUrl(url);
      } catch (error) {
        console.error('QR 코드 로드 실패:', error);
      }
    };

    fetchQRCode();
  }, [studentId, studentName]);

  const handleLogin = () => setIsLoggedIn(true);
  const handleLogout = () => setIsLoggedIn(false);
  const handleReservationClick = () => navigate('/reservation/room');
  const handleReservationStatusClick = () => navigate('/ReservationStatus');
  const handleNoticeClick = () => setShowNotice(true);
  const handleCloseNotice = () => setShowNotice(false);
  const handlePenaltyClick = () => setShowPenaltyPopup(true);
  const handleClosePenaltyPopup = () => setShowPenaltyPopup(false);
  const handleQRClick = () => setShowQRModal(true);
  const handleCloseQRModal = () => setShowQRModal(false);

  return (
    <div className="container relative">
      {isLoggedIn ? (
        <>
          <div className="headers">
            <div className="header-content">
              <h1 className="title">정보통신공학과 스터디룸</h1>
              <button className="logout-Button" onClick={handleLogout}>
                <img src={logoutButtonImage} alt="로그아웃 버튼" className="logoutButtonImage" />
              </button>
            </div>
          </div>

          <div className="welcome-message">
            <p>"환영합니다! 스터디룸 예약을 확인하고 QR코드를 스캔하여 입장해 주세요."</p>
          </div>

          <div className="qr-section">
            <span className="status-badge">입실</span>
            <div className="qr-content">
              <div className="qr-container" onClick={handleQRClick} style={{ cursor: 'pointer' }}>
                {qrCodeUrl ? (
                  <img src={qrCodeUrl} alt="QR Code" className="qr-image" />
                ) : (
                  <div className="qr-loading">Loading...</div>
                )}
              </div>
              <div className="qr-details">
                <p>예약 날짜: {currentDate}</p>
                <p>방 번호</p>
                <p>{studentName}</p>
                <p>{studentId}</p>
              </div>
            </div>
          </div>
        </>
      ) : (
        <>
          <div className="headers">
            <div className="header-content">
              <h1 className="title">정보통신공학과 스터디룸</h1>
              <button className="login-Button" onClick={handleLogin}>
                <img src={loginButtonImage} alt="로그인 버튼" className="loginButtonImage" />
              </button>
            </div>
          </div>
          
          <div className="welcome-message">
            <p>"환영합니다! 스터디룸 예약을 확인하고 QR코드를 스캔하여 입장해 주세요."</p>
          </div>
          <div className="qr-section">
            <div className="qr-content">
              <div className="loginMessage">로그인 후 이용해 주세요</div>
            </div>
          </div>
        </>
      )}

      <div className="category-section">
        <h2 className="section-title">카테고리</h2>
        <div className="category-grid">
          <button className="category-item reservation" onClick={handleReservationClick}>
            <div className="category-icon">📅</div>
            <div>예약하기</div>
            <div className="category-date">{currentDate}</div>
          </button>
          
          <button className="category-item status" onClick={handleReservationStatusClick}>
            <div className="category-icon">✔️</div>
            <div>예약 현황</div>
            <div className="category-date">{currentDate}</div>
          </button>
          
          <button className="category-item notice" onClick={handleNoticeClick}>
            <span><img src={alertImage} alt="유의사항" className="alertImage"/></span>
            <span className="category-text">유의사항</span>
          </button>
          
          <button className="category-item modify">
            <span className="category-icon">✏️</span>
            <span className="category-text">연장 및 취소</span>
          </button>
        </div>
      </div>

      <div className="penalty-section" onClick={handlePenaltyClick}>
        <h2 className="section-title">패널티 현황</h2>
        <div className="penalty-container">
          <div className="penalty-item">
            <div className="late-label">
              <img src={iconImage} alt="지각" className="penalty-icon" />
              <span> 지각</span>
            </div>
            <span className="penalty-count">1회</span>
          </div>
          <div className="penalty-item">
            <div className="noshow-label">
              <img src={iconxImage} alt="노쇼" className="penalty-icon" />
              <span> No Show</span>
            </div>
            <span className="penalty-count">n일 예약 제한</span>
          </div>
          <div className="penalty-item">
            <div className="cancel-label">
              <img src={xImage} alt="취소" className="penalty-icon" />
              <span> 취소</span>
            </div>
            <span className="penalty-count">0회</span>
          </div>
        </div>
      </div>

      {showNotice && (
        <div className="notice-popup" onClick={handleCloseNotice}>
          <div 
            className="penalty-content"  //원래 notice-content였는데 왜 안되는지 모르겠음
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-bold">스터디룸 이용 주의사항</h3>
              <button 
                onClick={handleCloseNotice}
                className="close-button p-1 hover:bg-gray-100 rounded-full"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="space-y-3">
              <p className="text-sm">1. 예약 시간 30분 안에 입실하지 않을 경우 자동 취소되며, 노쇼 패널티가 부과됩니다.</p>
              <p className="text-sm">2. 잔여 사용시간이 30분 이상 남아있다면 다음 예약 최대 시간이 1시간으로 조정됩니다.</p>
              <p className="text-sm">3. 퇴실을 30분 이상 늦게할 경우 패널티가 부여됩니다.</p>
              <p className="text-sm">4. 예약 인원 미준수 시 해당 학기 동안 예약이 제한됩니다.</p>
              <p className="text-sm">5. 스터디룸 내 음식물 반입 및 섭취는 엄격히 금지됩니다.</p>
              <p className="text-sm">6. 사용 후 정리정돈 및 쓰레기 분리수거는 필수입니다.</p>
              <p className="text-sm">7. 고의적인 시설물 파손 시 배상 책임이 있습니다.</p>
            </div>
          </div>
        </div>
      )}

      {showPenaltyPopup && (
        <div className="penalty-popup" onClick={handleClosePenaltyPopup}>
          <div 
            className="penalty-content" 
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-bold">패널티 안내</h3>
              <button 
                onClick={handleClosePenaltyPopup}
                className="close-button p-1 hover:bg-gray-100 rounded-full"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="space-y-3">
              <p className="text-sm">1. 예약 시간 미준수로 인한 패널티 부여.</p>
              <p className="text-sm">2. No Show 시 해당 학기 동안 예약 제한.</p>
              <p className="text-sm">3. 지각 3회 이상 시 추가 페널티 부여.</p>
              <p className="text-sm">4. 패널티는 관리자 승인 후 조정 가능합니다.</p>
              <p className="text-sm">** 내용 추후 수정 **</p>
            </div>
          </div>
        </div>
      )}

      {/* Simplified QR Modal */}
      {showQRModal && (
        <div 
          className="qrcode-popup" onClick={handleCloseQRModal}
        >
          <div 
            className="qrcode-content"
            onClick={e => e.stopPropagation()}
          >
            {qrCodeUrl && (
              <div className="relative">
                <img 
                  src={qrCodeUrl} 
                  alt="QR Code" 
                  className="max-h-[80vh] w-auto"
                  style={{ maxWidth: '90vw' }}
                />
                <button 
                  onClick={handleCloseQRModal}
                  className="absolute top-2 right-2 p-2 bg-white rounded-full hover:bg-gray-100"
                >
                  <X className="w-6 h-6" />
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default MainPage;
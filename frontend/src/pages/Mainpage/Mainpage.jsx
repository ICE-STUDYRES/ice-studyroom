import { useState, useEffect } from 'react';
import { useMainpageHandlers } from './handlers/MainpageHandlers';
import { useMemberHandlers } from './handlers/MemberHandlers.jsx';
import { usePenaltyHandlers } from './handlers/PenaltyHandlers.jsx';
import ProfileDropdown from './components/ProfileDropdown';
import { NoticePopup, PasswordChangePopup } from "./components/Popups.jsx";
import { LogIn, Home, QrCode } from 'lucide-react';
import alertImage from "../../assets/images/Alert.png";
import { useTokenHandler } from "./handlers/TokenHandler";

const MainPage = () => {
    const {
        showNotice,
        handleReservationClick,
        handleReservationStatusClick,
        handleMyReservationStatusClick,
        handleReservationManageClick,
        handleNoticeClick,
        handleCloseNotice,
      } = useMainpageHandlers();

      const {
        loginForm,
        passwordChangeForm,
        passwordChangeError,
        handleLogout,
        handlePasswordChange,
        handlePasswordChangeClick,
        handlePasswordChangeInputChange,
        showPasswordChangePopup,
        handleLoginClick,
        handleClosePasswordChangePopup,
      } = useMemberHandlers();

      const {
        penaltyReason,
        penaltyEndAt,
      } = usePenaltyHandlers();

      const {
        refreshTokens,
      } = useTokenHandler();
      
      const accessToken = sessionStorage.getItem('accessToken');
      const [recentReservation, setRecentReservation] = useState({
        date: null,
        roomNumber: null,
      });
      const [showPenaltyPopup, setShowPenaltyPopup] = useState(false);

      useEffect(() => {
        const getRecentReservation = async () => {
            try {
                let accessToken = sessionStorage.getItem('accessToken');
                if (!accessToken) {
                  return;
                }
    
                let response = await fetch('/api/reservations/my/latest', {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${accessToken}`,
                        'Content-Type': 'application/json'
                    }
                });
    
                if (response.status === 401) {                    
                    accessToken = await refreshTokens();
    
                    if (accessToken) {
                        return getRecentReservation();
                    }
                }
    
                const result = await response.json();
                if (response.ok && result.data) {
                    setRecentReservation({
                        date: result.data.scheduleDate,
                        roomNumber : result.data.roomNumber,
                        startTime : result.data.startTime,
                        endTime : result.data.endTime,
                    });
                } else {
                    setRecentReservation({ date: null, roomNumber: null });
                }
            } catch (err) {
            }
        };
        getRecentReservation();
    }, []);

  return (
    <div className="max-w-[480px] w-full mx-auto min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white px-4 py-3 flex items-center justify-between border-b">
        <div className="flex items-center gap-2">
          <button 
            onClick={() => {
              window.location.reload();
            }}
            className="p-1.5 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <Home className="w-5 h-5 text-gray-700" />
          </button>
          <h1 className="font-semibold text-gray-900">정보통신공학과</h1>
        </div>
        {accessToken ? (
          <div className="flex items-center gap-2">
            <ProfileDropdown
              userName={loginForm.email}
              userEmail={loginForm.email}
              onLogout={handleLogout}
              onPasswordChange={handlePasswordChangeClick}
            />
          </div>
        ) : (
          <button className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700" onClick={handleLoginClick}>
            <LogIn className="w-4 h-4" />
            <span>로그인</span>
          </button>
        )}
      </div>

      {/* Welcome Message */}
      <div className="bg-white border-b">
        <div className="px-4 py-3">
          <p className="text-sm text-gray-600">환영합니다!</p>
          <p className="text-sm text-gray-600">스터디룸 예약을 확인하고 QR코드를 스캔하여 입장해 주세요.</p>
        </div>
      </div>

      {/* 예약 및 패널티 현황 */}
      <div className="px-4 py-4">
        <div className="w-full rounded-2xl border border-gray-100 bg-white p-4">
          {accessToken ? (
            <>
              <div className="flex justify-between items-start mb-4">
                <h3 className="text-lg font-semibold">이용 현황</h3>
              </div>
              <div 
                onClick={handleMyReservationStatusClick}
                className="w-full p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
              >
                <div className="flex gap-4">
                  {/* 예약 정보 섹션 */}
                  <div className="flex-1">
                    {/* 최근 예약 섹션 */}
                    <div className="border-b border-gray-200 pb-3 mb-3">
                      <h4 className="text-sm font-semibold text-gray-700 mb-2">최근 예약 정보</h4>
                      <table className="w-full text-left text-sm text-gray-600">
                        <tbody>
                          <tr>
                            <td className="font-medium w-20">날짜:</td>
                            <td>{recentReservation.date || "정보 없음"}</td>
                          </tr>
                          <tr>
                            <td className="font-medium w-20">스터디룸:</td>
                            <td>{recentReservation.roomNumber ? `${recentReservation.roomNumber}호` : "정보 없음"}</td>
                          </tr>

                        </tbody>
                      </table>
                    </div>
                    
                    {/* 패널티 섹션 */}
                    <div>
                      <h4 className="text-sm font-semibold text-gray-700 mb-2">페널티 현황</h4>
                      <table className="w-full text-left text-sm text-gray-600">
                        <tbody>
                          <tr>
                            <td className="font-medium w-20">제한 기간:</td>
                            <td className={penaltyEndAt ? "text-red-500" : "text-green-500"}>
                              {penaltyEndAt ? `${penaltyEndAt}` : "제한 없음"}
                            </td>

                          </tr>
                          {penaltyReason && (
                            <tr>
                              <td className="font-medium w-20">사유:</td>
                              <td className="text-red-500">{penaltyReason}</td>
                            </tr>
                          )}
                        </tbody>
                      </table>
                    </div>
                  </div>

                  {/* QR 코드 안내 섹션 */}
                  <div className="flex flex-col items-center justify-center border-l border-gray-200 pl-4 ml-2">
                    <QrCode className="w-20 h-20 text-gray-700 mb-2" />
                    <span className="text-sm text-gray-600">QR 확인</span>
                  </div>
                </div>
              </div>
            </>
          ) : (
            <div className="flex flex-col items-center py-8 gap-4">
              <div className="text-sm text-gray-500">로그인 후 최근 예약 및 페널티 현황을 확인할 수 있습니다</div>
              <button 
                onClick={handleLoginClick}
                className="px-4 py-2 bg-blue-500 text-white rounded-lg text-sm hover:bg-blue-600 transition-colors"
              >
                로그인하기
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Category Section */}
      <div className="px-4">
        <h2 className="text-lg font-semibold mb-4">카테고리</h2>
        <div className="grid grid-cols-2 gap-4">
          <button 
            className="p-4 rounded-2xl border border-gray-100 bg-white flex flex-col items-center"
            onClick={handleReservationClick}
          >
            <div className="text-2xl mb-2">📅</div>
            <div className="text-sm font-medium">예약하기</div>
          </button>
          
          <button 
            className="p-4 rounded-2xl border border-gray-100 bg-white flex flex-col items-center"
            onClick={handleReservationStatusClick}
          >
            <div className="text-2xl mb-2">✔️</div>
            <div className="text-sm font-medium">전체 예약 현황</div>
          </button>
          
          <button 
            className="p-4 rounded-2xl border border-gray-100 bg-white flex flex-col items-center"
            onClick={handleNoticeClick}
          >
            <img src={alertImage} alt="유의사항" className="w-8 h-8 mb-2" />
            <span className="text-sm font-medium">유의사항</span>
          </button>
          
          <button className="p-4 rounded-2xl border border-gray-100 bg-white flex flex-col items-center"
            onClick={handleReservationManageClick}>
            <span className="text-2xl mb-2">✏️</span>
            <span className="text-sm font-medium">연장 및 취소</span>
          </button>
        </div>
      </div>
      
      <NoticePopup 
        showNotice={showNotice} 
        handleCloseNotice={handleCloseNotice} 
        showPenaltyPopup={showPenaltyPopup} 
        setShowPenaltyPopup={setShowPenaltyPopup} 
      />

      <PasswordChangePopup 
        showPasswordChangePopup={showPasswordChangePopup} 
        handleClosePasswordChangePopup={handleClosePasswordChangePopup} 
        handlePasswordChange={handlePasswordChange} 
        handlePasswordChangeInputChange={handlePasswordChangeInputChange} 
        passwordChangeForm={passwordChangeForm} 
        passwordChangeError={passwordChangeError} 
      />
    </div>
  );
};

export default MainPage;
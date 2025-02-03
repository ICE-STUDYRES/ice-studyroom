import { X, XCircle, Clock, LogOut, LogIn, Home, ChevronRight } from 'lucide-react';
import alertImage from "../../assets/images/alert.png";
import logo from "../../assets/images/hufslogo.png";
import { useMainpageHandlers } from './MainpageHandlers';
import { fetchMyReservations } from "../ReservationStatus/MyReservationStatus";
import { useState, useEffect } from 'react';
import ProfileDropdown from './ProfileDropdown';

const MainPage = () => {
    const {
        isLoggedIn,checkInStatus,showNotice,
        showPenaltyPopup,showSigninPopup,showSignUpPopup,signupForm,signupError,
        loginForm,isVerificationSent,isEmailVerified,verificationMessage,verificationSuccess,
        showPasswordChangePopup,
        passwordChangeForm,
        passwordChangeError,
        handleLogin,handleLoginClick,handleLoginInputChange,handleLogout,handleReservationClick,
        handleReservationStatusClick,handleMyReservationStatusClick,handleReservationManageClick,
        handleNoticeClick,handleCloseNotice,handlePenaltyClick,handleClosePenaltyPopup,
        handleCloseSigninPopup,handleCloseSignUpPopup,handleSignupInputChange,handleSignup,handleSignUpClick,
        handleSendVerification,handleVerifyCode,
        handlePasswordChange,
        handlePasswordChangeClick,
        handleClosePasswordChangePopup,
        handlePasswordChangeInputChange,
      } = useMainpageHandlers();

      const [recentReservation, setRecentReservation] = useState({
        date: null,
        roomNumber: null,
      });

      useEffect(() => {
        const getReservations = async () => {
          try {
            const reservations = await fetchMyReservations();
            if (reservations && reservations.length > 0) {
              const recent = reservations[reservations.length -1];
              setRecentReservation({
                date: recent.scheduleDate,
                roomNumber: recent.roomNumber,
              });
            }
          } catch (err) {
            console.error("Failed to fetch reservations:", err);
          }
        };
        getReservations();
      }, []);
  
  return (
    <div className="max-w-[480px] w-full mx-auto min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white px-4 py-3 flex items-center justify-between border-b">
        <div className="flex items-center gap-2">
          <button 
            onClick={() => {
              console.log("Access Token:", localStorage.getItem('accessToken'));
              console.log("Refresh Token:", localStorage.getItem('refreshToken'));
              navigate('/');
            }}
            className="p-1.5 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <Home className="w-5 h-5 text-gray-700" />
          </button>
          <h1 className="font-semibold text-gray-900">정보통신공학과</h1>
        </div>
        {isLoggedIn ? (
          <div className="flex items-center gap-2">
            <ProfileDropdown
              userName={loginForm.email} // 실제 사용자 이름으로 교체 필요
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

      {/* Modified QR Section to Reservation Check */}
      <div className="px-4 py-4">
        <div className="w-full rounded-2xl border border-gray-100 bg-white p-4">
          {isLoggedIn ? (
            <>
              <div className="flex justify-between items-start mb-4">
                <h3 className="text-lg font-semibold">내 예약 현황</h3>
                <span className="bg-blue-500 text-white px-3 py-1 rounded-lg text-sm">
                  {checkInStatus}
                </span>
              </div>
              <button 
                onClick={handleMyReservationStatusClick}
                className="w-full flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
              >
                <div className="flex flex-col gap-2 w-full">
                  <div className="text-xs text-gray-500">
                    <table className="w-full text-left text-sm text-gray-600">
                      <tbody>
                        <tr>
                        <td className="font-medium w-20">최근 예약:</td>
                        <td>{recentReservation.date || "정보 없음"}</td>
                          </tr>
                          <tr>
                            <td className="font-medium w-20">스터디룸:</td>
                            <td>{recentReservation.roomNumber ? `${recentReservation.roomNumber}호` : "정보 없음"}</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                </div>
                <ChevronRight className="w-5 h-5 text-gray-400" />
              </button>
            </>
          ) : (
            <div className="flex flex-col items-center py-8 gap-4">
              <div className="text-sm text-gray-500">로그인 후 예약 현황을 확인할 수 있습니다</div>
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

      {/* Penalty Section */}
      <div className="p-4">
        <div className="w-full rounded-2xl border border-gray-100 bg-white p-4" onClick={handlePenaltyClick}>
          <h2 className="text-lg font-semibold mb-4">패널티 현황</h2>
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <div className="flex items-center">
              <Clock className="w-8 h-8" />
                <span className="ml-2">지각</span>
              </div>
              <span className="ml-2">1회</span>
            </div>
            <div className="flex justify-between items-center">
              <div className="flex items-center">
              <XCircle className="w-8 h-8" />
                <span className="ml-2">No Show</span>
              </div>
              <span className="ml-2">n일 예약 제한</span>
            </div>
          </div>
        </div>
      </div>

      {/* Login Popup */}
      {showSigninPopup && (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" onClick={handleCloseSigninPopup}>
        <div className="bg-white rounded-lg w-96 p-6" onClick={e => e.stopPropagation()}>
          <div className="flex justify-between items-center mb-4">
            <img src={logo} alt="HUFS Logo" className="h-12" />
            <button className="text-2xl" onClick={handleCloseSigninPopup}>×</button>
          </div>
          <form onSubmit={handleLogin} className="space-y-4">
            <input
              type="email"
              name="email"
              value={loginForm.email}
              onChange={handleLoginInputChange}
              placeholder="이메일을 입력해주세요"
              className="w-full p-2 border rounded"
              required
            />
            <input
              type="password"
              name="password"
              value={loginForm.password}
              onChange={handleLoginInputChange}
              placeholder="비밀번호를 입력해주세요"
              className="w-full p-2 border rounded"
              required
            />
            <div className="flex items-center">
              <label className="flex items-center">
                <input type="checkbox" className="mr-2" />
                아이디 기억하기
              </label>
            </div>
            <button type="submit" className="w-full bg-blue-500 text-white p-2 rounded">
              로그인
            </button>
            <div className="flex items-center justify-center">
              <div className="flex-grow h-px bg-gray-200"></div>
              <div className="mx-4 text-gray-500">또는</div>
              <div className="flex-grow h-px bg-gray-200"></div>
            </div>
            <div className="flex items-center justify-center">
              <button
                type="button"
                className="text-gray-600 hover:underline px-4"
                onClick={handleSignUpClick}
              >
                회원가입
              </button>
            </div>
          </form>
        </div>
      </div>
    )}

      {/* Sign Up Popup */}
      {showSignUpPopup && (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" onClick={handleCloseSignUpPopup}>
        <div className="bg-white rounded-lg w-96 p-6" onClick={e => e.stopPropagation()}>
          <div className="flex justify-between items-center mb-4">
            <img src={logo} alt="HUFS Logo" className="h-12" />
            <button className="text-2xl" onClick={handleCloseSignUpPopup}>×</button>
          </div>
          <form onSubmit={handleSignup} className="space-y-4">
            <input
              type="text"
              name="name"
              value={signupForm.name}
              onChange={handleSignupInputChange}
              placeholder="이름을 입력해주세요"
              className="w-full p-2 border rounded"
              required
            />
            <input
              type="text"
              name="studentNum"
              value={signupForm.studentNum}
              onChange={handleSignupInputChange}
              placeholder="학번을 입력해주세요"
              className="w-full p-2 border rounded"
              required
            />
            <div className="space-y-2">
              <div className="flex gap-2">
                <input
                  type="email"
                  name="email"
                  value={signupForm.email}
                  onChange={handleSignupInputChange}
                  placeholder="이메일을 입력해주세요"
                  className="flex-1 p-2 border rounded"
                  required
                />
                <button
                  type="button"
                  onClick={() => handleSendVerification(signupForm.email)}
                  className="bg-gray-200 hover:bg-gray-300 px-4 py-2 rounded text-sm whitespace-nowrap"
                >
                  인증번호 전송
                </button>
              </div>
              {isVerificationSent && (
                <div className="flex gap-2">
                  <input
                    type="text"
                    name="authenticationCode"
                    value={signupForm.authenticationCode}
                    onChange={handleSignupInputChange}
                    placeholder="인증번호 입력"
                    className="w-full p-2 border rounded"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => handleVerifyCode(signupForm.email, signupForm.authenticationCode)}
                    className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded text-sm whitespace-nowrap"
                  >
                    인증확인
                  </button>
                </div>
              )}
              {verificationMessage && (
                <p className={`text-sm ${verificationSuccess ? 'text-green-500' : 'text-red-500'}`}>
                  {verificationMessage}
                </p>
              )}
            </div>
            <input
              type="password"
              name="password"
              value={signupForm.password}
              onChange={handleSignupInputChange}
              placeholder="비밀번호를 입력해주세요"
              className="w-full p-2 border rounded"
              required
            />
            <input
              type="password"
              name="confirmPassword"
              value={signupForm.confirmPassword}
              onChange={handleSignupInputChange}
              placeholder="비밀번호 확인"
              className="w-full p-2 border rounded"
              required
            />
            {signupError && (
              <p className="text-red-500 text-sm">{signupError}</p>
            )}
            <button 
              type="submit" 
              className="w-full bg-blue-500 text-white p-2 rounded disabled:bg-gray-400"
              disabled={!isEmailVerified}
            >
              회원가입
            </button>
          </form>
        </div>
      </div>
    )}

      {/* Notice Popup */}
      {showNotice && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-end justify-center z-50" onClick={handleCloseNotice}>
          <div className="bg-white w-full max-w-2xl rounded-t-lg p-6 transform animate-slide-up" onClick={e => e.stopPropagation()}>
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-bold">스터디룸 이용 주의사항</h3>
              <button onClick={handleCloseNotice} className="p-1 hover:bg-gray-100 rounded-full">
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="space-y-3 text-sm">
              <p>1. 예약 시간 30분 안에 입실하지 않을 경우 자동 취소되며, 노쇼 패널티가 부과됩니다.</p>
              <p>2. 잔여 사용시간이 30분 이상 남아있다면 다음 예약 최대 시간이 1시간으로 조정됩니다.</p>
              <p>3. 퇴실을 30분 이상 늦게할 경우 패널티가 부여됩니다.</p>
              <p>4. 예약 인원 미준수 시 해당 학기 동안 예약이 제한됩니다.</p>
              <p>5. 스터디룸 내 음식물 반입 및 섭취는 엄격히 금지됩니다.</p>
              <p>6. 사용 후 정리정돈 및 쓰레기 분리수거는 필수입니다.</p>
              <p>7. 고의적인 시설물 파손 시 배상 책임이 있습니다.</p>
            </div>
          </div>
        </div>
      )}

      {/* Penalty Popup */}
      {showPenaltyPopup && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-end justify-center z-50" onClick={handleClosePenaltyPopup}>
          <div className="bg-white w-full max-w-2xl rounded-t-lg p-6" onClick={e => e.stopPropagation()}>
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-bold">패널티 안내</h3>
              <button onClick={handleClosePenaltyPopup} className="p-1 hover:bg-gray-100 rounded-full">
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="space-y-3 text-sm">
              <p>1. 예약 시간 미준수로 인한 패널티 부여.</p>
              <p>2. No Show 시 해당 학기 동안 예약 제한.</p>
              <p>3. 지각 3회 이상 시 추가 페널티 부여.</p>
              <p>4. 패널티는 관리자 승인 후 조정 가능합니다.</p>
              <p>** 내용 추후 수정 **</p>
            </div>
          </div>
        </div>
      )}
      
      {showPasswordChangePopup && (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" onClick={handleClosePasswordChangePopup}>
        <div className="bg-white rounded-lg w-96 p-6" onClick={e => e.stopPropagation()}>
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold">비밀번호 변경</h2>
            <button className="text-2xl" onClick={handleClosePasswordChangePopup}>×</button>
          </div>
          <form onSubmit={handlePasswordChange} className="space-y-4">
            <input
              type="password"
              name="currentPassword"
              value={passwordChangeForm.currentPassword}
              onChange={handlePasswordChangeInputChange}
              placeholder="현재 비밀번호"
              className="w-full p-2 border rounded"
              required
            />
            <input
              type="password"
              name="newPassword"
              value={passwordChangeForm.newPassword}
              onChange={handlePasswordChangeInputChange}
              placeholder="새 비밀번호"
              className="w-full p-2 border rounded"
              required
            />
            <input
              type="password"
              name="confirmNewPassword"
              value={passwordChangeForm.confirmNewPassword}
              onChange={handlePasswordChangeInputChange}
              placeholder="새 비밀번호 확인"
              className="w-full p-2 border rounded"
              required
            />
            {passwordChangeError && (
              <p className="text-red-500 text-sm">{passwordChangeError}</p>
            )}
            <button type="submit" className="w-full bg-blue-500 text-white p-2 rounded hover:bg-blue-600">
              비밀번호 변경
            </button>
          </form>
        </div>
      </div>
    )}
    </div>
  );
};

export default MainPage;
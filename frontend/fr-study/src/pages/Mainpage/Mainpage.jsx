import React, { useState, useEffect } from 'react';
import { X, XCircle, Clock, UserX, LogOut, LogIn, Home } from 'lucide-react';
import alertImage from "../../assets/images/alert.png";
import logo from "../../assets/images/hufslogo.png";
import { QRCodeCanvas } from 'qrcode.react';
import { useMainpageHandlers } from './MainpageHandlers';

const MainPageUI = () => {
    const {
        studentId,
        studentName,
        isLoggedIn,
        currentDate,
        roomNumber,
        checkInStatus,
        qrCodeUrl,
        showNotice,
        showPenaltyPopup,
        showQRModal,
        showSigninPopup,
        showSignUpPopup,
        signupForm,
        signupError,
        loginForm,
        loginError,
        handleLogin,
        handleLoginClick,
        handleLoginInputChange,
        handleLogout,
        handleReservationClick,
        handleReservationStatusClick,
        handleReservationManageClick,
        handleNoticeClick,
        handleCloseNotice,
        handlePenaltyClick,
        handleClosePenaltyPopup,
        handleQRClick,
        handleCloseQRModal,
        handleCloseSigninPopup,
        handleCloseSignUpPopup,
        handleSignupInputChange,
        handleSignup,
        handleSignUpClick,
      } = useMainpageHandlers();
  
  return (
    <div className="max-w-[480px] w-full mx-auto min-h-screen bg-gray-50">

      {/* Header */}
      <div className="bg-white px-4 py-3 flex items-center justify-between border-b">
        <div className="flex items-center gap-2">
          <button 
            onClick={() => navigate('/')}
            className="p-1.5 hover:bg-gray-100 rounded-lg transition-colors"
          >
          <Home className="w-5 h-5 text-gray-700" />
          </button>
          <h1 className="font-semibold text-gray-900">정보통신공학과 스터디룸</h1>
        </div>
        {isLoggedIn ? (
          <button className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-700" onClick={handleLogout}>
            <LogOut className="w-4 h-4" />
            <span>로그아웃</span>
          </button>
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

      {/* QR Section */}
      <div className="px-4 py-4">
        <div className="w-full rounded-2xl border border-gray-100 bg-white p-4">
          {isLoggedIn ? (
            <>
              <div className="flex justify-between items-start mb-4">
                <h3 className="text-lg font-semibold">QR 코드</h3>
                <span className="bg-blue-500 text-white px-3 py-1 rounded-lg text-sm">
                  {checkInStatus}
                </span>
              </div>
              <div className="flex gap-4">
                    <div 
                      className="w-32 h-32 bg-gray-50 rounded-lg flex items-center justify-center"
                      onClick={handleQRClick}
                    >
                      <QRCodeCanvas 
                        value={`studentId=${studentId}&studentName=${studentName}`} // QR에 포함할 데이터
                        size={128} // QR 코드 크기
                        level={"H"} // 오류 복원 수준 (L, M, Q, H 중 선택)
                        includeMargin={true} // 여백 포함 여부
                      />
                    </div>
                <div className="flex flex-col gap-2 text-sm text-gray-600">
                  <p>예약 날짜: {currentDate}</p>
                  <p>방 번호: {roomNumber}</p>
                  <p>{studentName}</p>
                  <p>{studentId}</p>
                </div>
              </div>
            </>
          ) : (
            <div className="flex justify-center items-center py-8">
              <div className="text-sm text-gray-500">로그인 후 이용해 주세요</div>
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
            <div className="text-sm font-medium">예약 현황</div>
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
              <UserX className="w-8 h-8" />
                <span className="ml-2">No Show</span>
              </div>
              <span className="ml-2">n일 예약 제한</span>
            </div>
            <div className="flex justify-between items-center">
              <div className="flex items-center">
              <XCircle className="w-8 h-8" />
                <span className="ml-2">취소</span>
              </div>
              <span className="ml-2">0회</span>
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
              placeholder="학교 이메일을 입력해주세요"
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
            {loginError && (
              <p className="text-red-500 text-sm">{loginError}</p>
            )}
            <div className="flex items-center">
              <label className="flex items-center">
                <input type="checkbox" className="mr-2" />
                아이디 기억하기
              </label>
            </div>
            <button type="submit" className="w-full bg-blue-500 text-white p-2 rounded">
              로그인
            </button>
            <div className="text-center text-gray-500">또는</div>
            <button
              type="button"
              className="w-full bg-gray-100 text-gray-700 p-2 rounded border"
              onClick={handleSignUpClick}
            >
              회원가입
            </button>
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
            <input
              type="email"
              name="email"
              value={signupForm.email}
              onChange={handleSignupInputChange}
              placeholder="학교 이메일을 입력해주세요"
              className="w-full p-2 border rounded"
              required
            />
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
            <button type="submit" className="w-full bg-blue-500 text-white p-2 rounded">
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

      {/* QR Modal */}
      {showQRModal && (
        <div 
        className="fixed inset-0 bg-black bg-opacity-70 flex items-center justify-center z-50" 
        onClick={handleCloseQRModal}
        >
          <div 
          className="relative bg-white p-6 rounded-lg w-[80vw] h-[80vh] flex flex-col items-center justify-center" 
          onClick={e => e.stopPropagation()}
          >
            {qrCodeUrl && (
            <>
              <QRCodeCanvas 
                  value={`studentId=${studentId}&studentName=${studentName}`} // QR에 포함할 데이터
                  size={128} // QR 코드 크기
                  level={"H"} // 오류 복원 수준 (L, M, Q, H 중 선택)
                  includeMargin={true} // 여백 포함 여부
                />
              <button 
                onClick={handleCloseQRModal}
                className="absolute top-2 right-2 p-2 bg-white rounded-full hover:bg-gray-100"
              >
                <X className="w-6 h-6" />
              </button>
            </>
            )}
        </div>
      </div>
    )}
    </div>
  );
};

export default MainPageUI;
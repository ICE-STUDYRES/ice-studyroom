import React from "react";
import logo from "../../../assets/images/hufslogo.png";
import { X } from "lucide-react";

export const SignInPopup = ({ showSigninPopup, handleCloseSigninPopup, handleLogin, handleLoginInputChange, loginForm, handleSignUpClick }) => {
  if (!showSigninPopup) return null;
  return (
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
  );
};

export const SignUpPopup = ({ showSignUpPopup, handleCloseSignUpPopup, handleSignup, handleSignupInputChange, signupForm, signupError, verificationMessage, isEmailVerified, handleSendVerification, handleVerifyCode, verificationSuccess, formatTime, verificationTimer }) => {
  if (!showSignUpPopup) return null;
  return (
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
                      className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded text-sm whitespace-nowrap"
                    >
                      인증번호 전송
                    </button>
                  </div>
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
                    {verificationTimer > 0 && (
                      <p className="text-red-500 text-sm">남은 시간: {formatTime(verificationTimer)}</p>
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
  );
};

export const NoticePopup = ({ showNotice, handleCloseNotice, showPenaltyPopup, setShowPenaltyPopup }) => {
  if (!showNotice) return null;
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-end justify-center z-50" onClick={handleCloseNotice}>
          <div className="bg-white w-full max-w-2xl rounded-t-lg p-6 transform animate-slide-up" onClick={e => e.stopPropagation()}>
            <div className="flex justify-between items-center mb-4">
              <div className="flex gap-4">
                <button 
                  className={`text-lg font-bold pb-2 ${!showPenaltyPopup ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-500'}`}
                  onClick={() => setShowPenaltyPopup(false)}
                >
                  스터디룸 이용 주의사항
                </button>
                <button 
                  className={`text-lg font-bold pb-2 ${showPenaltyPopup ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-500'}`}
                  onClick={() => setShowPenaltyPopup(true)}
                >
                  패널티 안내
                </button>
              </div>
              <button onClick={handleCloseNotice} className="p-1 hover:bg-gray-100 rounded-full">
                <X className="w-5 h-5" />
              </button>
            </div>
            
            {/* Notice Content */}
            {!showPenaltyPopup && (
              <div className="space-y-3 text-sm">
                <p>1. 예약 시간 1시간 안에 입실하지 않을 경우 예약은 자동 취소되며, 노쇼 패널티가 부과됩니다.</p>
                <p>2. 예약 인원 미준수 시 해당 학기 동안 예약이 제한됩니다.</p>
                <p>3. 스터디룸 내 음식물 반입 및 섭취는 엄격히 금지됩니다.</p>
                <p>4. 사용 후 정리정돈 및 쓰레기 분리수거는 필수입니다.</p>
                <p>5. 고의적인 시설물 파손 시 배상 책임이 있습니다.</p>
              </div>
            )}

            {/* Penalty Content */}
            {showPenaltyPopup && (
              <div className="space-y-3 text-sm">
                <p>1. 예약 시간 미준수로 인한 패널티 부여.</p>
                <p>2. No Show 시 7일간 패널티 부여.</p>
                <p>3. 10분 이상 지각시 3일간 페널티 부여.</p>
                <p>4. 시작시간 1시간 이전에 취소시 2일간 패널티 부여.</p>
                <p>5. 패널티는 관리자 승인 후 조정 가능합니다.</p>
                <p>6. 패널티가 부여되면 예약 기능이 제한됩니다.</p>
              </div>
            )}
          </div>
        </div>
  );
};

export const PasswordChangePopup = ({ showPasswordChangePopup, handleClosePasswordChangePopup, handlePasswordChange, handlePasswordChangeInputChange, passwordChangeForm, passwordChangeError }) => {
  if (!showPasswordChangePopup) return null;
  return (
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
  );
};

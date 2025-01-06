import React, { useState } from "react";
import SignUpPopup from "./SignUpPopup";
import logo from "../../assets/images/hufslogo.png";
import "./popup.css";

function SignInPopup({ onClose, onLogin }) {
  const [showSignUpPopup, setShowSignUpPopup] = useState(false);

  const handleSignUpClick = () => {
    setShowSignUpPopup(true);
  };

  const handleCloseSignUpPopup = () => {
    setShowSignUpPopup(false);
  };

  const handleLoginClick = () => {
    // 여기에 실제 로그인 로직을 추가할 수 있습니다
    // 예: API 호출 등
    
    onLogin(); // 로그인 상태 변경
    onClose(); // 팝업 닫기
  };

  return (
    <>
      {!showSignUpPopup && (
        <div className="popup-overlay" onClick={onClose}>
          <div className="popup" onClick={(e) => e.stopPropagation()}>
            <div className="popup-header">
              <img src={logo} alt="HUFS Logo" className="popup-logo" />
              <button className="close-button" onClick={onClose}>×</button>
            </div>
            <div className="popup-body">
              <input type="email" placeholder="학교 이메일을 입력해주세요" className="popup-input" />
              <input type="password" placeholder="비밀번호를 입력해주세요" className="popup-input" />
              <div className="popup-options">
                <label>
                  <input type="checkbox" /> 아이디 기억하기
                </label>
              </div>
              <button className="popup-button" onClick={handleLoginClick}>로그인</button>
              <div className="popup-divider">또는</div>
              <button className="popup-signup-button" onClick={handleSignUpClick}>회원가입</button>
            </div>
          </div>
        </div>
      )}
      {showSignUpPopup && <SignUpPopup onClose={handleCloseSignUpPopup} />}
    </>
  );
}

export default SignInPopup;

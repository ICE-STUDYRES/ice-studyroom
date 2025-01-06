import React from "react";
import "./popup.css";
import logo from "../../assets/images/hufslogo.png";

function SignUpPopup({ onClose }) {
  return (
    <div className="popup-overlay" onClick={onClose}>
      <div className="popup" onClick={(e) => e.stopPropagation()}>
        <div className="popup-header">
          <img src={logo} alt="HUFS Logo" className="popup-logo" />
          <button className="close-button" onClick={onClose}>×</button>
        </div>
        <div className="popup-body">
          <input type="text" placeholder="이름을 입력해주세요" className="popup-input" />
          <input type="text" placeholder="학번을 입력해주세요" className="popup-input" />
          <input type="email" placeholder="학교 이메일을 입력해주세요" className="popup-input" />
          <input type="password" placeholder="비밀번호를 입력해주세요" className="popup-input" />
          <input type="password" placeholder="비밀번호 확인" className="popup-input" />
          <button className="popup-button">회원가입</button>
        </div>
      </div>
    </div>
  );
}

export default SignUpPopup;

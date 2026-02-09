{/* 비밀번호 재설정 완료 페이지 */}

import React from "react";
import { useNavigate } from "react-router-dom";

const PasswordResetComplete = () => {
  const navigate = useNavigate();

  const handleConfirm = () => {
    // 확인 버튼 클릭 시 로그인 페이지로 이동
    navigate("/auth/signin");
  };

  return (
    <div className="max-w-[480px] w-full mx-auto min-h-screen bg-gray-50 px-4 py-8">
      {/* 카드 */}
      <div className="bg-white rounded-2xl border border-gray-100 p-6 max-w-[440px] mx-auto text-center">

        {/* 메시지 */}
        <h2 className="text-xl font-semibold text-gray-900 mb-6">
          비밀번호 재설정이
          <br />
          완료되었습니다.
        </h2>

        {/* 버튼 */}
        <button
          onClick={handleConfirm}
          className="w-full p-3 rounded-lg bg-blue-500 text-white font-medium hover:bg-blue-600 transition"
        >
          확인
        </button>
      </div>
    </div>
  );
};

export default PasswordResetComplete;

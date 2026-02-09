{/* 새 비밀번호 입력 페이지 */}

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft } from "lucide-react";

const PasswordReset = () => {
  const navigate = useNavigate();
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = () => {
    if (!password || !confirmPassword) {
      setError("비밀번호를 모두 입력해주세요.");
      return;
    }

    if (password !== confirmPassword) {
      setError("비밀번호가 일치하지 않습니다.");
      return;
    }

    navigate("/password-reset/complete");
  };

  return (
    <div className="max-w-[480px] w-full mx-auto min-h-screen bg-gray-50 px-4 py-8">

      {/* 카드 */}
      <div className="bg-white rounded-2xl border border-gray-100 p-6 max-w-[440px] mx-auto">

        {/* 뒤로가기 */}
        <button
          onClick={() => navigate(-1)}
          className="mb-4 text-gray-600"
        >
          <ArrowLeft size={20} />
        </button>

        {/* 제목 */}
        <div className="text-center space-y-2 mb-6">
          <h2 className="text-xl font-semibold text-gray-900">
            비밀번호 재설정
          </h2>
          <p className="text-xs text-gray-500">
            인증이 완료되었습니다.
            <br />
            설정하실 새로운 비밀번호를 입력해주세요.
          </p>
        </div>

        {/* 새 비밀번호 */}
        <label className="block text-sm font-medium text-gray-700 mb-1">
          새 비밀번호
        </label>
        <input
          type="password"
          placeholder="새 비밀번호 입력"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="w-full p-3 rounded-lg bg-blue-50 outline-none focus:ring-2 focus:ring-blue-500"
        />

        {/* 비밀번호 확인 */}
        <label className="block text-sm font-medium text-gray-700 mt-4 mb-1">
          새 비밀번호 확인
        </label>
        <input
          type="password"
          placeholder="비밀번호 다시 입력"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          className={`w-full p-3 rounded-lg outline-none bg-blue-50
            ${error ? "ring-2 ring-red-400" : "focus:ring-2 focus:ring-blue-500"}
          `}
        />

        {/* 에러 메시지 */}
        {error && (
          <p className="text-red-500 text-xs mt-2">
            {error}
          </p>
        )}

        {/* 버튼 */}
        <button
          onClick={handleSubmit}
          className="w-full mt-6 p-3 rounded-lg bg-blue-500 text-white font-medium hover:bg-blue-600 transition"
        >
          다음
        </button>
      </div>
    </div>
  );
};

export default PasswordReset;
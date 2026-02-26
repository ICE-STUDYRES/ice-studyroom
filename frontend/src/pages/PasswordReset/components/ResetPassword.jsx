{/* 새 비밀번호 입력 페이지 */}

import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import axios from "axios";

const PasswordReset = () => {
  const navigate = useNavigate();

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const location = useLocation();

  /* 앞 페이지에서 받아온 이메일 */
  const email = location.state?.email;

  const handleSubmit = async () => {
    setError("");

    if (!password || !confirmPassword) {
      setError("비밀번호를 모두 입력해주세요.");
      return;
    }

    if (password !== confirmPassword) {
      setError("비밀번호가 일치하지 않습니다.");
      return;
    }

    /* 이메일 정보가 날아갔을 경우 */
    if (!email) {
      setError("이메일 정보가 없습니다. 처음부터 다시 시도해주세요.");
      return;
    }

    setIsLoading(true);

    try {
      const response = await axios.patch("/api/users/password-reset", {
        email: email,
        password: password
      });

      if (response.data.code === "S200") {
        navigate("/password-reset/complete");
      }

    } catch (error) {
      const errorCode = error.response?.data?.code;

      if (errorCode === "B400") {
        setError("올바른 비밀번호 형식이 아닙니다.");
      } else if (errorCode === "E500") {
        setError("서버에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
      } else {
        setError("비밀번호 재설정에 실패했습니다. 다시 시도해주세요.");
      }
    } finally {
      setIsLoading(false);
    }
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
          onChange={(e) => {
            setPassword(e.target.value);
            setError("");
          }}
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
          onChange={(e) => {
            setConfirmPassword(e.target.value);
            setError("");
          }}
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
          disabled={isLoading}
          className="w-full mt-6 p-3 rounded-lg bg-blue-500 text-white font-medium hover:bg-blue-600 transition"
        >
          {isLoading ? '변경 중...' : '다음'}
        </button>
      </div>
    </div>
  );
};

export default PasswordReset;
{/* 인증번호 입력 페이지 */}

import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import axios from "axios";

const CodeVerify = () => {
  const navigate = useNavigate();

  const [code, setCode] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const location = useLocation();

  /* 앞 페이지에서 받아온 이메일 */
  const email = location.state?.email;

  const handleNext = async () => {
    setError("");

    /* 인증번호 미입력 시 */
    if (!code.trim()) {
      setError("인증번호를 입력해주세요.");
      return;
    }

    setIsLoading(true);

    try {
      const response = await axios.post("/api/users/password-reset/email-verification/confirm", {
        email: email,
        code: code
      });

      if (response.data.code === "S200") {
        navigate("/password-reset/new", { state: { email } });
      }
    } catch (error) {
      const errorCode = error.response?.data?.code;

      if (errorCode === "C400") {
        setError("유효하지 않은 인증코드입니다.");
      } else if (errorCode === "E500") {
        setError("Internal Server Error.");
      } else {
        setError("알 수 없는 오류가 발생했습니다. 잠시후 다시 시도해주세요.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleBack = () => {
    navigate("/password-reset/email");
  };

  return (
    <div className="max-w-[480px] w-full mx-auto min-h-screen bg-gray-50 px-4 py-8">
      <div className="bg-white rounded-2xl border border-gray-100 p-6 max-w-[440px] mx-auto">

        <button
          onClick={handleBack}
          className="mb-4 flex items-center text-gray-500 hover:text-gray-700"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>

        {/* 제목+설명 */}
        <div className="text-center space-y-4 mb-6">
          <h2 className="text-xl font-semibold text-gray-900">
            인증번호 입력
          </h2>

          <p className="text-xs text-gray-500">
            해당 이메일로 인증번호가 발송되었습니다.
            <br />
            인증번호를 입력해주세요.
          </p>
        </div>

        {/* 인증번호 입력 */}
        <label className="block text-sm font-medium text-gray-700 mb-1">
          인증번호 입력
        </label>
        <input
          type="text"
          value={code}
          onChange={(e) => {
            setCode(e.target.value);
            setError("");
          }}
          placeholder="인증번호를 입력하세요"
          className={`w-full p-3 rounded-lg outline-none bg-blue-50
            ${error ? "ring-2 ring-red-400" : "focus:ring-2 focus:ring-blue-500"}
          `}
        />

        {/* 에러 메시지 */}
        {error && (
          <p className="text-red-500 text-sm mt-2">
            {error}
          </p>
        )}

        {/* 다음 버튼 */}
        <button
          onClick={handleNext}
          disabled={isLoading}
          className={`w-full mt-6 p-3 rounded-lg text-white font-medium transition-colors ${
            isLoading ? 'bg-blue-300 cursor-not-allowed' : 'bg-blue-500 hover:bg-blue-600'
          }`}
        >
          {isLoading ? '인증번호 확인 중...' : '다음'}
        </button>
      </div>
    </div>
  );
};

export default CodeVerify;

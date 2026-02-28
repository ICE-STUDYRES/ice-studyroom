{/* 이메일 입력 페이지 */}

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft } from "lucide-react";
import axios from "axios";

const EmailVerify = () => {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleBack = () => {
    navigate("/auth/signin");
  }

  const handleNext = async () => {
    setError("");

    /* 이메일 미입력 시 */
    if (!email.trim()) {
      setError("이메일을 입력해주세요.");
      return;
    }

    setIsLoading(true);

    try {
      const response = await axios.post("/api/users/password-reset/email-verification", {
        email: email
      });

      if (response.data.code === "S200") {
        navigate("/password-reset/code", { state: { email } });
      }
    } catch (error) {
      const errorCode = error.response?.data?.code;

      if (errorCode === "B400") {
        setError("올바른 이메일 형식이 아닙니다.");
      } else if (errorCode === "C404") {
        setError("이메일 정보를 확인해주세요.");
      } else if (errorCode === "E500") {
        setError("Internal Server Error.");
      } else {
        setError("알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-[480px] w-full mx-auto min-h-screen bg-gray-50 px-4 py-8">
      <div className="bg-white rounded-2xl border border-gray-100 p-6 max-w-[440px] mx-auto">

        {/* 이전 버튼 */}
        <button
          onClick={handleBack}
          className="mb-4 flex items-center gap-1 text-gray-500 hover:text-gray-700 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>


        {/* 제목+설명 */}
        <div className="text-center space-y-4 mb-6">
          <h2 className="text-xl font-semibold text-gray-900">
            본인 인증
          </h2>

          <p className="text-xs text-gray-500">
            비밀번호 재설정을 위해 먼저 본인 인증을 진행해야 합니다.
            <br />
            이메일을 입력하시면 해당 이메일로 인증 번호가 발송됩니다.
          </p>
        </div>

        {/* 이메일 입력 */}
        <label className="block text-sm font-medium text-gray-700 mb-1">
          이메일 입력
        </label>
        <input
          type="email"
          value={email}
          onChange={(e) => {
            setEmail(e.target.value);
            setError("");
          }}
          placeholder="이메일을 입력하세요"
          className={`w-full p-3 rounded-lg bg-blue-50 outline-none ${
            error ? "ring-2 ring-red-400" : "focus:ring-2 focus:ring-blue-500"
          }`}
        />

        {/* 에러 메시지 */}
        {error && (
          <p className="text-red-500 text-xs mt-2">
            {error}
          </p>
        )}

        {/* 버튼 */}
        <button
          onClick={handleNext}
          disabled={isLoading}
          className={`w-full mt-6 p-3 rounded-lg text-white font-medium transition-colors ${
            isLoading ? 'bg-blue-300 cursor-not-allowed' : 'bg-blue-500 hover:bg-blue-600'
          }`}
        >
          다음
        </button>
      </div>
    </div>
  );
};

export default EmailVerify;
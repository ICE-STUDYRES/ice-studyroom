{/* 인증번호 입력 페이지 */}

import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { ArrowLeft } from "lucide-react";

const DUMMY_CODE = "123456";

const CodeVerify = () => {
  const [code, setCode] = useState("");
  const [error, setError] = useState("");

  const navigate = useNavigate();

  const location = useLocation();
  //페이지 이동하면서 email 값을 같이 들고 이동,그 값을 다음 페이지에서 받기 위해 useLocation()을 씀

  // 이전 페이지에서 전달한 이메일을 꺼내서 email 변수에 저장
  const email = location.state?.email;
  //navigate()로 전달한 데이터 상자=>location.state: {email:"test@hufs.ac.kr"}가 들어있음

  const handleNext = () => {
    setError("");

    if (!code.trim()) {
      setError("인증번호를 입력해주세요.");
      return;
    }

    if (code !== DUMMY_CODE) {
      setError("인증번호 정보를 다시 확인해주세요.");
      return;
    }

    navigate("/password-reset/new");
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
          className="w-full p-3 rounded-lg bg-blue-50 focus:ring-2 focus:ring-blue-500 outline-none"
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
          className="w-full mt-6 p-3 rounded-lg bg-blue-500 text-white font-medium hover:bg-blue-600"
        >
          다음
        </button>
      </div>
    </div>
  );
};

export default CodeVerify;

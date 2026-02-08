import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft } from "lucide-react";

//더미 이메일 DB
const DUMMY_EMAILS = [
  "test@hufs.ac.kr"
];

const EmailVerify = () => {
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");

  const navigate = useNavigate();

  const handleBack = () => {
    navigate("/auth/signin");
  }

  const handleNext = () => {
    setError("");

    //1.이메일 미입력
    if (!email.trim()) {
      setError("이메일을 입력해주세요.");
      return;
    }

    //2.더미 DB에 이메일 없음
    if (!DUMMY_EMAILS.includes(email)) {
      setError("등록되지 않은 이메일입니다.");
      return;
    }

    //3.성공->인증번호 입력 페이지 이동
    navigate("/auth/signin", { state: { email } });
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
          value={email} //입력값을 state와 연결
          onChange={(e) => { //이메일을 입력할 때마다 이메일 state 갱신,에러 메시지 자동 제거
            setEmail(e.target.value);
            setError("");
          }}
          placeholder="이메일을 입력하세요"
          className="w-full p-3 rounded-lg bg-blue-50 focus:ring-2 focus:ring-blue-500 outline-none"
        />

        {/* 에러 메시지 */}
        {error && (
          <p className="text-red-500 text-sm mt-2">
            {error}
          </p>
        )}

        {/* 버튼 */}
        <button
          onClick={handleNext} //클릭 시 handleNext 실행,위에서 만든 로직이 여기서 동작
          className="w-full mt-6 p-3 rounded-lg bg-blue-500 text-white font-medium hover:bg-blue-600 transition-colors"
        >
          다음
        </button>
      </div>
    </div>
  );
};

export default EmailVerify;
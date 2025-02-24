import { React, useState } from "react";
import logo from "../../../assets/images/hufslogo.png";
import { useNavigate } from "react-router-dom";
import { useMemberHandlers } from "../handlers/MemberHandlers";
import { Home } from 'lucide-react';
import TermsAndPrivacyModal  from "../components/TermsandPrivacy";

const SignUpPage = () => {
    const {
        signupForm,
        handleSignupInputChange,
        handleSignup,
        verificationTimer,
        verificationMessage,
        signupError,
        verificationError,
        isEmailVerified,
        handleSendVerification,
        formatTime,
        handleVerifyCode,
        verificationSuccess,
        isSendingEmail
    } = useMemberHandlers();

    const navigate = useNavigate();
    const [isTermsAccepted, setIsTermsAccepted] = useState(false);
    const [isTermsModalOpen, setIsTermsModalOpen] = useState(false);

    return (
        <div className="max-w-[480px] w-full mx-auto min-h-screen bg-gray-50">
            {/* Header - 메인페이지와 동일한 스타일 */}
            <div className="bg-white px-4 py-3 flex items-center justify-between border-b">
                <div className="flex items-center gap-2">
                    <button 
                        onClick={() => navigate("/")}
                        className="p-1.5 hover:bg-gray-100 rounded-lg transition-colors"
                    >
                        <Home className="w-5 h-5 text-gray-700" />
                    </button>
                    <h1 className="font-semibold text-gray-900">정보통신공학과</h1>
                </div>
            </div>

            {/* Main Content */}
            <div className="px-4 py-8">
                <div className="bg-white rounded-2xl border border-gray-100 p-6">
                    <img 
                        src={logo} 
                        alt="HUFS Logo" 
                        className="h-12 mx-auto mb-6" 
                    />
                    
                    <form onSubmit={handleSignup} className="space-y-4">
                        <div>
                            <input
                                type="text"
                                name="name"
                                value={signupForm.name}
                                onChange={handleSignupInputChange}
                                placeholder="이름을 입력해주세요"
                                className="w-full p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-gray-50"
                                required
                            />
                        </div>
                        <div>
                            <input
                                type="text"
                                name="studentNum"
                                value={signupForm.studentNum}
                                onChange={handleSignupInputChange}
                                placeholder="학번을 입력해주세요"
                                className="w-full p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-gray-50"
                                required
                            />
                        </div>
                        <div>
                            <div className="grid grid-cols-3 gap-2 mb-2">
                                <input
                                    type="email"
                                    name="email"
                                    value={signupForm.email}
                                    onChange={handleSignupInputChange}
                                    placeholder="이메일을 입력해주세요"
                                    className="col-span-2 p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-gray-50"
                                    required
                                    disabled={isSendingEmail} // 🔹 이메일 전송 중이면 입력도 잠시 비활성화
                                />
                                <button
                                    type="button"
                                    onClick={() => handleSendVerification(signupForm.email)}
                                    className={`p-2.5 rounded-lg text-sm font-medium transition-colors ${
                                        isSendingEmail
                                            ? 'bg-gray-400 text-white cursor-not-allowed' // 🔹 비활성화 상태
                                            : 'bg-blue-500 hover:bg-blue-600 text-white'
                                    }`}
                                    disabled={isSendingEmail} // 🔹 전송 중에는 버튼 비활성화
                                >
                                    {isSendingEmail ? '메일 발송 중...' : '인증번호 전송'}
                                </button>
                            </div>

                            {/* 🔹 메일이 발송 중일 때 문구 추가 */}
                            {isSendingEmail && (
                                <p className="text-blue-500 text-sm mt-1">메일을 보내는 중입니다. 잠시만 기다려주세요...</p>
                            )}
                                {/* ⬇️ 인증 관련 오류 메시지 표시 ⬇️ */}
                                {verificationError && (
                                    <p className="text-red-500 text-sm mt-1">{verificationError}</p>
                                )}
                            <div className="grid grid-cols-3 gap-2">
                                <input
                                    type="text"
                                    name="authenticationCode"
                                    value={signupForm.authenticationCode}
                                    onChange={handleSignupInputChange}
                                    placeholder="인증번호 입력"
                                    className="col-span-2 p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-gray-50"
                                    required
                                />
                                <button
                                    type="button"
                                    onClick={() => handleVerifyCode(signupForm.email, signupForm.authenticationCode)}
                                    className="bg-blue-500 hover:bg-blue-600 text-white p-2.5 rounded-lg text-sm transition-colors font-medium"
                                >
                                    인증확인
                                </button>
                            </div>
                            {verificationTimer > 0 && (
                                <p className="text-red-500 text-sm mt-1">남은 시간: {formatTime(verificationTimer)}</p>
                            )}
                            {verificationMessage && (
                                <p className={`text-sm mt-1 ${verificationSuccess ? 'text-green-500' : 'text-red-500'}`}>
                                    {verificationMessage}
                                </p>
                            )}
                        </div>
                        <div>
                            <input
                                type="password"
                                name="password"
                                value={signupForm.password}
                                onChange={handleSignupInputChange}
                                placeholder="비밀번호를 입력해주세요"
                                className="w-full p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-gray-50"
                                required
                            />
                        </div>
                        <div>
                            <input
                                type="password"
                                name="confirmPassword"
                                value={signupForm.confirmPassword}
                                onChange={handleSignupInputChange}
                                placeholder="비밀번호 확인"
                                className="w-full p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-gray-50"
                                required
                            />
                        </div>
                        {/* 이용약관 동의 체크박스 & 모달 */}
                        <div className="flex items-center space-x-2">
                            <input 
                                type="checkbox"
                                id="terms"
                                checked={isTermsAccepted}
                                onChange={(e) => setIsTermsAccepted(e.target.checked)}
                                disabled={!isTermsAccepted}
                                className="w-4 h-4 border rounded"
                            />
                            <label htmlFor="terms" className="text-sm text-gray-600">
                                <button 
                                    type="button" 
                                    onClick={() => setIsTermsModalOpen(true)} 
                                    className="text-blue-500 hover:underline"
                                >
                                    이용약관 보기
                                </button> 및 개인정보 처리방침에 동의합니다.
                            </label>
                        </div>

                        {signupError && (
                            <p className="text-red-500 text-sm">{signupError}</p>
                        )}
                        <button 
                            type="submit" 
                            className="w-full bg-blue-500 text-white p-3 rounded-lg hover:bg-blue-600 transition-colors font-medium disabled:bg-gray-400 disabled:hover:bg-gray-400"
                            disabled={!isEmailVerified || !isTermsAccepted}
                        >
                            회원가입
                        </button>
                    </form>
                    
                    <div className="flex items-center justify-center my-6">
                        <div className="flex-grow h-px bg-gray-200"></div>
                        <div className="mx-4 text-sm text-gray-500">또는</div>
                        <div className="flex-grow h-px bg-gray-200"></div>
                    </div>

                    <button 
                        className="w-full p-3 border border-gray-200 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors font-medium"
                        onClick={() => navigate("/auth/signin")}
                    >
                        로그인
                    </button>
                </div>
            </div>
            {/* 이용약관 모달 */}
            <TermsAndPrivacyModal  
                isOpen={isTermsModalOpen} 
                onClose={() => setIsTermsModalOpen(false)} 
                onAccept={() => setIsTermsAccepted(true)}
                onReject={() => setIsTermsAccepted(false)}
                isTermsAccepted={isTermsAccepted}
            />
        </div>
    );
};

export default SignUpPage;
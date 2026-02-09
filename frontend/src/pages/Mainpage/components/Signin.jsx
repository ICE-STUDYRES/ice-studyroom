import React from "react";
import { useEffect } from 'react';
import logo from "../../../assets/images/hufslogo.png";
import { useNavigate } from "react-router-dom";
import { useMemberHandlers } from "../handlers/MemberHandlers";
import { Home } from 'lucide-react';

const SignInPage = () => {
    const {
        loginForm,
        handleLogin,
        handleLoginInputChange,
        loginError
    } = useMemberHandlers();
    
    const navigate = useNavigate();
    
  useEffect (() => {
    const handleKeyPress = (event) => {
      if (event.ctrlKey && event.shiftKey && event.key === 'K') {
        navigate('/auth/admin-signin');
        return;
      }
  };
  window.addEventListener('keydown', handleKeyPress);
  return () => {
    window.removeEventListener('keydown', handleKeyPress);
  };
}
)

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
                    
                    <form onSubmit={handleLogin} className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">이메일</label>
                            <input
                                type="email"
                                name="email"
                                value={loginForm.email}
                                onChange={handleLoginInputChange}
                                placeholder="이메일을 입력해주세요"
                                className="w-full p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-gray-50"
                                required
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">비밀번호</label>
                            <input
                                type="password"
                                name="password"
                                value={loginForm.password}
                                onChange={handleLoginInputChange}
                                placeholder="비밀번호를 입력해주세요"
                                className="w-full p-2.5 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-gray-50"
                                required
                            />
                        </div>

                        {/* 🔹 로그인 오류 메시지 표시 */}
                        {loginError && (
                            <p className="text-red-500 text-sm mt-1">{loginError}</p>
                        )}

                        <button 
                            type="submit" 
                            className="w-full bg-blue-500 text-white p-3 rounded-lg hover:bg-blue-600 transition-colors font-medium"
                        >
                            로그인
                        </button>
                    </form>
                    
                    <div className="flex items-center justify-center my-6">
                        <div className="flex-grow h-px bg-gray-200"></div>
                        <div className="mx-4 text-sm text-gray-500">또는</div>
                        <div className="flex-grow h-px bg-gray-200"></div>
                    </div>

                    <div className="flex flex-col gap-3">
                    <button 
                        className="w-full p-3 border border-gray-200 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors font-medium"
                        onClick={() => navigate("/auth/signup")}
                    >
                        회원가입
                    </button>

                    <button 
                        className="w-full p-3 border border-gray-200 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors font-medium"
                        onClick={() => navigate("/password-reset/email")}
                    >
                        비밀번호 찾기
                    </button>
                    </div>

                </div>
            </div>
        </div>
    );
};

export default SignInPage;
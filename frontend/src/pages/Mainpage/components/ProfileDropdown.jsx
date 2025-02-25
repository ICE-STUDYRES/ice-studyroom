import { useState, useRef, useEffect } from 'react';
import { User, LogOut, Key } from 'lucide-react';
import { useTokenHandler } from "../handlers/TokenHandler";

const ProfileDropdown = ({ onLogout, onPasswordChange }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [userName, setUserName] = useState('');
  const [userEmail, setUserEmail] = useState('');
  const dropdownRef = useRef(null);

  const {
    refreshTokens,
  } = useTokenHandler();

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  useEffect(() => {
    const fetchUserInfo = async () => {
        let accessToken = sessionStorage.getItem('accessToken');
        if (!accessToken) return;

        try {
            let response = await fetch('/api/users', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json',
                },
            });

            if (response.status === 401) {
                console.warn('토큰이 만료됨. 새로고침 시도.');

                accessToken = await refreshTokens();

                if (accessToken) {
                    return fetchUserInfo();
                } else {
                    console.error('토큰 갱신 실패. 로그아웃 필요.');
                    return;
                }
            }

            const result = await response.json();
            if (result.code === 'S200' && result.data) {
                setUserName(result.data.name);
                setUserEmail(result.data.email);
            }
        } catch (error) {
            console.error('🚨 사용자 정보 가져오기 실패:', error);
        }
    };

    fetchUserInfo();

}, []);

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="p-1.5 hover:bg-gray-100 rounded-lg transition-colors"
      >
        <User className="w-5 h-5 text-gray-700" />
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-64 bg-white rounded-lg shadow-lg border border-gray-100 z-50">
          <div className="p-4 border-b border-gray-100">
            <p className="font-medium text-gray-900">{userName}</p>
            <p className="text-sm text-gray-500">{userEmail}</p>
          </div>

          <div className="p-2">
            <button
              onClick={() => {
                onPasswordChange();
                setIsOpen(false);
              }}
              className="w-full flex items-center gap-2 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 rounded-md"
            >
              <Key className="w-4 h-4" />
              비밀번호 변경
            </button>

            <button
              onClick={() => {
                onLogout();
                setIsOpen(false);
              }}
              className="w-full flex items-center gap-2 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 rounded-md"
            >
              <LogOut className="w-4 h-4" />
              로그아웃
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProfileDropdown;

import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

export const useMainpageHandlers = () => {
    const [currentDate, setCurrentDate] = useState("");
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [roomNumber, setRoomNumber] = useState("305-1");
    const [checkInStatus, setCheckInStatus] = useState("입실");
    const [studentId] = useState("201902149");
    const [studentName] = useState("양재원");
    const [qrCodeUrl, setQrCodeUrl] = useState(null);
    const [showNotice, setShowNotice] = useState(false);
    const [showPenaltyPopup, setShowPenaltyPopup] = useState(false);
    const [showQRModal, setShowQRModal] = useState(false);
    const [showSigninPopup, setShowSigninPopup] = useState(false);
    const [showSignUpPopup, setShowSignUpPopup] = useState(false);
    const [signupForm, setSignupForm] = useState({
      name: '',
      studentNum: '',
      email: '',
      password: '',
      confirmPassword: ''
    });
    const [signupError, setSignupError] = useState('');
    const [loginForm, setLoginForm] = useState({
      email: '',
      password: ''
    });
    const [loginError, setLoginError] = useState('');
    const [tokens, setTokens] = useState(null);
  
    useEffect(() => {
      const today = new Date();
      const days = ['일', '월', '화', '수', '목', '금', '토'];
      const year = today.getFullYear();
      const month = String(today.getMonth() + 1).padStart(2, '0');
      const day = String(today.getDate()).padStart(2, '0');
      const dayOfWeek = days[today.getDay()];
  
      setCurrentDate(`${year}.${month}.${day} (${dayOfWeek})`);
  
      const storedLoginStatus = localStorage.getItem("isLoggedIn");
      if (storedLoginStatus === "true") {
        setIsLoggedIn(true);
      }
    }, []);
  
    useEffect(() => {
      const fetchQRCode = async () => {
        try {
          const response = await fetch(`/api/qr/${studentId}/${studentName}`);
          const blob = await response.blob();
          const url = URL.createObjectURL(blob);
          setQrCodeUrl(url);
        } catch (error) {
          console.error('QR 코드 로드 실패:', error);
        }
      };
  
      fetchQRCode();
    }, [studentId, studentName]);
  
    useEffect(() => {
      // 요청 인터셉터
      const requestInterceptor = axios.interceptors.request.use(
        (config) => {
          const token = localStorage.getItem('accessToken');
          if (token) {
            config.headers.Authorization = `Bearer ${token}`;
          }
          return config;
        },
        (error) => Promise.reject(error)
      );
    
      // 응답 인터셉터
      const responseInterceptor = axios.interceptors.response.use(
        (response) => response, // 정상 응답
        async (error) => {
          const originalRequest = error.config;
    
          // 401 에러이고 아직 재시도하지 않은 경우
          if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true; // 재시도 플래그 설정
            console.log('🔄 액세스 토큰 만료, 갱신 시도 중...');
    
            try {
              const newAccessToken = await refreshTokens(); // 토큰 갱신 시도
    
              if (newAccessToken) {
                console.log('✅ 액세스 토큰 갱신 성공');
                originalRequest.headers.Authorization = `Bearer ${newAccessToken}`; // 새로운 토큰 추가
                return axios(originalRequest); // 실패했던 요청 재시도
              } else {
                console.log('❌ 토큰 갱신 실패 - 로그아웃 처리');
                handleLogout(); // 로그아웃 처리
                return Promise.reject(error);
              }
            } catch (refreshError) {
              console.log('❌ 토큰 갱신 중 에러:', refreshError.message);
              handleLogout();
              return Promise.reject(refreshError);
            }
          }
    
          return Promise.reject(error); // 다른 에러는 그대로 반환
        }
      );
    
      return () => {
        axios.interceptors.request.eject(requestInterceptor);
        axios.interceptors.response.eject(responseInterceptor);
      };
    }, []);
    
    const navigate = useNavigate();  
    const handleReservationClick = () => {
      if (isLoggedIn) {
        navigate('/reservation/room');
      } else {
        alert('로그인 후 이용 가능합니다.');
      }
    };
    const handleReservationStatusClick = () => navigate('/ReservationStatus');
    const handleReservationManageClick = () => {
      if (isLoggedIn) {
        navigate('/reservation/manage');
      } else {
        alert('로그인 후 이용 가능합니다.');
      }
    };
    const handleNoticeClick = () => setShowNotice(true);
    const handleCloseNotice = () => setShowNotice(false);
    const handlePenaltyClick = () => {
      if (isLoggedIn) {
        setShowPenaltyPopup(true);
      } else {
        alert('로그인 후 이용 가능합니다.');
      }
    };
    const handleClosePenaltyPopup = () => setShowPenaltyPopup(false);
    const handleQRClick = () => setShowQRModal(true);
    const handleCloseQRModal = () => setShowQRModal(false);
    const handleLoginClick = () => setShowSigninPopup(true);
    const handleCloseSigninPopup = () => setShowSigninPopup(false);
    const handleSignUpClick = () => {
      setShowSigninPopup(false);
      setShowSignUpPopup(true);
    };
    const handleCloseSignUpPopup = () => setShowSignUpPopup(false);
    const handleSignupInputChange = (e) => {
      const { name, value } = e.target;
      setSignupForm(prev => ({
        ...prev,
        [name]: value
      }));
    };
  
    const handleSignup = async (e) => {
      e.preventDefault();
      setSignupError('');
    
      if (!signupForm.email.endsWith('@hufs.ac.kr')) {
        setSignupError('학교 이메일(@hufs.ac.kr)만 사용 가능합니다.');
        return;
      }
    
      if (signupForm.password !== signupForm.confirmPassword) {
        setSignupError('비밀번호가 일치하지 않습니다.');
        return;
      }
    
      try {
        const response = await axios.post('/api/users', {
          email: signupForm.email,
          password: signupForm.password,
          name: signupForm.name,
          studentNum: signupForm.studentNum
        });
    
        if (response.data.code === 'S200') {
          alert('회원가입이 완료되었습니다.');
          handleCloseSignUpPopup();
          setShowSigninPopup(true);
        } else {
          setSignupError(response.data.message || '회원가입 중 오류가 발생했습니다.');
        }
      } catch (error) {
        console.error('Signup error:', error);
        setSignupError('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
      }
  };
  
    const handleLoginInputChange = (e) => {
      const { name, value } = e.target;
      setLoginForm(prev => ({
        ...prev,
        [name]: value
      }));
    };
  
    // 로그인 처리
    const handleLogin = async (e) => {
      e.preventDefault();
      setLoginError('');
    
      try {
        const response = await axios.post('api/users/login', loginForm, {
          headers: {
            'Content-Type': 'application/json'
          }
        });
    
        const data = response.data;
    
        if (data.code === 'S200') {
          const newTokens = {
            accessToken: data.data.accessToken,
            refreshToken: data.data.refreshToken
          };
  
          // 토큰 정보를 콘솔에 출력
          console.log('Login Tokens:', {
            accessToken: newTokens.accessToken,
            refreshToken: newTokens.refreshToken
          });
          setTokens(newTokens);
          localStorage.setItem('accessToken', newTokens.accessToken);
          localStorage.setItem('refreshToken', newTokens.refreshToken);
          localStorage.setItem('isLoggedIn', 'true');
          setIsLoggedIn(true);
          setShowSigninPopup(false);
        } else {
          setLoginError(data.message || '로그인에 실패했습니다.');
        }
      } catch (error) {
        console.error('Login error:', error);
        setLoginError('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
      }
  
  };
  
    // 로그아웃 핸들러
    const handleLogout = async () => {
      if (!tokens || !tokens.accessToken) {
        console.warn("No tokens available for logout.");
        clearAuthData();
        return;
      }
    
      try {
        const response = await axios.post(
          '/api/users/logout',
          {
            refreshToken: tokens.refreshToken, // 요청 본문
          },
          {
            headers: {
              Authorization: `Bearer ${tokens.accessToken}`, // 명세에 맞게 수정
              'Content-Type': 'application/json',
            },
          }
        );
    
        if (response.data.code === 'S200') {
          clearAuthData();
        } else {
          console.error('Logout failed:', response.data.message);
          clearAuthData();
        }
      } catch (error) {
        console.error('Logout error:', error);
        clearAuthData();
      }
    };
    
    // 인증 관련 데이터를 정리하는 헬퍼 함수
    const clearAuthData = () => {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('isLoggedIn');
      setTokens({ accessToken: null, refreshToken: null });
      setIsLoggedIn(false);
    };
  
    const refreshTokens = async () => {
      try {
        console.log('📤 토큰 갱신 요청 전송 중...');
        const currentAccessToken = localStorage.getItem('accessToken');
        const currentRefreshToken = localStorage.getItem('refreshToken');
    
        if (!currentAccessToken || !currentRefreshToken) {
          throw new Error('토큰이 존재하지 않습니다.');
        }
    
        const response = await axios.post('/api/users/refresh', 
          { refreshToken: currentRefreshToken },
          {
            headers: {
              'Authorization': `Bearer ${currentAccessToken}`,
              'Content-Type': 'application/json'
            }
          }
        );
    
        const { code, data, message } = response.data;
    
        if (code === 'S200' && data) {
          const { accessToken, refreshToken } = data;
    
          // 새로운 토큰 저장
          localStorage.setItem('accessToken', accessToken);
          localStorage.setItem('refreshToken', refreshToken);
          setTokens({ accessToken, refreshToken }); // 상태 업데이트
    
          // 새로 발급받은 토큰 출력
          console.log('✅ 새로 발급된 AccessToken:', accessToken);
          console.log('✅ 새로 발급된 RefreshToken:', refreshToken);
    
          return accessToken; // 새로 발급된 AccessToken 반환
        } else {
          console.log('❌ 갱신 실패 - 서버 응답:', code, message);
          throw new Error('토큰 갱신 실패');
        }
      } catch (error) {
        console.log('❌ 토큰 갱신 에러:', error.message);
        if (error.response?.data) {
          console.log('서버 에러 응답:', error.response.data);
        }
        handleLogout();
        return null;
      }
    };

  return {
    isLoggedIn,
    currentDate,
    roomNumber,
    checkInStatus,
    qrCodeUrl,
    showNotice,
    showPenaltyPopup,
    showQRModal,
    showSigninPopup,
    showSignUpPopup,
    signupForm,
    signupError,
    loginForm,
    loginError,
    handleLogin,
    handleLoginClick,
    handleLoginInputChange,
    handleLogout,
    handleReservationClick,
    handleReservationStatusClick,
    handleReservationManageClick,
    handleNoticeClick,
    handleCloseNotice,
    handlePenaltyClick,
    handleClosePenaltyPopup,
    handleQRClick,
    handleCloseQRModal,
    handleCloseSigninPopup,
    handleCloseSignUpPopup,
    handleSignupInputChange,
    handleSignup,
    handleSignUpClick,
  };
};

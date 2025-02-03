import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

export const useMainpageHandlers = () => {
    const [currentDate, setCurrentDate] = useState("");
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [checkInStatus, setCheckInStatus] = useState("예약완료");
    const [qrCodeUrl, setQrCodeUrl] = useState(null);
    const [showNotice, setShowNotice] = useState(false);
    const [showPenaltyPopup, setShowPenaltyPopup] = useState(false);
    const [showQRModal, setShowQRModal] = useState(false);
    const [showSigninPopup, setShowSigninPopup] = useState(false);
    const [showSignUpPopup, setShowSignUpPopup] = useState(false);
    const [signupForm, setSignupForm] = useState({
      email: '',
      password: '',
      confirmPassword: '',
      name: '',
      studentNum: '',
      authenticationCode: '',
      isAuthenticated: false
  });
    const [signupError, setSignupError] = useState('');
    const [loginForm, setLoginForm] = useState({
      email: '',
      password: ''
    });
    const [isVerificationSent, setIsVerificationSent] = useState(false);
    const [isEmailVerified, setIsEmailVerified] = useState(false);
    const [verificationMessage, setVerificationMessage] = useState('');
    const [verificationSuccess, setVerificationSuccess] = useState(false);

    const [showPasswordChangePopup, setShowPasswordChangePopup] = useState(false);
    const [passwordChangeForm, setPasswordChangeForm] = useState({
      currentPassword: '',
      newPassword: '',
      confirmNewPassword: ''
    });
    const [passwordChangeError, setPasswordChangeError] = useState('');
    
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

    const navigate = useNavigate();  
    const handleReservationClick = () => {
      if (isLoggedIn) {
        navigate('/reservation/room');
      } else {
        alert('로그인 후 이용 가능합니다.');
      }
    };
    const handleMyReservationStatusClick = () => navigate('/MyReservationStatus');
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
  
      // 이메일 형식 확인
      if (!signupForm.email.endsWith('@hufs.ac.kr')) {
          setSignupError('학교 이메일(@hufs.ac.kr)만 사용 가능합니다.');
          return;
      }
  
      // 비밀번호 일치 확인
      if (signupForm.password !== signupForm.confirmPassword) {
          setSignupError('비밀번호가 일치하지 않습니다.');
          return;
      }
  
      // 인증번호 입력 여부 확인
      if (!signupForm.authenticationCode || signupForm.authenticationCode.trim() === '') {
          setSignupError('인증번호를 입력해주세요.');
          return;
      }
  
      // 이메일 인증 여부 확인
      if (!signupForm.isAuthenticated) {
          setSignupError('이메일 인증을 진행해주세요.');
          return;
      }
  
      try {
          const response = await axios.post('/api/users', {
              email: signupForm.email,
              isAuthenticated: signupForm.isAuthenticated,
              authenticationCode: signupForm.authenticationCode,
              password: signupForm.password,
              name: signupForm.name,
              studentNum: signupForm.studentNum,
          });
  
          if (response.data.code === 'S200') {
              alert('회원가입이 완료되었습니다.');
              setSignupForm({
                  email: '',
                  password: '',
                  confirmPassword: '',
                  name: '',
                  studentNum: '',
                  authenticationCode: '',
                  isAuthenticated: false,
              });
              setShowSignUpPopup(false); // 회원가입 팝업 닫기
          } else {
              setSignupError(response.data.message || '회원가입 중 오류가 발생했습니다.');
          }
      } catch (error) {
          if (error.response?.data?.code === 'C400') {
              setSignupError(error.response.data.message);
          } else {
              console.error('Signup error:', error);
              setSignupError('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
          }
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
    
      try {
        const response = await axios.post('/api/users/login', {
          email: loginForm.email,
          password: loginForm.password,
        });
    
        if (response.data.code === 'S200') {
          const accessToken = response.data.data.accessToken;
          const refreshToken = response.data.data.refreshToken;
    
          // 토큰과 로그인 상태를 로컬 스토리지에 저장
          localStorage.setItem('accessToken', accessToken);
          localStorage.setItem('refreshToken', refreshToken);
          localStorage.setItem('isLoggedIn', 'true'); // 로그인 상태 저장

          console.log('Login Tokens:', { accessToken, refreshToken }); // 로그인 시 토큰 출력
    
          setIsLoggedIn(true);
          setShowSigninPopup(false); // 로그인 팝업 닫기
        }
      } catch (error) {
        console.error('Login error:', error);
      }
    };  
 
    // 로그아웃 핸들러
    const handleLogout = async () => {
      try {
          const accessToken = localStorage.getItem('accessToken');
          const refreshToken = localStorage.getItem('refreshToken');
  
          if (!accessToken || !refreshToken) {
              console.warn("No tokens found, clearing storage and redirecting.");
              localStorage.clear();
              setIsLoggedIn(false);
              navigate('/');
              return;
          }
  
          const response = await axios.post(
              '/api/users/logout',
              { refreshToken }, 
              {
                  headers: {
                      'Authorization': `Bearer ${accessToken}`
                  }
              }
          );
  
          console.log("Logout response:", response); // 응답 로그 출력
  
          if (response.status !== 200) {  // response.ok 대신 response.status 사용
              console.warn("Logout request failed. Status:", response.status);
          }
  
          localStorage.clear();
          setIsLoggedIn(false);
          navigate('/');
      } catch (error) {
          console.error("Logout failed:", error);
          localStorage.clear();
          setIsLoggedIn(false);
          navigate('/');
      }
  };  
  
  const refreshTokens = async () => {
    try {
        const refreshToken = localStorage.getItem('refreshToken');
        const accessToken = localStorage.getItem('accessToken');

        if (!refreshToken) {
            console.error("No refresh token found. Logging out.");
            handleLogout();
            return null;
        }

        const response = await axios.post(
            '/api/users/refresh',
            { refreshToken },
            {
                headers: {
                    'Authorization': `Bearer ${accessToken}`
                }
            }
        );

        if (response.data.code !== "S200" || !response.data.data) {
            console.error("Failed to refresh token. Unexpected response:", response.data);
            handleLogout();
            return null;
        }

        const { accessToken: newAccessToken, refreshToken: newRefreshToken } = response.data.data;
        localStorage.setItem('accessToken', newAccessToken);
        localStorage.setItem('refreshToken', newRefreshToken);

        // Log a success message
        console.log("Tokens refreshed successfully:", { newAccessToken, newRefreshToken });

        return newAccessToken;
    } catch (error) {
        console.error("Error refreshing token:", error.response?.data || error);

        if (error.response?.status === 401) {
            handleLogout();
        }
        return null;
    }
};

    const handleSendVerification = async (email) => {
      setVerificationMessage('');
      try {
        const response = await axios.post('/api/users/email-verification', { email });
    
        if (response.data.code === 'S200') {
          setIsVerificationSent(true); // 인증번호 전송 상태 업데이트
          setVerificationMessage(response.data.data.message || '인증 메일이 전송되었습니다.');
        } else {
          setVerificationMessage(response.data.message || '인증 메일 발송 실패');
        }
      } catch (error) {
        if (error.response?.data?.code === 'B409') {
          setVerificationMessage('이미 사용 중인 이메일입니다.');
        } else if (error.response?.data?.code === 'B429') {
          setVerificationMessage('인증 메일이 이미 발송되었습니다.');
        } else {
          console.error('Verification error:', error);
          setVerificationMessage('인증 메일 전송 중 오류가 발생했습니다.');
        }
      }
    };
    
    const handleVerifyCode = async (email, code) => {
      try {
        const response = await axios.post('/api/users/email-verification/confirm', { email, code });
    
        if (response.data.code === 'S200') {
          setIsEmailVerified(true);
          setVerificationSuccess(true);
          setVerificationMessage('이메일 인증이 완료되었습니다.');
    
          // 이메일 인증 완료 후 signupForm의 isAuthenticated 업데이트
          setSignupForm(prev => ({
            ...prev,
            isAuthenticated: true,
          }));
        } else {
          setVerificationMessage(response.data.message || '인증 코드 확인 실패');
        }
      } catch (error) {
        console.error('Verification code error:', error);
        setVerificationMessage('서버 오류로 인증 코드를 확인하지 못했습니다.');
      }
    };
    
    const handlePasswordChangeClick = () => {
      setShowPasswordChangePopup(true);
      setShowSigninPopup(false);
    };
    
    const handleClosePasswordChangePopup = () => {
      setShowPasswordChangePopup(false);
      setPasswordChangeForm({
        currentPassword: '',
        newPassword: '',
        confirmNewPassword: ''
      });
      setPasswordChangeError('');
    };
    
    const handlePasswordChangeInputChange = (e) => {
      const { name, value } = e.target;
      setPasswordChangeForm(prev => ({
        ...prev,
        [name]: value
      }));
    };
    
    const handlePasswordChange = async (e) => {
      e.preventDefault();
    
      const { currentPassword, newPassword, confirmNewPassword } = passwordChangeForm;
    
      if (newPassword !== confirmNewPassword) {
        setPasswordChangeError('새 비밀번호가 일치하지 않습니다.');
        return;
      }
    
      if (currentPassword === newPassword) {
        setPasswordChangeError('현재 비밀번호와 새 비밀번호는 달라야 합니다.');
        return;
      }
    
      try {
        const response = await fetch('/api/users/password', {
          method: 'PATCH',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
          },
          body: JSON.stringify({
            currentPassword,
            updatedPassword: newPassword,
            updatedPasswordForCheck: confirmNewPassword
          })
        });
    
        const result = await response.json();
        if (result.code !== 'S200') {
          throw new Error(result.message || '비밀번호 변경에 실패했습니다.');
        }
    
        handleClosePasswordChangePopup();
        // 성공 메시지 표시
        alert(result.data || '비밀번호가 성공적으로 변경되었습니다.');
      } catch (error) {
        setPasswordChangeError(error.message);
      }
    };    

  return {
    isLoggedIn,
    currentDate,
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
    isEmailVerified,verificationMessage,verificationSuccess,isVerificationSent,
    showPasswordChangePopup,
    passwordChangeForm,
    passwordChangeError,
    handleLogin,
    handleLoginClick,
    setIsLoggedIn,
    handleLoginInputChange,
    handleLogout,
    refreshTokens,
    handleReservationClick,
    handleReservationStatusClick,
    handleMyReservationStatusClick,
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
    handleSendVerification,
    handleVerifyCode,
    handlePasswordChange,
    handlePasswordChangeClick,
    handleClosePasswordChangePopup,
    handlePasswordChangeInputChange,
  };
};

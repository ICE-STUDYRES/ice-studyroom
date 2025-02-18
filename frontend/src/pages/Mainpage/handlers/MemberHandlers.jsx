import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useNotification } from '../../Notification/Notification';

export const useMemberHandlers = () => {
    useEffect(() => {
        const storedLoginStatus = localStorage.getItem("isLoggedIn");
        if (storedLoginStatus === "true") {
          setIsLoggedIn(true);
        }
      }, []);
      
    const navigate = useNavigate();
    const [isLoggedIn, setIsLoggedIn] = useState(false);
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
    const [loginForm, setLoginForm] = useState({ email: '', password: '' });
    const [isEmailVerified, setIsEmailVerified] = useState(false);
    const [verificationMessage, setVerificationMessage] = useState('');
    const [verificationSuccess, setVerificationSuccess] = useState(false);
    const [passwordChangeForm, setPasswordChangeForm] = useState({
        currentPassword: '',
        newPassword: '',
        confirmNewPassword: ''
    });
    const [passwordChangeError, setPasswordChangeError] = useState('');
    const { addNotification } = useNotification();
    const [showSigninPopup, setShowSigninPopup] = useState(false);
    const [showSignUpPopup, setShowSignUpPopup] = useState(false);
    const [showPasswordChangePopup, setShowPasswordChangePopup] = useState(false);

    const handleSignUpClick = () => {
        setShowSignUpPopup(true);
        setShowSigninPopup(false);
      };

    const handleLoginClick = () => setShowSigninPopup(true);

    const handleSignupInputChange = (e) => {
        const { name, value } = e.target;
        setSignupForm(prev => ({ ...prev, [name]: value }));
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
        if (!signupForm.authenticationCode || signupForm.authenticationCode.trim() === '') {
            setSignupError('인증번호를 입력해주세요.');
            return;
        }
        if (!signupForm.isAuthenticated) {
            setSignupError('이메일 인증을 진행해주세요.');
            return;
        }
        try {
            const response = await axios.post('/api/users', signupForm);
            if (response.data.code === 'S200') {
                addNotification('signup', 'success');
                setSignupForm({
                    email: '', password: '', confirmPassword: '', name: '', studentNum: '', authenticationCode: '', isAuthenticated: false
                });
            } else {
                addNotification('signup', 'error', response.data.message);
            }
        } catch (error) {
            setVerificationMessage('회원가입 실패');
        }
    };

    const handleLoginInputChange = (e) => {
        const { name, value } = e.target;
        setLoginForm(prev => ({ ...prev, [name]: value }));
    };

    const handlePasswordChangeClick = () => {
        setShowPasswordChangePopup(true);
      };

    const handlePasswordChangeInputChange = (e) => {
    const { name, value } = e.target;
    setPasswordChangeForm(prev => ({
        ...prev,
        [name]: value
    }));
    };

    const handleCloseSigninPopup = () => setShowSigninPopup(false);    
    const handleCloseSignUpPopup = () => setShowSignUpPopup(false);
    const handleClosePasswordChangePopup = () => {
        setShowPasswordChangePopup(false);
        setPasswordChangeForm({
          currentPassword: '',
          newPassword: '',
          confirmNewPassword: ''
        });
        setPasswordChangeError('');
      };

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
    
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', refreshToken);
            localStorage.setItem('isLoggedIn', 'true');
    
            setIsLoggedIn(true);    
            setShowSigninPopup(false);
          }
        } catch (error) {
          console.error('Login error:', error);
        }
    };

    const handleSendVerification = async (email) => {
        setVerificationMessage('');
        try {
            const response = await axios.post('/api/users/email-verification', { email });
            setVerificationMessage(response.data.message || '인증 메일이 전송되었습니다.');
        } catch (error) {
            setVerificationMessage('인증 메일 전송 중 오류가 발생했습니다.');
        }
    };

    const handleVerifyCode = async (email, code) => {
        try {
            const response = await axios.post('/api/users/email-verification/confirm', { email, code });
            if (response.data.code === 'S200') {
                setIsEmailVerified(true);
                setVerificationSuccess(true);
                setSignupForm(prev => ({ ...prev, isAuthenticated: true }));
            } else {
                setVerificationMessage(response.data.message || '인증 코드 확인 실패');
            }
        } catch (error) {
            setVerificationMessage('서버 오류로 인증 코드를 확인하지 못했습니다.');
        }
    };

    const handlePasswordChange = async (e) => {
        e.preventDefault();
        if (passwordChangeForm.newPassword !== passwordChangeForm.confirmNewPassword) {
            setPasswordChangeError('새 비밀번호가 일치하지 않습니다.');
            return;
        }
        try {
            const response = await axios.patch('/api/users/password', passwordChangeForm, {
                headers: { 'Authorization': `Bearer ${localStorage.getItem('accessToken')}` }
            });
            if (response.data.code === 'S200') {
                alert('비밀번호가 성공적으로 변경되었습니다.');
                setShowPasswordChangePopup(false);
            }
        } catch (error) {
            setPasswordChangeError('비밀번호 변경에 실패했습니다.');
        }
    };

    const handleLogout = async () => {
        try {
            const accessToken = localStorage.getItem('accessToken');
            const refreshToken = localStorage.getItem('refreshToken');
    
            if (!accessToken || !refreshToken) {
                // console.warn("No tokens found, clearing storage and redirecting.");
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
    
            if (response.status !== 200) {  // response.ok 대신 response.status 사용
                // console.warn("Logout request failed. Status:", response.status);
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

    return {
        //로그인 로그아웃
        isLoggedIn,
        setIsLoggedIn,
        loginForm,
        handleLogin,
        handleLoginInputChange,
        handleLogout,
        //회원가입
        signupForm,
        signupError,
        isEmailVerified,
        verificationMessage,
        verificationSuccess,
        handleSignupInputChange,
        handleSignup,
        handleSendVerification,
        handleVerifyCode,
        //비밀번호 변경
        passwordChangeForm,
        passwordChangeError,
        handlePasswordChange,
        handlePasswordChangeClick,
        handlePasswordChangeInputChange,
        //회원 관련 팝업창
        showSigninPopup,
        showSignUpPopup,
        showPasswordChangePopup,
        handleSignUpClick,
        handleLoginClick,
        handleCloseSignUpPopup,
        handleCloseSigninPopup,
        handleClosePasswordChangePopup,
    };
};
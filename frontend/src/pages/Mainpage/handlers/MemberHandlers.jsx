import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useNotification } from '../../Notification/Notification';

export const useMemberHandlers = () => {
    const navigate = useNavigate();
    const accessToken = sessionStorage.getItem('accessToken');
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
        updatedPassword: '',
        updatedPasswordForCheck: ''
    });
    const [passwordChangeError, setPasswordChangeError] = useState('');
    const { addNotification } = useNotification();
    const [showPasswordChangePopup, setShowPasswordChangePopup] = useState(false);
    const [verificationTimer, setVerificationTimer] = useState(0);
    const [isTimerRunning, setIsTimerRunning] = useState(false);

    const isValidPassword = (password) => {
        const specialCharacterRegex = /[`~!@#$%^&*()_+-={};:'",<.>/?|]/;
        return specialCharacterRegex.test(password);
    };

    const handleLoginClick = () => navigate('/Signin');

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
        if (!isValidPassword(signupForm.password)) {
            setSignupError('비밀번호에는 최소 1개 이상의 특수문자가 포함되어야 합니다.');
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
                navigate('/signin');
            } else {
                addNotification('signup', 'error', response.data.message);
            }
        } catch (error) {
            setSignupError('회원가입 실패');
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

    const handleClosePasswordChangePopup = () => {
        setShowPasswordChangePopup(false);
        setPasswordChangeForm({
          currentPassword: '',
          updatedPassword: '',
          updatedPasswordForCheck: ''
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
                const role = response.data.data.role;
    
                sessionStorage.setItem('accessToken', accessToken);
    
                if (role === 'ROLE_USER') {
                    navigate('/');
                } else if (role === 'ROLE_ADMIN') {
                    navigate('/'); //추후 admin으로 변경
                }
            }
        } catch (error) {
            console.error('Login error:', error);
        }
    };
    

    const handleSendVerification = async (email) => {
        setVerificationTimer(300);
        setIsTimerRunning(true);
        setVerificationSuccess(false);
        try {
            const response = await axios.post('/api/users/email-verification', { email });
            addNotification('verification', 'success', '인증번호가 발송되었습니다. 5분 내에 입력하세요.');
        } catch (error) {
            addNotification('verification', 'error', '인증번호 발송 실패');
            setVerificationTimer(0);
            setIsTimerRunning(false);
        }
    };

    useEffect(() => {
        if (isTimerRunning && verificationTimer > 0) {
            const timer = setInterval(() => {
                setVerificationTimer((prev) => prev - 1);
            }, 1000);
            return () => clearInterval(timer);
        }
    }, [isTimerRunning, verificationTimer]);

    const formatTime = (seconds) => {
        if (seconds <= 0) return '';
        const minutes = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${minutes}:${secs < 10 ? '0' : ''}${secs}`;
    };

    const handleVerifyCode = async (email, code) => {
        try {
            const response = await axios.post('/api/users/email-verification/confirm', { email, code });
            if (response.data.code === 'S200') {
                setIsEmailVerified(true);
                setVerificationSuccess(true);
                setVerificationTimer(0);
                setIsTimerRunning(false);
                setSignupForm(prev => ({ ...prev, isAuthenticated: true }));
                setVerificationMessage('인증이 완료되었습니다.');
            } else {
                setVerificationMessage('인증 코드가 올바르지 않습니다.');
            }
        } catch (error) {
            setVerificationMessage('서버 오류로 인증 코드를 확인하지 못했습니다.');
        }
    };

    const handlePasswordChange = async (e) => {
        e.preventDefault();
        if (passwordChangeForm.updatedPassword !== passwordChangeForm.updatedPasswordForCheck) {
            setPasswordChangeError('새 비밀번호가 일치하지 않습니다.');
            return;
        }
        if (!isValidPassword(passwordChangeForm.updatedPassword)) {
            setPasswordChangeError('새 비밀번호에는 최소 1개 이상의 특수문자가 포함되어야 합니다.');
            return;
        }
        try {
            const accessToken = sessionStorage.getItem('accessToken');
            const response = await axios.patch('/api/users/password', passwordChangeForm, {
                headers: { 'Authorization': `Bearer ${accessToken}` }
            });
            if (response.data.code === 'S200') {
                alert('비밀번호가 성공적으로 변경되었습니다.');
                setShowPasswordChangePopup(false);
                setPasswordChangeForm({
                    currentPassword: '',
                    updatedPassword: '',
                    updatedPasswordForCheck: ''
                });
            }
        } catch (error) {
            setPasswordChangeError('비밀번호 변경에 실패했습니다.');
        }
    };

    const handleLogout = async () => {
        try {
            if (!accessToken) {
                // console.warn("No tokens found, clearing storage and redirecting.");
                sessionStorage.clear();
                navigate('/');
                return;
            }
    
            const response = await axios.post(
                '/api/users/logout',
                {}, 
                {
                    headers: {
                        'Authorization': `Bearer ${accessToken}`
                    },
                    withCredentials: true
                }
            );
    
            if (response.status !== 200) {  // response.ok 대신 response.status 사용
                // console.warn("Logout request failed. Status:", response.status);
            }
    
            sessionStorage.clear();
            window.location.reload();
        } catch (error) {
            console.error("Logout failed:", error);
            sessionStorage.clear();
            window.location.reload();
        }
    };  

    return {
        //로그인 로그아웃
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
        formatTime,
        verificationTimer,
        //비밀번호 변경
        passwordChangeForm,
        passwordChangeError,
        handlePasswordChange,
        handlePasswordChangeClick,
        handlePasswordChangeInputChange,
        //회원 관련 팝업창
        showPasswordChangePopup,
        handleLoginClick,
        handleClosePasswordChangePopup,
    };
};
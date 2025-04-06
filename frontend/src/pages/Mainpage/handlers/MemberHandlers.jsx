import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useNotification } from '../../Notification/Notification';
import { useTokenHandler } from "./TokenHandler";
import { useUserDispatch } from "./UserContext";

export const useMemberHandlers = () => {
    const navigate = useNavigate();
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
    const [loginError, setLoginError] = useState('');
    const [loginForm, setLoginForm] = useState({ email: '', password: '' });
    const [isSendingEmail, setIsSendingEmail] = useState(false);
    const [isEmailVerified, setIsEmailVerified] = useState(false);
    const [verificationMessage, setVerificationMessage] = useState('');
    const [verificationError, setVerificationError] = useState('');
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
    const { refreshTokens } = useTokenHandler();
    const setUserData = useUserDispatch();

    const isValidPassword = (password) => {
        const passwordRegex = /^(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&])[a-z\d@$!%*?&]{8,}$/;
        return passwordRegex.test(password);
    };

    const isValidStudentNum = (studentNum) => {
        return /^\d{9}$/.test(studentNum);
    };

    const handleLoginClick = () => navigate('/auth/signin');

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
            setSignupError('비밀번호는 최소 8자 이상이며, 영문 소문자, 숫자, 특수문자(@$!%*?&)를 포함해야 합니다.');
            return;
        }
        if (!isValidStudentNum(signupForm.studentNum)) {
            setSignupError('학번은 9자리 숫자로 입력해야 합니다.');
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
                navigate('/auth/signin');
            } else {
                addNotification('signup', 'error', response.data.message);
            }
        } catch (error) {
            if (error.response?.status === 409) {
                setSignupError(error.response.data.message);
            } else {
                setSignupError('회원가입 실패');
            }
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
        setLoginError('');
    
        try {
            const response = await axios.post('/api/users/login', {
                email: loginForm.email,
                password: loginForm.password,
            });
    
            if (response.data.code === 'S200') {
                const accessToken = response.data.data.accessToken;
                const role = response.data.data.role;
    
                sessionStorage.setItem('accessToken', accessToken);
                
                const userInfoResponse = await axios.get("/api/users", {
                    headers: { Authorization: `Bearer ${accessToken}` }
                });
                setUserData(userInfoResponse.data.data);
    
                if (role === 'ROLE_USER') {
                    navigate('/');
                } else {
                    const errorMessage = '로그인 중 오류가 발생했습니다.';
                    setLoginError(errorMessage);
                }
            }
        } catch (error) {
            const errorMessage = error.response?.data?.message || '로그인 중 오류가 발생했습니다.';
            console.log(error);
            setLoginError(errorMessage);
        }
    };

    const handleAdminLogin = async (e) => {
        e.preventDefault();
        setLoginError('');
    
        try {
            const response = await axios.post('/api/users/login', {
                email: loginForm.email,
                password: loginForm.password,
            });
    
            if (response.data.code === 'S200') {
                const accessToken = response.data.data.accessToken;
                const role = response.data.data.role;
    
                sessionStorage.setItem('accessToken', accessToken);
    
                if (role === 'ROLE_ADMIN') {
                    navigate('/adminpage');
                } else if (role == 'ROLE_ATTENDANT') {
                    navigate('/attendance')
                } else {
                    const errorMessage = '로그인 중 오류가 발생했습니다.';
                    setLoginError(errorMessage);
                }
            }
        } catch (error) {
            const errorMessage = error.response?.data?.message || '로그인 중 오류가 발생했습니다.';
            setLoginError(errorMessage);
        }
    };
    
    const handleSendVerification = async (email) => {
        setVerificationTimer(0);
        setIsTimerRunning(false);
        setVerificationSuccess(false);
        setVerificationError('');
        setIsSendingEmail(true);
    
        try {
            const response = await axios.post('/api/users/email-verification', { email });
    
            if (response.status === 200) {
                setVerificationTimer(300);
                setIsTimerRunning(true);
            }
        } catch (error) {
            if (error.response?.status === 429) {
                setVerificationError('인증 메일이 이미 발송되었습니다. 잠시 후 다시 시도해주세요.');
            } else {
                setVerificationError(error.response?.data?.message || '인증번호 발송 실패');
            }
        } finally {
            setIsSendingEmail(false);
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
            }
        } catch (error) {
            const errorMessage = error.response?.data?.message || '서버 오류로 인증 코드를 확인하지 못했습니다.';
            setVerificationMessage(errorMessage);
        }
    };    

    const handlePasswordChange = async (e) => {
        e.preventDefault();
        setPasswordChangeError('');
    
        if (passwordChangeForm.updatedPassword !== passwordChangeForm.updatedPasswordForCheck) {
            setPasswordChangeError('새 비밀번호가 일치하지 않습니다.');
            return;
        }
        if (!isValidPassword(passwordChangeForm.updatedPassword)) {
            setPasswordChangeError('새 비밀번호는 최소 8자 이상이며, 영문 소문자, 숫자, 특수문자(@$!%*?&)를 포함해야 합니다.');
            return;
        }
    
        try {
            let accessToken = sessionStorage.getItem('accessToken');
    
            const response = await axios.patch(
                '/api/users/password',
                passwordChangeForm,
                {
                    headers: { 'Authorization': `Bearer ${accessToken}` }
                }
            );
    
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
            if (error.response?.status === 401) {
                const newAccessToken = await refreshTokens();
    
                if (newAccessToken) {
                    return handlePasswordChange(e);
                } else {
                    sessionStorage.clear();
                    navigate('/auth/signin');
                }
            } else {
                setPasswordChangeError(error.response?.data?.message || '비밀번호 변경에 실패했습니다.');
            }
        }
    };    

    const handleLogout = async () => {
        try {
            let accessToken = sessionStorage.getItem('accessToken');
            if (!accessToken) {
                navigate('/')
                return;
            }
    
            const response = await axios.post(
                '/api/users/auth/logout',
                {}, 
                {
                    headers: { 'Authorization': `Bearer ${accessToken}` },
                    withCredentials: true
                }
            );
    
            if (response.status === 200) {
                sessionStorage.clear();
                navigate('/');
            }
        } catch (error) {
            if (error.response?.status === 401) { 
                const newAccessToken = await refreshTokens();
                if (newAccessToken) {
                    return handleLogout();
                } else {
                    sessionStorage.clear();
                    navigate('/');
                }
            }
        }
    };
    
    return {
        //로그인 로그아웃
        loginForm,
        handleLogin,
        handleAdminLogin,
        handleLoginInputChange,
        handleLogout,
        loginError,
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
        verificationError,
        isSendingEmail,
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
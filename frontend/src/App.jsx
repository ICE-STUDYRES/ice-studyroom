import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import MainPage from './pages/Mainpage/Mainpage';
import StudyRoomManage from './pages/Reservation/StudyRoomManage';
import ReservationStatus from './pages/ReservationStatus/ReservationStatus';
import StudyRoomBooking from './pages/Reservation/components/StudyRoomBookingUI';
import MyReservationStatus from './pages/ReservationStatus/MyReservationStatus';
import { Notification } from './pages/Notification/Notification';
import AttendanceSystem from './pages/ReservationStatus/AttendanceSystem';
import Adminpage from './pages/Mainpage/Adminpage/Adminpage';
import Signin from './pages/Mainpage/components/Signin';
import AdminSignin from './pages/Mainpage/components/AdminSignin';
import Signup from './pages/Mainpage/components/Signup';
import { UserProvider } from './pages/Mainpage/handlers/UserContext';
import Chatbot from './pages/Chatbot/Chatbot';
import EmailVerify from './pages/PasswordReset/components/EmailVerify';
import './main.css';

function App() {
  return (
    <UserProvider>
      <Notification>
        <Router>
          <Routes>
            {/* 기존 라우트들 */}
            <Route path="/" element={<MainPage />} />
            <Route path="/reservation/room" element={<StudyRoomBooking />} />
            <Route path="/adminpage" element={<Adminpage />} />
            <Route path="/attendance" element={<AttendanceSystem />} />
            <Route path="/reservation/manage" element={<StudyRoomManage />} />
            <Route path="/reservation/status" element={<ReservationStatus />} />
            <Route path="/reservation/my-status" element={<MyReservationStatus />} />
            <Route path="/auth/signin" element={<Signin />} />
            <Route path="/auth/signup" element={<Signup />} />
            <Route path="/auth/admin-signin" element={<AdminSignin />} />
            {/* 홈 페이지 버튼 연결 완료 후 제거 예정 */}
            <Route path="/auth/admin-signin" element={<AdminSignin />} /> 
            <Route path="/chatbot" element={<Chatbot />} />
          </Routes>
        </Router> 
      </Notification>
    </UserProvider>
  );
}

export default App;

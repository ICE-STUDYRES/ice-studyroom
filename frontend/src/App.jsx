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
import Signup from './pages/Mainpage/components/Signup';
import './main.css';

function App() {
  return (
    <Notification>
      <Router>
        <Routes>
          <Route path="/" element={<MainPage />} />
          <Route path="/reservation/room" element={<StudyRoomBooking/>} />
          <Route path="/adminpage" element={<Adminpage/>} />
          <Route path="/attendance" element={<AttendanceSystem/>} />
          <Route path="/reservation/manage" element={<StudyRoomManage/>} />
          <Route path="/reservation/status" element={<ReservationStatus/>} />
          <Route path="/reservation/my-status" element={<MyReservationStatus/>}/>
          <Route path="/auth/signin" element={<Signin/>}/>
          <Route path="/auth/signup" element={<Signup/>}/>
        </Routes>
      </Router> 
    </Notification>
  );
}

export default App;
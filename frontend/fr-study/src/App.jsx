import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import MainPage from './pages/Mainpage/Mainpage';
import StudyRoomManage from './pages/Reservation/StudyRoomManage';
import ReservationStatus from './pages/ReservationStatus/ReservationStatus';
import StudyRoomBooking from './pages/Reservation/StudyRoomBookingUI';
import MyReservationStatus from './pages/ReservationStatus/MyReservationStatus';
import './main.css';


function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/reservation/room" element={<StudyRoomBooking/>} />
        <Route path="/reservation/manage" element={<StudyRoomManage/>} />
        <Route path="/ReservationStatus" element={<ReservationStatus/>} />
        <Route path="/MyReservationStatus" element={<MyReservationStatus/>}/>
      </Routes>
    </Router>
  );
}

export default App;

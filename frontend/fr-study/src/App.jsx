import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import MainPage from './pages/Mainpage/mainpage';
import ReservationStatus from './pages/ReservationStatus/ReservationStatus';
import StudyRoomBooking from './pages/Reservation/StudyRoomBooking';
import './main.css';


function App() {
  return (

    <Router>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/reservation/room" element={<StudyRoomBooking/>} />
        <Route path="/ReservationStatus" element={<ReservationStatus/>} />
      </Routes>
    </Router>
  );
}

export default App;

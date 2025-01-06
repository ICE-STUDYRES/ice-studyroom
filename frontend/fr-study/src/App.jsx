import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { BookingProvider } from './pages/Reservation/BookingContext';
import MainPage from './pages/Mainpage/Mainpage';
import ReservationStatus from './pages/ReservationStatus/ReservationStatus';
import TimeSelection from './pages/Reservation/TimeSelection';
import RoomSelection from './pages/Reservation/RoomSelection';
import InfoInput from './pages/Reservation/Infoinput';


function App() {
  return (
    <BookingProvider>
    <Router>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/reservation/room" element={<RoomSelection />} />
        <Route path="/reservation/time" element={<TimeSelection />} />
        <Route path="/reservation/info" element={<InfoInput />} />
        <Route path="/ReservationStatus" element={<ReservationStatus/>} />
      </Routes>
    </Router>
    </BookingProvider>
  );
}

export default App;

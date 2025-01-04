import React, { createContext, useContext, useState } from 'react';

const BookingContext = createContext();

export const BookingProvider = ({ children }) => {
  const [bookingInfo, setBookingInfo] = useState({
    roomNo: '',
    timeRange: '',
    representativeName: '',  // 추가
    members: []             // 추가
  });

  const updateBookingInfo = (info) => {
    setBookingInfo(prev => ({
      ...prev,
      ...info
    }));
  };

  return (
    <BookingContext.Provider value={{ bookingInfo, updateBookingInfo }}>
      {children}
    </BookingContext.Provider>
  );
};

export const useBooking = () => {
  const context = useContext(BookingContext);
  if (!context) {
    throw new Error('useBooking must be used within a BookingProvider');
  }
  return context;
};
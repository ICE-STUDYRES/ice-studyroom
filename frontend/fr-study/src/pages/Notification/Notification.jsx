import React, { createContext, useContext, useState, useCallback } from 'react';
import { NOTIFICATION_MESSAGES, NotificationContainer } from './NotificationMessage'; 

// 🔹 Notification Context 생성
const NotificationContext = createContext(null);

// ✅ Notification Provider (전역 상태 관리)
export const Notification = ({ children }) => {
  const [notifications, setNotifications] = useState([]);

  // ✅ (카테고리 + success/error)
  const addNotification = useCallback((category, status, description = null) => {
    if (!NOTIFICATION_MESSAGES[category] || !NOTIFICATION_MESSAGES[category][status]) return; // 잘못된 타입이면 무시
  
    const id = Date.now();
    setNotifications(prev => {
      const newNotification = {
        ...NOTIFICATION_MESSAGES[category][status],
        description: description ?? NOTIFICATION_MESSAGES[category][status].description,
        id,
      };
  
      const updatedNotifications = [...prev, newNotification];
  
      // ✅ 최대 2개까지만 유지 (초과 시 오래된 항목 제거)
      if (updatedNotifications.length > 2) {
        updatedNotifications.shift(); // 가장 오래된 알림 제거
      }
  
      return updatedNotifications;
    });
  
    setTimeout(() => {
      setNotifications(prev => prev.filter(n => n.id !== id));
    }, 3000);
  }, []);
  
  

  const removeNotification = useCallback((id) => {
    setNotifications(prev => prev.filter(n => n.id !== id));
  }, []);

  return (
    <NotificationContext.Provider value={{ addNotification, removeNotification }}>
      {children}
      <NotificationContainer notifications={notifications} /> {/* ✅ 컨테이너 이동 */}
    </NotificationContext.Provider>
  );
};

// 🔹 Notification 사용을 위한 커스텀 훅
export const useNotification = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotification must be used within a Notification');
  }
  return context;
};

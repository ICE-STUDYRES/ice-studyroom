import React from 'react';
import { Bell } from 'lucide-react';

const NotificationBell = ({ hasUnread, onClick }) => {
  return (
    <button 
      onClick={onClick} 
      className="relative p-2 mr-2 hover:bg-gray-100 rounded-full transition-colors"
    >
      <Bell className="w-6 h-6 text-gray-700" />
      
      {/* MainPage에서 hasUnread=true를 줬을 때만 빨간 점 표시 */}
      {hasUnread && (
        <span className="absolute top-1.5 right-1.5 w-2.5 h-2.5 bg-red-500 rounded-full border-2 border-white"></span>
      )}
    </button>
  );
};

export default NotificationBell;
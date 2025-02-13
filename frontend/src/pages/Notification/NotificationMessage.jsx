import React from 'react';
import { CheckCircle, Clock, XCircle, UserPlus } from 'lucide-react';

//  알림 메시지 (카테고리별 success / error)
export const NOTIFICATION_MESSAGES = {
  member: {
    error: {
      type: 'member',
      title: '회원 전용',
      description: '로그인 후 이용해주세요',
    },
  },
  signup: {
    success: {
      type: 'signup',
      title: '회원가입 성공',
      description: '회원가입이 완료됐습니다.',
    },
    error: {
      type: 'signup',
      title: '회원가입 실패',
    },
  },
  reservation: {
    success: {
      type: 'reservation',
      title: '예약 완료',
      description: '내 예약 현황을 확인해주세요.',
    },
    error: {
      type: 'reservation',
      title: '예약 실패',
    },
    missingFields: {
      type: 'reservation',
      title: '예약 실패',
      description: '입력하지 않은 예약 정보가 있습니다.',
    },
    scheduleId_error: {
      type: 'reservation',
      title: '예약 실패',
      description: '선택된 시간에 예약 가능한 시간이 없습니다.',
    }
  },
  extension: {
    success: {
      type: 'extension',
      title: '예약 연장 완료',
      description: '내 예약 현황을 확인해주세요.',
    },
    error: {
      type: 'extension',
      title: '예약 연장 실패',
    },
  },
  cancellation: {
    success: {
      type: 'cancellation',
      title: '예약 취소 완료',
      description: '예약 현황에서 확인 부탁드립니다.',
    },
    error: {
      type: 'cancellation',
      title: '예약 취소 실패',
    },
  },
  attendance: {
    success: {
      type: 'attendance',
      title: '출석 완료',
      description: '정상적으로 출석이 완료됐습니다.',
    },
    late: {
      type: 'attendance',
      title: '지각 처리됨',
      description: '출석 가능 시간이 지났습니다. 지각 처리됩니다.'
    },
    notStarted: {
      type: 'attendance',
      title: '출석 불가',
    },
    expired: {
      type: 'attendance',
      title: '출석 불가',
    },
  },
};



// 🔹 카테고리별 아이콘 설정
const getIcon = (category) => {
  switch (category) {
    case 'member':
      return <UserPlus className='w-5 h-5' />;
    case 'signup':
      return <UserPlus className="w-5 h-5" />;
    case 'reservation':
      return <CheckCircle className="w-5 h-5" />;
    case 'extension':
      return <Clock className="w-5 h-5" />;
    case 'cancellation':
      return <XCircle className="w-5 h-5" />;
    case 'attendance': // ✅ 출석 관련 아이콘
      return <CheckCircle className="w-5 h-5" />;
    default:
      return null;
  }
};

// 카테고리별 스타일 적용 (배경색 & 텍스트 색상)
const getStyles = (category) => {
  switch (category) {
    case 'member':
      return 'bg-indigo-50 text-indigo-800 border-indigo-200'; // ✅ 맴버 스타일
    case 'signup':
      return 'bg-purple-50 text-purple-800 border-purple-200'; // ✅ 회원가입 스타일
    case 'reservation':
      return 'bg-green-50 text-green-800 border-green-200'; // ✅ 예약 스타일
    case 'extension':
      return 'bg-blue-50 text-blue-800 border-blue-200'; // ✅ 예약 연장 스타일
    case 'cancellation':
      return 'bg-red-50 text-red-800 border-red-200'; // ✅ 예약 취소 스타일
    case 'attendance': // ✅ 출석 스타일 (상태별 다르게 설정)
      return 'bg-yellow-50 text-yellow-800 border-yellow-200'; // 기본 출석 성공
    default:
      return 'bg-gray-50 text-gray-800 border-gray-200';
  }
};

// ✅ Notification 컨테이너 (UI)
export const NotificationContainer = ({ notifications }) => {
  return (
    <div className="fixed bottom-5 left-1/2 transform -translate-x-1/2 w-full max-w-xs space-y-2">
      {notifications.map((notification) => (
        <div
          key={notification.id}
          className={`flex items-center gap-2 px-4 py-3 rounded-lg shadow-lg border transition-all duration-300 ease-in-out ${getStyles(notification.type, notification.status)}`}
        >
          {getIcon(notification.type)}
          <div className="flex flex-col">
            <p className="text-sm font-medium">{notification.title}</p>
            {notification.description && <p className="text-xs">{notification.description}</p>}
          </div>
        </div>
      ))}
    </div>
  );
};



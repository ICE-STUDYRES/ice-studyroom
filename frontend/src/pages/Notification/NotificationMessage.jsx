import React from 'react';
import { CheckCircle, Clock, XCircle, UserPlus } from 'lucide-react';

//  알림 메시지 (카테고리별 success / error)
export const NOTIFICATION_MESSAGES = {
  penalty: {
    error: {
      type: 'penalty',
      title: '접근 제한',
      description: '패널티 해제 후 이용 가능합니다',
      status: 'error',
    }
  },
  member: {
    error: {
      type: 'member',
      title: '회원 전용',
      description: '로그인 후 이용해주세요',
      status: 'error',
    },
  },
  signup: {
    success: {
      type: 'signup',
      title: '회원가입 성공',
      description: '회원가입이 완료됐습니다.',
      status: 'success',
    },
    error: {
      type: 'signup',
      title: '회원가입 실패',
      status: 'error',
    },
  },
  reservation: {
    success: {
      type: 'reservation',
      title: '예약 완료',
      description: '내 예약 현황을 확인해주세요.',
      status: 'success',
    },
    error: {
      type: 'reservation',
      title: '예약 실패',
      status: 'error',
    },
    missingFields: {
      type: 'reservation',
      title: '예약 실패',
      description: '입력하지 않은 예약 정보가 있습니다.',
      status: 'error',
    },
    scheduleId_error: {
      type: 'reservation',
      title: '예약 실패',
      description: '선택된 시간에 예약 가능한 시간이 없습니다.',
      status: 'error',
    }
  },
  extension: {
    success: {
      type: 'extension',
      title: '예약 연장 완료',
      description: '내 예약 현황을 확인해주세요.',
      status: 'success',
    },
    error: {
      type: 'extension',
      title: '예약 연장 실패',
      status: 'error',
    },
  },
  cancellation: {
    success: {
      type: 'cancellation',
      title: '예약 취소 완료',
      description: '예약 현황에서 확인 부탁드립니다.',
      status: 'success',
    },
    error: {
      type: 'cancellation',
      title: '예약 취소 실패',
      status: 'error',
    },
  },
  attendance: {
    success: {
      type: 'attendance',
      title: '출석 완료',
      description: '정상적으로 출석이 완료됐습니다.',
      status: 'success',
    },
    late: {
      type: 'attendance',
      title: '지각 처리됨',
      description: '출석 가능 시간이 지났습니다. 지각 처리됩니다.',
      status: 'error',
    },
    notStarted: {
      type: 'attendance',
      title: '출석 불가',
      status: 'error',
    },
    expired: {
      type: 'attendance',
      title: '출석 불가',
      status: 'error',
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
    case 'attendance':
      return <CheckCircle className="w-5 h-5" />;
    default:
      return null;
  }
};

// ✅ 스타일 (success는 초록색, 나머지는 빨간색)
const getStyles = (status) => {
  if (status === 'success') {
    return 'bg-green-50 text-green-800 border-green-200'; // ✅ 성공 - 초록색
  } else {
    return 'bg-red-50 text-red-800 border-red-200'; // ❌ 실패 및 기타 상태 - 빨간색
  }
};

// ✅ Notification 컨테이너 (UI)
export const NotificationContainer = ({ notifications }) => {
  return (
    <div className="fixed bottom-5 left-1/2 transform -translate-x-1/2 w-full max-w-xs space-y-2">
      {notifications.map((notification) => (
        <div
          key={notification.id}
          className={`flex items-center gap-2 px-4 py-3 rounded-lg shadow-lg border transition-all duration-300 ease-in-out ${getStyles(notification.status)}`}
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

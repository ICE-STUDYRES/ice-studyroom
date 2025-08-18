import { CheckCircle, Loader, QrCode, X } from 'lucide-react';
import AttendanceHandler from './AttendanceHandler';

const QRAttendanceUI = () => {
  const { scanState, studentData, currentTime } = AttendanceHandler();
  
  // Render different UI based on scan state
  const renderContent = () => {
    switch (scanState) {
      case 'initial':
        return (
          <div className="flex flex-col items-center">
            <div className="mb-8 text-center">
              <QrCode className="w-24 h-24 text-gray-500 mx-auto mb-8" />
              <p className="text-3xl text-gray-700 mb-8">QR 리더기로 출석 QR을 스캔해주세요</p>
              <div className="mx-auto w-96 h-96 border-4 border-gray-200 rounded-lg overflow-hidden bg-gray-100 flex flex-col items-center justify-center">
                <QrCode className="w-24 h-24 text-gray-400 mb-4" />
                <p className="text-gray-500 text-xl mb-4">출석 QR을 1번만 스캔해주세요</p>
                <p className="text-gray-400 mb-8">소리가 나면 핸드폰을 리더기에서 멀리 해주세요.</p>
              </div>
            </div>
          </div>
        );
        
      case 'scanning':
        return (
          <div className="flex flex-col items-center justify-center py-20">
            <Loader className="w-32 h-32 text-gray-700 animate-spin mb-8" />
            <h2 className="text-4xl font-bold text-gray-700 mb-4">출석 처리 중...</h2>
            <p className="text-2xl text-gray-500">잠시만 기다려주세요</p>
          </div>
        );
            
      case 'complete-present':
        return (
          <div className="attendance-result py-16 flex flex-col items-center animate-fadeIn">
            <CheckCircle className="w-32 h-32 text-green-500 mb-8" />
            
            <div className="mb-10">
              <h2 className="text-6xl font-bold text-gray-700 mb-6">{studentData.name}</h2>
              <p className="text-5xl text-green-600 font-semibold">출석했습니다</p>
            </div>
            
            <div className="text-2xl text-gray-600 mt-4">
              {currentTime}
            </div>
            
            <p className="text-xl text-blue-600 mt-8">
              내 예약 현황에서 출석 현황을 확인해주세요
            </p>
          </div>
        );
        
      case 'complete-late':
        return (
          <div className="attendance-result py-16 flex flex-col items-center animate-fadeIn">
            <CheckCircle className="w-32 h-32 text-yellow-400 mb-8" />
            
            <div className="mb-10">
              <h2 className="text-6xl font-bold text-gray-700 mb-6">{studentData.name}</h2>
              <p className="text-5xl text-yellow-500 font-semibold">지각했습니다</p>
            </div>
            
            <div className="text-2xl text-gray-600 mt-4">
              {currentTime}
            </div>
            
            <p className="text-xl text-yellow-500 mt-8">
              내 예약 현황에서 출석 현황을 확인해주세요
            </p>
          </div>
        );
        
      case 'complete-error':
        return (
          <div className="attendance-result py-16 flex flex-col items-center animate-fadeIn">
            <X className="w-32 h-32 text-red-500 mb-8" />
            
            <div className="mb-10">
              <h2 className="text-4xl font-bold text-red-800 mb-6">{studentData?.name || "인식 오류"}</h2>
              <p className="text-3xl text-red-600 font-semibold">
                {studentData?.message || "QR 코드를 다시 스캔해주세요"}
              </p>
            </div>
            
            <div className="text-2xl text-gray-600 mt-4">
              {currentTime}
            </div>
          </div>
        );
        
      default:
        return null;
    }
  };
  
  return (
    <div className="flex flex-col items-center min-h-screen bg-gray-50">
      {/* Header */}
      <header className="w-full py-8 bg-gray-700 text-white text-center shadow-md">
        <h1 className="text-4xl font-bold">정보통신공학과 스터디룸 출석 시스템</h1>
      </header>
      
      <main className="container mx-auto p-12 my-16 bg-white rounded-xl shadow-lg max-w-4xl text-center">
        {renderContent()}
      </main>
      
      <footer className="w-full py-4 bg-gray-200 text-center mt-auto">
        <p className="text-gray-600">© 2025 정보통신공학과 스터디룸 관리시스템</p>
      </footer>
      
      {/* Global styles */}
      <style dangerouslySetInnerHTML={{__html: `
        @keyframes fadeIn {
          from { opacity: 0; transform: translateY(20px); }
          to { opacity: 1; transform: translateY(0); }
        }
        .animate-fadeIn {
          animation: fadeIn 0.5s ease-out forwards;
        }
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
        .animate-spin {
          animation: spin 1.5s linear infinite;
        }
      `}} />
    </div>
  );
};

export default QRAttendanceUI;
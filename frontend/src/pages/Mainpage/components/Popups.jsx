import React from "react";
import { X } from "lucide-react";

export const NoticePopup = ({ showNotice, handleCloseNotice, showPenaltyPopup, setShowPenaltyPopup }) => {
  if (!showNotice) return null;
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-end justify-center z-50" onClick={handleCloseNotice}>
          <div className="bg-white w-full max-w-2xl rounded-t-lg p-6 transform animate-slide-up" onClick={e => e.stopPropagation()}>
            <div className="flex justify-between items-center mb-4">
              <div className="flex gap-4">
                <button 
                  className={`text-lg font-bold pb-2 ${!showPenaltyPopup ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-500'}`}
                  onClick={() => setShowPenaltyPopup(false)}
                >
                  스터디룸 이용 주의사항
                </button>
                <button 
                  className={`text-lg font-bold pb-2 ${showPenaltyPopup ? 'border-b-2 border-blue-500 text-blue-500' : 'text-gray-500'}`}
                  onClick={() => setShowPenaltyPopup(true)}
                >
                  패널티 안내
                </button>
              </div>
              <button onClick={handleCloseNotice} className="p-1 hover:bg-gray-100 rounded-full">
                <X className="w-5 h-5" />
              </button>
            </div>
            
            {/* Notice Content */}
            {!showPenaltyPopup && (
              <div className="space-y-3 text-sm">
                <p>1. 예약 시간 1시간 안에 입실하지 않을 경우 예약은 자동 취소되며, 노쇼 패널티가 부과됩니다.</p>
                <p>2. 예약 인원 미준수 시 해당 학기 동안 예약이 제한됩니다.</p>
                <p>3. 스터디룸 내 음식물 반입 및 섭취는 엄격히 금지됩니다.</p>
                <p>4. 사용 후 정리정돈 및 쓰레기 분리수거는 필수입니다.</p>
                <p>5. 고의적인 시설물 파손 시 배상 책임이 있습니다.</p>
              </div>
            )}

            {/* Penalty Content */}
            {showPenaltyPopup && (
              <div className="space-y-3 text-sm">
                <p style={{ fontWeight: "bold"}}>예약 시간 미준수 시 패널티가 부여되며, 해당 기간 동안 예약 기능이 제한됩니다.</p>
                <p>1. No Show 시 7일간 패널티 부여</p>
                <p>2. 예약 입장 시간 30분 초과 입장 시 지각 처리 및 3일간 패널티 부여</p>
                <p>3. 예약 입장 시간 1시간 전 예약 취소 시 2일간 패널티 부여</p>
                <p>4. 패널티는 관리자 승인 후 조정 가능합니다.</p>
                <p>5. 패널티가 부여되면 예약 기능이 제한됩니다.</p>
              </div>
            )}
          </div>
        </div>
  );
};

export const PasswordChangePopup = ({ showPasswordChangePopup, handleClosePasswordChangePopup, handlePasswordChange, handlePasswordChangeInputChange, passwordChangeForm, passwordChangeError }) => {
  if (!showPasswordChangePopup) return null;
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" onClick={handleClosePasswordChangePopup}>
        <div className="bg-white rounded-lg w-96 p-6" onClick={e => e.stopPropagation()}>
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold">비밀번호 변경</h2>
            <button className="text-2xl" onClick={handleClosePasswordChangePopup}>×</button>
          </div>
          <form onSubmit={handlePasswordChange} className="space-y-4">
            <input
              type="password"
              name="currentPassword"
              value={passwordChangeForm.currentPassword}
              onChange={handlePasswordChangeInputChange}
              placeholder="현재 비밀번호"
              className="w-full p-2 border rounded"
              required
            />
            <input
              type="password"
              name="updatedPassword"
              value={passwordChangeForm.updatedPassword}
              onChange={handlePasswordChangeInputChange}
              placeholder="새 비밀번호"
              className="w-full p-2 border rounded"
              required
            />
            <input
              type="password"
              name="updatedPasswordForCheck"
              value={passwordChangeForm.updatedPasswordForCheck}
              onChange={handlePasswordChangeInputChange}
              placeholder="새 비밀번호 확인"
              className="w-full p-2 border rounded"
              required
            />
            {passwordChangeError && (
              <p className="text-red-500 text-sm">{passwordChangeError}</p>
            )}
            <button type="submit" className="w-full bg-blue-500 text-white p-2 rounded hover:bg-blue-600">
              비밀번호 변경
            </button>
          </form>
        </div>
      </div>
  );
};

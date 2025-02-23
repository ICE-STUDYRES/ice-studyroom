import React, { useState, useEffect  } from "react";
import { X } from "lucide-react";

const TermsAndPrivacyModal = ({ isOpen, onClose, onAccept, onReject, isTermsAccepted  }) => {
    const [isTermsChecked, setIsTermsChecked] = useState(false);
    const [isPrivacyChecked, setIsPrivacyChecked] = useState(false);

    useEffect(() => {
        if (isOpen) {
            setIsTermsChecked(isTermsAccepted);
            setIsPrivacyChecked(isTermsAccepted);
        }
    }, [isOpen, isTermsAccepted]);

    const handleAccept = () => {
        if (isTermsChecked && isPrivacyChecked) {
            onAccept();
            onClose();
        }
    };

    const handleClose = () => {
        if (!isTermsChecked || !isPrivacyChecked) {
            onReject(); 
        }
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
            <div className="bg-white w-4/5 md:w-1/2 lg:w-1/3 p-6 rounded-lg shadow-lg">
                {/* Header */}
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-lg font-semibold">이용약관 및 개인정보 처리방침</h2>
                    <button onClick={handleClose} className="text-gray-500 hover:text-gray-800">
                        <X className="w-5 h-5" />
                    </button>
                </div>

                {/* 이용약관 */}
                <div className="border p-4 rounded-md mb-4">
                    <h3 className="text-base font-semibold mb-2">📜 이용약관</h3>
                    <div className="h-40 overflow-y-auto text-sm text-gray-700 border p-2 rounded-md space-y-3">
                        <p><strong>제 1 조 (목적)</strong></p>
                        <p>본 약관은 스터디룸 예약 시스템(이하 "서비스")의 이용과 관련하여, 운영자와 회원 간의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.</p>

                        <p><strong>제 2 조 (용어의 정의)</strong></p>
                        <p><strong>"서비스"</strong>라 함은 학과 내 스터디룸을 예약하고 이용할 수 있도록 제공되는 온라인 시스템을 의미합니다.</p>
                        <p><strong>"회원"</strong>이라 함은 본 약관에 동의하고 학번, 이름, 학교 이메일을 통해 가입하여 서비스 이용 권한을 부여받은 자를 의미합니다.</p>
                        <p><strong>"관리자"</strong>라 함은 회원의 예약 관리 및 페널티 부여 등의 권한을 가진 운영자를 의미합니다.</p>
                        <p><strong>"스터디룸"</strong>이라 함은 회원이 예약을 통해 이용할 수 있는 공간을 의미합니다.</p>
                        <p><strong>"QR코드 인증"</strong>이라 함은 예약된 스터디룸에 입실할 때 회원이 QR코드를 스캔하여 출석을 확인하는 절차를 의미합니다.</p>
                        <p><strong>"페널티"</strong>라 함은 예약 후 미입실, 무단 사용, 규칙 위반 등의 행위에 대해 부과되는 제재 조치를 의미합니다.</p>

                        <p><strong>제 3 조 (이용약관의 효력 및 변경)</strong></p>
                        <p>본 약관은 회원가입 시 동의 절차를 거친 후 적용됩니다.</p>
                        <p>운영자는 관련 법령을 준수하는 범위 내에서 본 약관을 변경할 수 있으며, 변경된 약관은 서비스 내 공지사항을 통해 사전 고지됩니다.</p>
                        <p>회원은 변경된 약관에 동의하지 않을 경우 서비스 이용을 중단하고 탈퇴할 수 있습니다.</p>
                        
                        <p><strong>제 4 조 (회원가입 및 계정 관리)</strong></p>
                        <p>회원가입은 학번, 이름, 학교 이메일, 비밀번호 입력 후, 이메일 인증을 완료해야 최종 승인됩니다.</p>
                        <p>이메일 인증을 위해 회원은 본인의 유효한 학교 이메일을 입력해야 하며, 발송된 인증번호를 제한 시간 내에 입력해야 합니다.</p>
                        <p>비밀번호는 최소 보안 기준을 충족해야 하며, 회원이 직접 관리해야 합니다.</p>
                        <p>회원은 본인의 계정을 타인에게 공유하거나 양도할 수 없습니다.</p>
                        <p>계정 정보가 부정확하거나 타인의 정보를 도용한 경우, 서비스 이용이 제한될 수 있습니다.</p>

                        <p><strong>제 5 조 (서비스 이용 및 예약 절차)</strong></p>
                        <p>회원은 시스템을 통해 스터디룸을 예약할 수 있으며, 예약 완료 후 QR코드를 통해 입실을 인증해야 합니다.</p>
                        <p>예약 취소는 시스템이 정한 기한 내에서만 가능하며, 기한을 초과하여 예약을 취소하지 않으면 페널티가 부과될 수 있습니다.</p>
                        <p>예약된 시간 내에 QR코드 인증이 완료되지 않을 경우, <strong>"무단 미입실"</strong>로 간주되어 페널티가 부과될 수 있습니다.</p>
                        <p>동일 시간대에 여러 개의 예약을 시도하는 등의 부정 사용이 감지될 경우, 예약이 취소될 수 있습니다.</p>

                        <p><strong>제 6 조 (페널티 및 제한사항)</strong></p>
                        <p>회원이 예약 후 정해진 시간 내 입실하지 않거나 규정을 위반할 경우 페널티가 부과됩니다.</p>
                        <p>누적된 페널티에 따라 일정 기간 동안 예약이 제한될 수 있습니다.</p>
                        <p>관리자는 회원의 이용 기록을 확인하여 페널티를 부과 및 해제할 수 있습니다.</p>
                        <p>페널티에 대한 이의 신청은 운영자가 정한 절차에 따라 진행할 수 있습니다.</p>

                        <p><strong>제 7 조 (관리자의 역할 및 권한)</strong></p>
                        <p>관리자는 회원의 예약 및 페널티를 관리하고, 원활한 서비스 운영을 위한 조치를 취할 수 있습니다.</p>
                        <p>관리자는 부정 이용 행위가 확인된 경우 해당 회원의 서비스 이용을 제한할 수 있습니다.</p>

                        <p><strong>제 8 조 (개인정보 보호 및 처리)</strong></p>
                        <p>회원가입을 위해 수집되는 정보는 이름, 학번, 학교 이메일, 비밀번호이며, 서비스 운영을 위한 최소한의 정보만을 수집합니다.</p>
                        <p>이메일 인증을 위한 코드 전송 및 예약 알림 등의 목적으로 회원 이메일이 활용될 수 있습니다.</p>
                    </div>

                    <div className="flex items-center mt-2">
                        <input
                            type="checkbox"
                            id="termsCheck"
                            checked={isTermsChecked}
                            onChange={() => setIsTermsChecked(!isTermsChecked)}
                            className="w-4 h-4 border rounded"
                        />
                        <label htmlFor="termsCheck" className="text-sm text-gray-600 ml-2">
                            이용약관에 동의합니다.
                        </label>
                    </div>
                </div>

                {/* 개인정보 처리방침 */}
                <div className="border p-4 rounded-md">
                    <h3 className="text-base font-semibold mb-2">🔒 개인정보 처리방침</h3>
                    <div className="h-40 overflow-y-auto text-sm text-gray-700 border p-2 rounded-md space-y-3">
                        <p><strong>제 1 조 (개인정보 수집 및 이용 목적)</strong></p>
                        <p>스터디룸 예약 시스템(이하 "서비스")는 원활한 서비스 제공을 위해 최소한의 개인정보를 수집합니다.</p>
                        <p>수집된 개인정보는 서비스 운영, 사용자 인증, 예약 확인, 페널티 관리 등의 목적에 활용됩니다.</p>

                        <p><strong>제 2 조 (수집하는 개인정보 항목)</strong></p>
                        <p>필수정보: 학번, 이름, 학교 이메일, 비밀번호</p>
                        <p>자동 수집 정보: 로그인 기록, 예약 내역, 페널티 내역</p>

                        <p><strong>제 3 조 (개인정보 보관 및 삭제)</strong></p>
                        <p>회원이 서비스 탈퇴를 요청하면, 개인정보는 즉시 삭제되며, 이용 기록(예약, 페널티 등)은 내부 규정에 따라 일정 기간 보관 후 삭제됩니다.</p>
                        <p>법적 의무에 따라 일정 기간 보관해야 하는 정보는 해당 법령에 따릅니다.</p>

                        <p><strong>제 4 조 (개인정보 보호 조치)</strong></p>
                        <p>서비스 운영자는 회원의 개인정보 보호를 위해 적절한 보안 조치를 취하며, 암호화된 저장 및 전송을 보장합니다.</p>
                        <p>회원 계정의 비밀번호는 안전한 방식으로 암호화되어 저장되며, 운영자도 이를 확인할 수 없습니다.</p>
                        <p>개인정보 보호를 위해 무단 접근을 방지하는 보안 시스템을 운영하고 있습니다.</p>

                        <p><strong>제 5 조 (개인정보 제공 및 위탁)</strong></p>
                        <p>서비스 운영자는 원칙적으로 회원의 동의 없이 개인정보를 제3자에게 제공하지 않습니다.</p>

                        <p><strong>제 6 조 (개인정보 열람 및 수정)</strong></p>
                        <p>회원은 언제든지 본인의 개인정보를 열람하고 수정할 수 있습니다.</p>
                        <p>회원 정보의 변경은 서비스 내 설정 메뉴를 통해 직접 수정할 수 있으며, 수정 후에는 즉시 반영됩니다.</p>

                        <p><strong>제 7 조 (문의처)</strong></p>
                        <p>개인정보 보호 관련 문의 사항은 서비스 운영 담당자 이메일을 통해 접수할 수 있습니다.</p>
                        <p>회원의 개인정보 보호 및 권익 보호를 위해 최선을 다할 것을 약속합니다.</p>
                    </div>

                    <div className="flex items-center mt-2">
                        <input
                            type="checkbox"
                            id="privacyCheck"
                            checked={isPrivacyChecked}
                            onChange={() => setIsPrivacyChecked(!isPrivacyChecked)}
                            className="w-4 h-4 border rounded"
                        />
                        <label htmlFor="privacyCheck" className="text-sm text-gray-600 ml-2">
                            개인정보 처리방침에 동의합니다.
                        </label>
                    </div>
                </div>

                {/* 동의 버튼 */}
                <button
                    onClick={handleAccept}
                    disabled={!isTermsChecked || !isPrivacyChecked}
                    className={`mt-4 w-full p-2 rounded-lg font-medium ${
                        isTermsChecked && isPrivacyChecked ? "bg-blue-500 text-white hover:bg-blue-600" : "bg-gray-300 text-gray-500 cursor-not-allowed"
                    }`}
                >
                    동의하고 닫기
                </button>
            </div>
        </div>
    );
};

export default TermsAndPrivacyModal;

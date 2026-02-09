import { useEffect, useRef, useState } from "react";
import "./Chatbot.css";

import ChatbotHeader from "./components/ChatbotHeader";
import ChatbotRobot from "./components/ChatbotRobot";
import ChatbotButtons from "./components/ChatbotButtons";
import ChatbotFaqButtons from "./components/ChatbotFaqButtons";
import ChatbotFooter from "./components/ChatbotFooter";
import ChatMessage from "./components/ChatMessage";

/*  더미 데이터 (API 연동 시 삭제 예정) */
const faqData = {
  예약: ["QR 예약은?", "QR 코드 스캔으로 예약?", "예약 불가한 경우는?"],
  "체크인(QR)": ["체크인 마감 시간은?", "QR 오류 시 어떻게 하나요?"],
  연장: ["연장 가능한가요?", "연장 방법은?", "연장 비용은?"],
  "취소 / 변경": ["예약 취소 방법?", "변경 가능한가요?"],
  "이용시간 / 규정": ["이용 시간은?", "규정 위반 시 패널티?"],
  "패널티 / 제재": ["패널티 기준은?", "제재 해제는 가능한가요?"],
  "시설 / 장비": ["시설 안내?", "장비 대여 가능한가요?"],
  기타: ["기타 문의는 어떻게 하나요?"],
};

const initialMessages = [
  { text: "안녕하세요! 정보통신공학과 스터디룸 챗봇입니다.", isUser: false },
  { text: "문의 내용을 하단에 입력하거나 아래의 선택지 중 하나를 클릭해주세요.", isUser: false,
  },
];

const ChatbotPage = () => {
  const [messages, setMessages] = useState(initialMessages);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [showCategoryButtons, setShowCategoryButtons] = useState(true);
  const [lastSelectedCategory, setLastSelectedCategory] = useState(null);
  const [modalType, setModalType] = useState(null);
  const bottomRef = useRef(null);

  const scrollToBottom = () => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    const timer = setTimeout(scrollToBottom, 80);
    return () => clearTimeout(timer);
  }, [messages, selectedCategory, showCategoryButtons]);

  /* 대표질문 선택 */
  const handleCategorySelect = (category) => {
    setMessages((prev) => [...prev, { text: category, isUser: true }]);
    setSelectedCategory(category);
    setLastSelectedCategory(category);
    setShowCategoryButtons(false); // 대표질문 숨김
  };

  /* FAQ 선택 */
  const handleFaqSelect = (question) => {
    setMessages((prev) => [
      ...prev,
      { text: question, isUser: true },
      {
        text: "해당 질문에 대한 안내입니다. (더미 응답)",
        isUser: false,
        showActions: true,
      },
    ]);
    setSelectedCategory(null); 
  };

  /* 🔮 카테고리 변경하기 */
  const handleResetCategory = () => {
    setSelectedCategory(null);
    setLastSelectedCategory(null);
    setShowCategoryButtons(true);
  };

  /* 🔍 대표질문 다시보기 */
  const handleShowFaqAgain = () => {
    if (!lastSelectedCategory) return;

    setSelectedCategory(lastSelectedCategory); 
    setShowCategoryButtons(false); 
  };

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <div className="w-full max-w-[450px] h-[95vh] bg-white rounded-xl shadow-md flex flex-col overflow-hidden">
        <ChatbotHeader />

        <div className="flex-1 px-4 py-6 chat-scroll bg-[#F9FAFC] overflow-y-auto">
          <ChatbotRobot />

          {messages.map((msg, idx) => (
            <ChatMessage
              key={idx}
              isUser={msg.isUser}
              showActions={msg.showActions}
              onActionClick={setModalType}
            >
              {msg.text}
            </ChatMessage>
          ))}

          {/* 대표질문 버튼 */}
          {showCategoryButtons && (
            <ChatbotButtons onSelect={handleCategorySelect} />
          )}

          {/* FAQ 버튼 */}
          {!showCategoryButtons && selectedCategory && (
            <ChatbotFaqButtons
              faqs={faqData[selectedCategory] || []}
              onSelectFaq={handleFaqSelect}
            />
          )}

          <div ref={bottomRef} />
        </div>

        <ChatbotFooter
          onResetCategory={handleResetCategory}
          onShowFaqAgain={handleShowFaqAgain}
        />
      </div>

      {/* 공통 모달 */}
      {modalType && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-5 w-[320px]">
            <h3 className="font-semibold mb-2">
              {modalType === "evidence" && "근거"}
              {modalType === "links" && "관련 링크"}
              {modalType === "support" && "추가문의"}
            </h3>

            <p className="text-sm text-gray-600">
              현재는 더미 데이터입니다.
            </p>

            <button
              className="mt-4 text-sm text-blue-500"
              onClick={() => setModalType(null)}
            >
              닫기
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default ChatbotPage;
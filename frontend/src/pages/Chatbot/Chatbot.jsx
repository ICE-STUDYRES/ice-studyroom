import { useEffect, useRef, useState } from "react";
import "./Chatbot.css";

import ChatbotHeader from "./components/ChatbotHeader";
import ChatbotRobot from "./components/ChatbotRobot";
import ChatbotButtons from "./components/ChatbotButtons";
import ChatbotFaqButtons from "./components/ChatbotFaqButtons";
import ChatbotFooter from "./components/ChatbotFooter";
import ChatMessage from "./components/ChatMessage";

const faqData = {
  예약: ["QR 예약은?", "QR 코드스캔으로 예약?", "예약 불가?"],
  "체크인(QR)": ["QR 예약은?", "QR 코드 스캔으로 예약?", "체크인 몇 분까지 가능?"],
  연장: ["연장 가능한가요?", "연장 방법은?", "연장 비용은?"],
  "취소 / 변경": ["예약 취소 방법?", "변경 가능한가요?"],
  "이용시간 / 규정": ["이용 시간은?", "규정 위반 시 패널티?"],
  "패널티 / 제재": ["패널티 기준은?", "결제 방법은?"],
  "시설 / 장비": ["시설 안내?", "장비 대여 가능한가요?"],
  기타: ["기타 문의는 어떻게 하나요?"],
};

const initialMessages = [
  { text: "안녕하세요! 정보통신공학과 스터디룸 챗봇입니다.", isUser: false },
  { text: "문의 내용을 하단에 입력하거나 아래의 선택지 중 하나를 클릭해주세요.", isUser: false },
];

const ChatbotPage = () => {
  const [messages, setMessages] = useState(initialMessages);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const bottomRef = useRef(null);

  const scrollToBottom = () => {
    if (bottomRef.current) {
      bottomRef.current.scrollIntoView({
        behavior: "smooth",
        block: "end",
        inline: "nearest",
      });
    }
  };

  // messages 변경 시마다 스크롤 (렌더링 후 약간 지연 → 가장 안정적)
  useEffect(() => {
    const timer = setTimeout(scrollToBottom, 80); // 50~100ms 사이가 실제로 가장 잘 맞음
    return () => clearTimeout(timer);
  }, [messages, selectedCategory]);

  // 페이지 처음 로드 시에도 강제 스크롤
  useEffect(() => {
    scrollToBottom();
  }, []); // 의존성 빈 배열 → mount 시 1회만

  const handleCategorySelect = (category) => {
    setMessages((prev) => [...prev, { text: category, isUser: true }]);
    setSelectedCategory(category);
  };

  const handleResetCategory = () => {
    setSelectedCategory(null);
  };

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <div className=" w-full max-w-[450px] h-[95vh] bg-white rounded-xl shadow-md flex flex-col overflow-hidden">
        <ChatbotHeader />

        <div className="flex-1 px-4 py-6 chat-scroll bg-[#F9FAFC] overflow-y-auto">
          <ChatbotRobot />

          {messages.map((msg, idx) => (
            <ChatMessage key={idx} isUser={msg.isUser}>
              {msg.text}
            </ChatMessage>
          ))}

          {!selectedCategory && <ChatbotButtons onSelect={handleCategorySelect} />}

          {selectedCategory && (
            <ChatbotFaqButtons faqs={faqData[selectedCategory] || []} />
          )}

          {/* 스크롤 타겟 div - 채팅 영역 맨 아래에 위치 */}
          <div ref={bottomRef} style={{ height: "1px", minHeight: "1px" }} />
        </div>

        <ChatbotFooter onResetCategory={handleResetCategory} />
      </div>
    </div>
  );
};

export default ChatbotPage;
import { useEffect, useRef, useState } from "react";
import "./Chatbot.css";
import axios from "axios";

import ChatbotHeader from "./components/ChatbotHeader";
import ChatbotRobot from "./components/ChatbotRobot";
import ChatbotButtons from "./components/ChatbotButtons";
import ChatbotFaqButtons from "./components/ChatbotFaqButtons";
import ChatbotFooter from "./components/ChatbotFooter";
import ChatMessage from "./components/ChatMessage";

const initialCategories = [
  { id: "RESERVATION", name: "예약" },
  { id: "CHECKIN_QR", name: "체크인(QR)" },
  { id: "EXTEND", name: "연장" },
  { id: "CANCEL_CHANGE", name: "취소 / 변경" },
  { id: "RULES", name: "이용시간 / 규정" },
  { id: "PENALTY", name: "패널티 / 제재" },
  { id: "FACILTY", name: "시설 / 장비" },
  { id: "ETC", name: "기타" },
];

const initialFaqByCategory = {
  RESERVATION: [
    { id: 201, text: "QR 예약은?" },
    { id: 202, text: "예약 불가한 경우는?" },
  ],
  CHECKIN_QR: [
    { id: 301, text: "체크인 마감 시간은?" },
  ],
};

const initialMessages = [
  { text: "안녕하세요! 정보통신공학과 스터디룸 챗봇입니다.", isUser: false },
  { text: "문의 내용을 하단에 입력하거나 아래의 선택지 중 하나를 클릭해주세요.", isUser: false,
  },
];

const ChatbotPage = () => {
  const [categories, setCategories] = useState(initialCategories);
  const [faqsByCategory, setFaqsByCategory] = useState(initialFaqByCategory);

  const [messages, setMessages] = useState(initialMessages);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [answerCard, setAnswerCard] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
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
    setMessages((prev) => [...prev, { text: category.name, isUser: true }]);
    setSelectedCategory(category.id);
    setLastSelectedCategory(category.id);
    setShowCategoryButtons(false); // 대표질문 숨김
  };

  /* FAQ 선택 -> API 호출 */
  const handleFaqSelect = async ({ categoryId, questionId, text }) => {
    setMessages((prev) => [...prev, { text, isUser: true }]);
    setSelectedCategory(null);

    setLoading(true);
    setError(null);

    try {
      const res = await fetchChatbotAnswer({ categoryId, questionId,});
      const answer = res.data.answer
      setAnswerCard(answer);

      setMessages((prev) => [
        ...prev,
        { text: answer.summary, isUser: false, showActions: true, },
      ]);
    } catch (e) {
      setMessages((prev) => [
        ...prev,
        { text: "답변을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.", isUser: false, },
      ]);
    } finally {
      setLoading(false);
    }
  };
      const fetchChatbotAnswer = async ({ categoryId, questionId }) => {
        return axios.post("/api/v2/chatbot/answers", { categoryId, questionId });
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
              answerCard={msg.showActions ? answerCard : null}
              onActionClick={setModalType}
            >
              {msg.text}
            </ChatMessage>
          ))}

          {/* 대표질문 버튼 */}
          {showCategoryButtons && (
            <ChatbotButtons categories={categories} onSelect={handleCategorySelect} />
          )}

          {/* FAQ 버튼 */}
          {!showCategoryButtons && selectedCategory && (
            <ChatbotFaqButtons
              faqs={faqsByCategory[selectedCategory] || []}
              categoryId={selectedCategory}
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

            {modalType === "evidence" && (
              <ul className="text-sm text-gray-600 space-y-2">
                {answerCard?.evidence?.snippets?.map((s, i) => (
                  <li key={i}>• {s}</li>
                ))}
              </ul>
            )}

            {modalType === "links" && (
              <a
                href={answerCard?.links?.notionUrl}
                target="_blank"
                className="text-blue-500 text-sm"
              >
                노션 규정 페이지로 이동
              </a>
            )}

            {modalType === "support" && (
              <div className="text-sm text-gray-600">
                <p>{answerCard?.support?.managerName}</p>
                <p>{answerCard?.support?.managerPhone}</p>
              </div>
            )}


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
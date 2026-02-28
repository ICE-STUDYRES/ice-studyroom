import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Chatbot.css";
import axios from "axios";

import ChatbotHeader from "./components/ChatbotHeader";
import ChatbotRobot from "./components/ChatbotRobot";
import ChatbotButtons from "./components/ChatbotButtons";
import ChatbotFaqButtons from "./components/ChatbotFaqButtons";
import ChatbotFooter from "./components/ChatbotFooter";
import ChatMessage from "./components/ChatMessage";
import ChatbotIntro from "./components/ChatbotIntro";

const ChatbotPage = () => {
  const navigate = useNavigate();
  const [isAuthorized, setIsAuthorized] = useState(false);

  const [categories, setCategories] = useState([]);
  const [faqsByCategory, setFaqsByCategory] = useState({});
  const [messages, setMessages] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [answerCard, setAnswerCard] = useState(null);
  const [loading, setLoading] = useState(false);
  const [showCategoryButtons, setShowCategoryButtons] = useState(true);
  const [lastSelectedCategory, setLastSelectedCategory] = useState(null);
  const [modalType, setModalType] = useState(null);
  const bottomRef = useRef(null);
  const requestIdRef = useRef(0);

  /* 로그인 체크 */
  useEffect(() => {
    const token = sessionStorage.getItem("accessToken");
    if (!token) {
      alert("로그인이 필요합니다.");
      navigate("/auth/signin");
    } else {
      setIsAuthorized(true);
    }
  }, [navigate]);

  /* 순차 등장: 헤더 → 로봇 → 인트로 → 카테고리 버튼 8개 */
  const [showRobot, setShowRobot] = useState(false);
  const [showIntro, setShowIntro] = useState(false);
  const [showInitialMessages, setShowInitialMessages] = useState(false);
  const [showButtons, setShowButtons] = useState(false);

  useEffect(() => {
    const step1 = setTimeout(() => setShowRobot(true));
    const step2 = setTimeout(() => setShowIntro(true));
    const step3 = setTimeout(() => setShowInitialMessages(true), 50);
    const step4 = setTimeout(() => setShowButtons(true), 500);

    return () => {
      clearTimeout(step1);
      clearTimeout(step2);
      clearTimeout(step3);
      clearTimeout(step4);
    };
  }, []);

  /* 스크롤 */
  const scrollToBottom = () => {
    bottomRef.current?.scrollIntoView({ 
    behavior: "smooth",
    block: "end",
   });
  };

  useEffect(() => {
    const timer = setTimeout(scrollToBottom, 80);
    return () => clearTimeout(timer);
  }, [messages, selectedCategory, showCategoryButtons, showInitialMessages]);

  /* 진행 중 요청 취소 */
  const cancelOngoingRequest = () => {
    requestIdRef.current++;
    setLoading(false);
  };

  /* 이벤트 전송 */
  const sendChatbotEvent = async ({ eventType, categoryId = null, questionId = null, buttonType = null }) => {
    try {
      const accessToken = sessionStorage.getItem("accessToken");
      await axios.post(
        "/api/v2/chatbot/events",
        {
          eventType,
          categoryId,
          questionId,
          buttonType,
          screen: "CHATBOT_CHAT_PAGE",
          occurredAt: new Date().toISOString().slice(0, 19),
        },
        { headers: { Authorization: `Bearer ${accessToken}` } }
      );
    } catch (e) {
      console.error("챗봇 이벤트 전송 실패", e);
    }
  };

  /* 카테고리 조회 */
  const fetchCategories = async () => {
    try {
      const accessToken = sessionStorage.getItem("accessToken");
      const res = await axios.get("/api/v2/chatbot/categories", {
        headers: { Authorization: `Bearer ${accessToken}` },
      });
      const serverCategories = res.data.data.categories ?? [];
      const mapped = serverCategories.map((cat) => ({
        id: cat.categoryId,
        name: cat.label,
      }));
      setCategories(mapped);
    } catch (error) {
      console.error("카테고리 조회 실패", error);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  /* 답변 조회 */
  const fetchChatbotAnswer = async ({ categoryId, questionId }) => {
    const accessToken = sessionStorage.getItem("accessToken");
    return axios.post(
      "/api/v2/chatbot/answers",
      { categoryId, questionId },
      { headers: { Authorization: `Bearer ${accessToken}` } }
    );
  };

  /* 카테고리 선택 */
  const handleCategorySelect = async (category) => {
    cancelOngoingRequest();
    setMessages((prev) => [...prev, { text: category.name, isUser: true }]);
    setSelectedCategory(category.id);
    setLastSelectedCategory(category.id);
    setShowCategoryButtons(false);

    await sendChatbotEvent({ eventType: "CATEGORY_SELECT", categoryId: category.id });

    try {
      const accessToken = sessionStorage.getItem("accessToken");
      const res = await axios.get(
        `/api/v2/chatbot/categories/${category.id}/questions?includeClickCount=false`,
        { headers: { Authorization: `Bearer ${accessToken}` } }
      );

      const faqList = res.data.data.questions.map((q) => ({
        id: q.questionId,
        text: q.content,
      }));

      setFaqsByCategory((prev) => ({
        ...prev,
        [category.id]: faqList,
      }));
    } catch (e) {
      console.error("FAQ 조회 실패", e);
      setFaqsByCategory((prev) => ({
        ...prev,
        [category.id]: [{ id: 0, text: "FAQ를 불러올 수 없습니다." }],
      }));
    }
  };

  /* FAQ 선택 */
  const handleFaqSelect = async ({ categoryId, questionId, text }) => {
    const currentRequestId = ++requestIdRef.current;
    setMessages((prev) => [...prev, { text, isUser: true }]);
    setSelectedCategory(null);
    setLoading(true);

    await sendChatbotEvent({ eventType: "QUESTION_CLICK", categoryId, questionId });

    try {
      const res = await fetchChatbotAnswer({ categoryId, questionId });
      if (currentRequestId !== requestIdRef.current) return;
      const answer = res.data.data;
      setAnswerCard(answer);

      await new Promise((resolve) => setTimeout(resolve, 1500));
      if (currentRequestId !== requestIdRef.current) return;

      setMessages((prev) => [...prev, { text: answer.summary, isUser: false, showActions: true }]);
    } catch (e) {
      if (currentRequestId !== requestIdRef.current) return;
      setMessages((prev) => [
        ...prev,
        { text: "답변을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.", isUser: false },
      ]);
      console.error("답변 API 호출 실패", e);
    } finally {
      if (currentRequestId === requestIdRef.current) setLoading(false);
    }
  };

  /* 카테고리 초기화 */
  const handleResetCategory = () => {
    cancelOngoingRequest();
    setSelectedCategory(null);
    setLastSelectedCategory(null);
    setShowCategoryButtons(true);
    sendChatbotEvent({ eventType: "CATEGORY_CHANGE", categoryId: lastSelectedCategory });
  };

  /* 대표질문 다시보기 */
  const handleShowFaqAgain = () => {
    if (!lastSelectedCategory) return;
    cancelOngoingRequest();
    setSelectedCategory(lastSelectedCategory);
    setShowCategoryButtons(false);

    sendChatbotEvent({ eventType: "QUESTION_RELOAD", categoryId: lastSelectedCategory });
  };

  if (!isAuthorized) return null;
  return (
    <div className="chat-container min-h-screen bg-blue-50 flex items-center justify-center">
      <div className="w-full max-w-[480px] h-screen bg-white flex flex-col">
        <ChatbotHeader />
        <div className="flex-1 px-4 py-4 chat-scroll bg-[#F9FAFC] overflow-y-auto">
          {/* 로봇 */}
          {showRobot && <ChatbotRobot />}
          {/* 인트로 */}
          {showIntro && <ChatbotIntro />}

          {messages.map((msg, idx) => (
            <ChatMessage
              key={idx}
              isUser={msg.isUser}
              showActions={msg.showActions}
              answerCard={msg.showActions ? answerCard : null}
              onActionClick={async (buttonType) => {
                setModalType(buttonType);
                await sendChatbotEvent({
                  eventType: "BUTTON_CLICK",
                  categoryId: answerCard?.categoryId,
                  questionId: answerCard?.questionId,
                  buttonType: buttonType.toUpperCase(),
                });
              }}
            >
              {msg.text}
            </ChatMessage>
          ))}

          {/* Thinking */}
          {loading && (
            <div className="thinking flex justify-start items-center gap-2 mt-2 mb-2">
              <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce delay-0"></span>
              <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce delay-150"></span>
              <span className="w-2 h-2 bg-gray-400 rounded-full animate-bounce delay-300"></span>
              <span className="text-gray-500 text-sm ml-2">답변을 생각하는 중입니다...</span>
            </div>
          )}

          {/* 카테고리 버튼 */}
          {showButtons && showCategoryButtons && categories.length > 0 && (
            <ChatbotButtons categories={categories} onSelect={handleCategorySelect} />
          )}

          {!showCategoryButtons && selectedCategory && (
            <ChatbotFaqButtons
              faqs={faqsByCategory[selectedCategory] || []}
              categoryId={selectedCategory}
              onSelectFaq={handleFaqSelect}
            />
          )}

          <div ref={bottomRef} />
        </div>

        <ChatbotFooter onResetCategory={handleResetCategory} onShowFaqAgain={handleShowFaqAgain} />
      </div>

      {/* 모달 */}
      {modalType && answerCard && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="relative bg-white rounded-2xl shadow-xl p-6 w-[340px] max-h-[70vh] flex flex-col">
            <h3 className="mb-4 font-semibold">
              {modalType === "evidence" && "근거"}
              {modalType === "links" && "관련 링크"}
              {modalType === "support" && "추가문의"}
            </h3>

            {modalType === "evidence" && (
              <div className="flex-1 overflow-y-auto pr-2">
                <ul className="text-sm text-gray-600 space-y-2">
                  {answerCard?.evidence?.snippets?.map((s, i) => (
                    <li key={i}>• {s}</li>
                  ))}
                </ul>
              </div>
            )}

            {modalType === "links" && (
              <a
                href={answerCard?.links?.notionUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="text-blue-500 text-sm block"
              >
                노션 규정 페이지로 이동
              </a>
            )}

            {modalType === "support" && (
              <div className="text-sm text-gray-600 space-y-2">
                <p>
                  <span className="font-medium">담당자:</span>김정통
                </p>
                <p>
                  <span className="font-medium">담당자 연락처:</span>010-1234-5678
                </p>
                <p>
                  <span className="font-medium">정보통신공학과 스터디룸 오픈채팅:</span>{" "}
                  <a href="https://open.kakao.com/o/giOS427b" target="_blank" rel="noopener noreferrer" className="text-blue-500 underline">
                    바로가기
                  </a>
                </p>
              </div>
            )}

            <button
              className="absolute top-4 right-4 w-8 h-8 flex items-center justify-center rounded-full text-gray-400 hover:bg-gray-100 hover:text-gray-700 transition"
              onClick={() => setModalType(null)}
            >
              <span className="text-sm">✕</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default ChatbotPage;
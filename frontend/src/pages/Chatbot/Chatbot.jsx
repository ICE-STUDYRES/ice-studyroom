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

const initialMessages = [
  { text: "안녕하세요! 정보통신공학과 스터디룸 챗봇입니다.", isUser: false },
  { text: "궁금한 내용을 선택하시면 바로 안내해드릴게요!", isUser: false },
];

const ChatbotPage = () => {
  const navigate = useNavigate();
  const [isAuthorized, setIsAuthorized] = useState(false);

  const [categories, setCategories] = useState([]);
  const [faqsByCategory, setFaqsByCategory] = useState({});
  const [messages, setMessages] = useState(initialMessages);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [answerCard, setAnswerCard] = useState(null);
  const [loading, setLoading] = useState(false);
  const [showCategoryButtons, setShowCategoryButtons] = useState(true);
  const [lastSelectedCategory, setLastSelectedCategory] = useState(null);
  const [modalType, setModalType] = useState(null);
  const bottomRef = useRef(null);

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

  /* 스크롤 자동 이동 */
  const scrollToBottom = () => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    const timer = setTimeout(scrollToBottom, 80);
    return () => clearTimeout(timer);
  }, [messages, selectedCategory, showCategoryButtons]);

  /* 사용자 이벤트 로그 전송 */
  const sendChatbotEvent = async ({
    eventType,
    categoryId = null,
    questionId = null,
    buttonType = null,
  }) => {
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
    setMessages((prev) => [...prev, { text: category.name, isUser: true }]);
    setSelectedCategory(category.id);
    setLastSelectedCategory(category.id);
    setShowCategoryButtons(false);

    await sendChatbotEvent({
      eventType: "CATEGORY_SELECT",
      categoryId: category.id,
    });

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
    setMessages((prev) => [...prev, { text, isUser: true }]);
    setSelectedCategory(null);
    setLoading(true);

    await sendChatbotEvent({
      eventType: "QUESTION_CLICK",
      categoryId,
      questionId,
    });

    try {
      const res = await fetchChatbotAnswer({ categoryId, questionId });
      const answer = res.data.data;
      setAnswerCard(answer);

      await new Promise((resolve) => setTimeout(resolve, 1500));

      setMessages((prev) => [
        ...prev,
        { text: answer.summary, isUser: false, showActions: true },
      ]);
    } catch (e) {
      await new Promise((resolve) => setTimeout(resolve, 1500));
      setMessages((prev) => [
        ...prev,
        {
          text: "답변을 불러오지 못했습니다. 잠시 후 다시 시도해주세요.",
          isUser: false,
        },
      ]);
      console.error("답변 API 호출 실패", e);
    } finally {
      setLoading(false);
    }
  };

  /* 카테고리 초기화 */
  const handleResetCategory = () => {
    setSelectedCategory(null);
    setLastSelectedCategory(null);
    setShowCategoryButtons(true);
    
    sendChatbotEvent({
      eventType: "CATEGORY_CHANGE",
      categoryId: lastSelectedCategory,
    });
  };

  /* 대표질문 다시보기 */
  const handleShowFaqAgain = () => {
    if (!lastSelectedCategory) return;
    setSelectedCategory(lastSelectedCategory);
    setShowCategoryButtons(false);

    sendChatbotEvent({
      eventType: "QUESTION_RELOAD",
      categoryId: lastSelectedCategory,
    });
  };

  if (!isAuthorized) return null;
  return (
    <div className="chat-container min-h-screen bg-blue-50 flex items-center justify-center">
      <div className="w-full max-w-[460px] h-[98vh] bg-white rounded-xl shadow-md flex flex-col overflow-hidden">
        <ChatbotHeader />
        <div className="flex-1 px-4 py-4 chat-scroll bg-[#F9FAFC] overflow-y-auto">
          <ChatbotRobot />

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

          {loading && (
            <div className="text-sm text-gray-400 mt-2">
              답변을 생각하는 중입니다...
            </div>
          )}

          {showCategoryButtons && categories.length > 0 && (
            <ChatbotButtons
              categories={categories}
              onSelect={handleCategorySelect}
            />
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

        <ChatbotFooter
          onResetCategory={handleResetCategory}
          onShowFaqAgain={handleShowFaqAgain}
        />
      </div>

      {/* 공통 모달 */}
      {modalType && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="relative bg-white rounded-2xl shadow-xl p-6 w-[340px]">
            <h3 className="mb-4 font-semibold">
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
                rel="noopener noreferrer"
                className="text-blue-500 text-sm block"
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

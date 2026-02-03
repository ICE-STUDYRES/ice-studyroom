import { useEffect, useRef } from "react";
import "./Chatbot.css";

import ChatbotHeader from "./components/ChatbotHeader";
import ChatbotRobot from "./components/ChatbotRobot";
import ChatbotIntro from "./components/ChatbotIntro";
import ChatbotButtons from "./components/ChatbotButtons";
import ChatbotFooter from "./components/ChatbotFooter";

const ChatbotPage = () => {
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, []);

  return (
    /* 🔹 바깥: 화면 중앙 정렬용 wrapper */
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      
      {/* 🔹 이게 웹앱 카드 컨테이너 */}
      <div className="w-full max-w-[390px] h-[700px] bg-white rounded-xl shadow-md flex flex-col overflow-hidden">
        
        <ChatbotHeader />

        {/* 🔹 채팅 영역만 스크롤 */}
        <div className="flex-1 px-4 py-6 chat-scroll">
          <ChatbotRobot />
          <ChatbotIntro />
          <ChatbotButtons />
          <div ref={bottomRef} />
        </div>

        <ChatbotFooter />
      </div>
    </div>
  );
};

export default ChatbotPage;

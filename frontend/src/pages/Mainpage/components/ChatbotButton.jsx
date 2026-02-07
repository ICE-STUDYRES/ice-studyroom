import { useState } from "react";
import { useNavigate } from "react-router-dom";

const ChatbotButton = () => {
  const navigate = useNavigate();
  const [hovered, setHovered] = useState(false);

  const accentColor = "bg-blue-200";
  const accentBorder = "border-blue-200";

  return (
    <>
      {/* 스타일 정의 */}
      <style>
        {`
          @keyframes eff35-move {
            30% {
              transform: translate3d(0, -5px, 0) rotate(5deg);
            }
            50% {
              transform: translate3d(0, -3px, 0) rotate(-4deg);
            }
            80% {
              transform: translate3d(0, 0, 0) rotate(-3deg);
            }
            100% {
              transform: rotate(0deg);
            }
          }

          .animate-eff35 {
            animation-name: eff35-move;
            animation-duration: 0.4s;
            animation-timing-function: linear;
            animation-iteration-count: 1;
          }
        `}
      </style>

      {/* 하단 고정 바 */}
      <div
        className={`
          fixed bottom-0 left-0 right-0 mx-auto
          w-full max-w-[480px] h-12 sm:h-16
          ${accentColor}
          border-t border-teal-200/70
          z-40
        `}
      />

      {/* 챗봇 버튼 + 툴팁 그룹 */}
      <div
        className="fixed bottom-6 left-1/2 -translate-x-1/2 z-50"
        onMouseEnter={() => setHovered(true)}
        onMouseLeave={() => setHovered(false)}
      >
        {/* 말풍선 */}
        <div
          className={`
            absolute bottom-full left-1/2 mb-1
            px-4 py-2.5
            bg-gray-600/95
            text-white text-sm font-medium
            rounded-2xl rounded-bl-none
            shadow-xl whitespace-nowrap
            border border-gray-600/50
            transition-all duration-200 ease-out
            ${hovered ? "opacity-100 translate-x-4 scale-100" : "opacity-0 translate-y-3 scale-95 pointer-events-none"}
          `}
        >
          챗봇이랑 대화해보세요
        </div>

        {/* 버튼 자체 */}
        <button
          onClick={() => navigate("/chatbot")}
          className={`
            w-14 h-14 sm:w-16 sm:h-16
            bg-white
            rounded-3xl
            flex items-center justify-center
            border-4 ${accentBorder}
            shadow-lg shadow-black/20
            transition-all duration-300 ease-out
            overflow-hidden
          `}
        >
          <img
            src="src/assets/images/chatbot/Chatbot.png"
            alt="챗봇"
            className={`
              w-8 h-8 sm:w-9 sm:h-9 object-contain
              ${hovered ? "animate-eff35" : ""}
            `}
          />
        </button>
      </div>
    </>
  );
};

export default ChatbotButton;

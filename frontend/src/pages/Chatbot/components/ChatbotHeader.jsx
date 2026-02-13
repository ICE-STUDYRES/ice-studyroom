import { useNavigate } from "react-router-dom";
import { useState } from "react";

const ChatbotHeader = () => {
  const navigate = useNavigate();
  const [hovered, setHovered] = useState(false);

  return (
    <>
      <style>
        {`
          @keyframes eff35-move {
            30% { transform: translate3d(0, -5px, 0) rotate(5deg); }
            50% { transform: translate3d(0, -3px, 0) rotate(-4deg); }
            80% { transform: translate3d(0, 0, 0) rotate(-3deg); }
            100% { transform: rotate(0deg); }
          }

          .animate-eff35 {
            animation: eff35-move 0.4s linear;
          }
        `}
      </style>

      <div className="relative flex items-center justify-center px-4 py-4 border-b bg-white">
        <span className="font-semibold text-sm text-gray-800">
          ICE Studyroom
        </span>

        <button
          onClick={() => navigate("/")}
          onMouseEnter={() => setHovered(true)}
          onMouseLeave={() => setHovered(false)}
          className="absolute right-4
                     w-9 h-9
                     flex items-center justify-center
                     rounded-full
                     text-gray-400
                     hover:bg-gray-100
                     hover:text-gray-700
                     transition"
        >
          <span
            className={`text-base leading-none ${
              hovered ? "animate-eff35" : ""
            }`}
          >
            ✕
          </span>
        </button>
      </div>
    </>
  );
};

export default ChatbotHeader;
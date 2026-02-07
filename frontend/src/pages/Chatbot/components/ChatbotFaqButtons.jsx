const ChatbotFaqButtons = ({ faqs, onSelectFaq }) => {
  return (
    <div className="mt-6 mb-4 space-y-2">
      {faqs.map((question, idx) => (
        <button
          key={idx}
          onClick={() => onSelectFaq(question)}
          className="
            w-full
            flex items-center justify-between
            px-4 py-3
            border border-gray-200
            rounded-lg
            bg-white
            text-xs text-gray-800
            hover:bg-gray-50
            transition
          "
        >
          <span className="flex-1 text-center">{question}</span>
          <span className="text-gray-400 text-base">›</span>
        </button>
      ))}
    </div>
  );
};

export default ChatbotFaqButtons;
